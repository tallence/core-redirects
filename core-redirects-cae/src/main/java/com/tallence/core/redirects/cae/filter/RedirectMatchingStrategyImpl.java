package com.tallence.core.redirects.cae.filter;

import com.coremedia.blueprint.base.multisite.cae.SiteResolver;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SiteHelper;
import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.cae.service.RedirectService;
import com.tallence.core.redirects.cae.service.SiteRedirects;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

@Service
public class RedirectMatchingStrategyImpl implements RedirectMatchingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectMatchingStrategyImpl.class);

  private final RedirectService redirectService;
  private final SiteResolver siteResolver;

  public RedirectMatchingStrategyImpl(RedirectService redirectService, SiteResolver siteResolver) {
    this.redirectService = redirectService;
    this.siteResolver = siteResolver;
  }

  @Override
  public Result getMatchingRedirect(HttpServletRequest request) {

    // Fetch redirects
    SiteRedirects redirects = getSiteRedirects(request);
    return determinePreAction(redirects, request);
  }

  /**
   * Fetch redirects for the given site.
   *
   * @param request current request
   * @return a result holder
   */
  @NonNull
  private SiteRedirects getSiteRedirects(HttpServletRequest request) {
    // Determine site (in order to fetch the redirects for it)
    Site site = getSiteFromRequest(request);

    // Fetch redirect holder for site
    return redirectService.getRedirectsForSite(site);
  }

  /**
   * Because the {@code SiteFilter} might have run or not (depending on the setup), we can either simply take
   * the current site from the request or we have to parse it outselves.
   */
  @Nullable
  private Site getSiteFromRequest(HttpServletRequest request) {
    // Check first, if someone has already made the lookup
    Site site = SiteHelper.getSiteFromRequest(request);

    if (site == null) {
      // If site is not in request, it means that the SiteFilter has not run yet. So we fetch the site ourselves.
      // This code is shamelessly copied from SiteFilter.java in cae-base-lib
      String pathInfo = request.getPathInfo();
      try {
        if (!StringUtils.hasLength(pathInfo) || "/".equals(pathInfo)) {
          LOG.debug("Could not determine a site without a path info in request {}", request);
        } else {
          site = siteResolver.findSiteByPath(pathInfo);
        }
      } catch (Exception e) {
        LOG.warn("Could not determine the site for the request", e);
      }

    }
    return site;
  }

  /**
   * Determines, if a redirect should be executed now, after handling or never.
   */
  private Result determinePreAction(SiteRedirects redirects, HttpServletRequest request) {

    if (request == null || request.getPathInfo() == null) {
      return Result.none();
    }

    var pathInfo = request.getPathInfo();
    if (pathInfo.endsWith("/")) {
      pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
    }

    Redirect redirect = getMatchingRedirect(redirects.getPlainRedirects().get(pathInfo), request);
    if (redirect == null) {
      for (Map.Entry<Pattern, List<Redirect>> patternRedirect : redirects.getPatternRedirects().entrySet()) {
        if (patternRedirect.getKey().matcher(pathInfo).matches()) {
          redirect = getMatchingRedirect(patternRedirect.getValue(), request);
          break;
        }
      }
    }
    if (redirect != null) {
      return redirect.getRedirectType() == RedirectType.ALWAYS ? Result.send(redirect) : Result.wrap(redirect);
    }
    return Result.none();
  }

  /**
   * Look into the plain redirects map with the given pathInfo.
   *
   * Use the redirect with the highest number of (matching) sourceUrlParams.
   *
   * @param potentialRedirects redirects which match the request path.
   * @param request the request
   */
  private Redirect getMatchingRedirect(List<Redirect> potentialRedirects, HttpServletRequest request) {

    final var requestParameterMap = request.getParameterMap();
    List<Redirect> redirects = potentialRedirects.stream()
            .filter(r -> r.getSourceParameters().stream().allMatch(s -> matchesUrlParams(s, requestParameterMap)))
            .filter(r -> !isTargetInvalid(r.getTarget()))
            .collect(Collectors.toList());
    if (redirects.size() <= 1) {
      return redirects.size() == 1 ? redirects.get(0) : null;
    }

    final var numberOfParams = redirects.stream().collect(Collectors.toMap(r -> r.getSourceParameters().size(), Function.identity()));
    return numberOfParams.entrySet()
            .stream().max(comparingByKey())
            .map(Map.Entry::getValue)
            .orElse(null);
  }

  private boolean matchesUrlParams(RedirectSourceParameter sourceParameter, Map<String, String[]> requestParameterMap) {
    final var values = requestParameterMap.get(sourceParameter.getName());

    if (!RedirectSourceParameter.Operator.EQUALS.equals(sourceParameter.getOperator())) {
      LOG.error("No other operator than EQUALS is currently supported");
      return false;
    }

    return Arrays.stream(values)
            .filter(Objects::nonNull)
            .anyMatch(v -> v.equalsIgnoreCase(sourceParameter.getValue()));
  }

  private boolean isTargetInvalid(Content targetLink) {
    Calendar now = Calendar.getInstance();
    Calendar validFrom = targetLink.getDate("validFrom");
    if (validFrom != null && validFrom.after(now)) {
      return true;
    }
    Calendar validTo = targetLink.getDate("validTo");
    return validTo != null && validTo.before(now);
  }
}
