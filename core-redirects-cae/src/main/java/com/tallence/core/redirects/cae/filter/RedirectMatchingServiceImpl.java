/*
 * Copyright 2019 Tallence AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import static com.coremedia.cap.common.IdHelper.parseContentId;
import static java.util.Map.Entry.comparingByKey;

/**
 * Default Strategy to select a redirect in the {@link SiteRedirects} which matches the given request.
 *
 * It checks:
 * <ul>
 *  <li>if the {@link HttpServletRequest#getPathInfo()} matches the {@link Redirect#getSource()}</li>
 *  <li>if the {@link HttpServletRequest#getParameterMap()} is matches by all {@link Redirect#getSourceParameters()}</li>
 * </ul>
 *
 * If you want to implement a custom strategy: Try to override {@link #determinePreAction} or {@link #checkUrlParams}.
 * If you need more methods to be "protected" feel free to create an issue in the gitHub repo.
 */
@Service
public class RedirectMatchingServiceImpl implements RedirectMatchingService {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectMatchingServiceImpl.class);

  private final RedirectService redirectService;
  private final SiteResolver siteResolver;

  public RedirectMatchingServiceImpl(RedirectService redirectService, SiteResolver siteResolver) {
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
          LOG.debug("Could not determine a site without a site name in the path info, request: {}", request);
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
   * Lookup the plain- and patternRedirects in the given redirects for the given request
   */
  protected Result determinePreAction(SiteRedirects redirects, HttpServletRequest request) {

    if (request == null || request.getPathInfo() == null) {
      return Result.none();
    }

    var pathInfo = request.getPathInfo().toLowerCase();
    if (pathInfo.endsWith("/")) {
      pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
    }

    var redirect = Optional.ofNullable(redirects.getPlainRedirects().get(pathInfo))
            .map(list -> checkUrlParams(list, request)).orElse(null);
    if (redirect == null) {
      for (Map.Entry<Pattern, List<Redirect>> patternRedirect : redirects.getPatternRedirects().entrySet()) {
        if (patternRedirect.getKey().matcher(pathInfo).matches()) {
          redirect = checkUrlParams(patternRedirect.getValue(), request);
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
   * Resolve a redirect in the given list.
   * A redirect is chosen, if all of its url parameters match the given request.
   *
   * If more than one redirect match: Use the redirect with the highest number of (matching) sourceUrlParams.
   *
   * @param potentialRedirects redirects which match the request path.
   * @param request the request
   */
  protected Redirect checkUrlParams(@NonNull List<Redirect> potentialRedirects, HttpServletRequest request) {

    final var requestParameterMap = request.getParameterMap();
    List<Redirect> redirects = potentialRedirects.stream()
            .filter(r -> r.getSourceParameters().stream().allMatch(s -> matchesSourceParam(s, requestParameterMap)))
            .filter(r -> r.getTarget() == null || isTargetValid(r.getTarget()))
            .collect(Collectors.toList());
    if (redirects.size() <= 1) {
      return redirects.size() == 1 ? redirects.get(0) : null;
    }

    //More than one potential redirect left: pick the one, with the highest amount of parameters.
    //If more than one redirect has the same number of parameters: to be deterministic, pick the one with the lowest contentId
    final var numberOfParams = redirects.stream()
            .collect(Collectors.toMap(r -> r.getSourceParameters().size(), Function.identity(),
                    (o, o2) -> parseContentId(o.getContentId()) < parseContentId(o2.getContentId()) ? o : o2));
    return numberOfParams.entrySet()
            .stream().max(comparingByKey())
            .map(Map.Entry::getValue)
            .orElse(null);
  }

  private boolean matchesSourceParam(RedirectSourceParameter sourceParameter, Map<String, String[]> requestParameterMap) {

    if (!RedirectSourceParameter.Operator.EQUALS.equals(sourceParameter.getOperator())) {
      LOG.error("No other operator than EQUALS is currently supported");
      return false;
    }

    return Optional.ofNullable(requestParameterMap.get(sourceParameter.getName()))
            .stream()
            .flatMap(Arrays::stream)
            .anyMatch(v -> v.equalsIgnoreCase(sourceParameter.getValue()));
  }

  /**
   * Cannot use the {@link com.coremedia.blueprint.common.services.validation.ValidationService}
   * because it does not work with content objects.
   * @return true, if the target is valid
   */
  private boolean isTargetValid(Content targetLink) {
    Calendar now = Calendar.getInstance();
    Calendar validFrom = targetLink.getDate("validFrom");
    if (validFrom != null && validFrom.after(now)) {
      return false;
    }
    Calendar validTo = targetLink.getDate("validTo");
    return validTo == null || validTo.after(now);
  }
}
