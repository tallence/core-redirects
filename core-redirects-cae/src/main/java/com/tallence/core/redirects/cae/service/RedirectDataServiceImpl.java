package com.tallence.core.redirects.cae.service;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.annotations.VisibleForTesting;
import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.model.SourceUrlType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tallence.core.redirects.cae.model.Redirect.SOURCE_URL;
import static com.tallence.core.redirects.cae.model.Redirect.TARGET_LINK;

/**
 * Service for resolving and building redirects.
 *
 * The redirects for all sites are created at postConstruct. If a changeEvent arrives during the initial creation,
 * the job waits for the corresponding future to complete, see {@link #initialJobs}.
 *
 */
@Scope("singleton")
@Service
public class RedirectDataServiceImpl implements RedirectDataService {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectDataServiceImpl.class);

  // This query fetches all redirects below a specific folder
  private static final String FETCH_REDIRECTS_QUERY = "TYPE " + Redirect.NAME + ": isInProduction AND BELOW ?0";

  private final ContentRepository contentRepository;
  private final ExecutorService redirectRecomputeThreadPool;
  private final SitesService sitesService;
  private final String redirectsPath;


  private Map<Site, SiteRedirects> redirects = new HashMap<>();
  private Map<Site, Future<Boolean>> initialJobs = new HashMap<>();

  public RedirectDataServiceImpl(ContentRepository contentRepository,
                                 @Qualifier("redirectRecomputeThreadPool") ExecutorService redirectRecomputeThreadPool,
                                 SitesService sitesService,
                                 @Value("${core.redirects.path}") String redirectsPath) {
    this.contentRepository = contentRepository;
    this.redirectRecomputeThreadPool = redirectRecomputeThreadPool;
    this.sitesService = sitesService;
    this.redirectsPath = redirectsPath;
  }

  @Override
  public SiteRedirects getForSite(Site site) {
    SiteRedirects result = redirects.get(site);
    return result != null ? result : new SiteRedirects();
  }

  @VisibleForTesting
  public ExecutorService getExecutorService() {
    return redirectRecomputeThreadPool;
  }

  @Override
  public void addJob(Site site, JobType type, Content content) {
    redirectRecomputeThreadPool.submit(new RedirectJob(site, type, content));
  }

  private SiteRedirects computeInitial(Site site) {

    Content redirectsFolder = site.getSiteRootFolder().getChild(redirectsPath);
    if (redirectsFolder == null) {
      LOG.error("Configuration error! Missing redirects folder at {}/{}. Please create at least an empty folder.",
              site.getSiteRootFolder().getPath(), redirectsPath);
      return new SiteRedirects();
    } else {
      LOG.debug("Reading redirects from folder {}", redirectsFolder.getPath());
    }
    // Fetch the redirect content from
    Collection<Content> redirectContents = fetchRedirectDocumentsFromFolder(redirectsFolder);

    //Prefetch to get data with just one server call
    contentRepository.prefetch(redirectContents);

    List<Redirect> redirectEntries = mapToRedirects(redirectContents, site);

    // Collect and sort redirects by type
    final SiteRedirects result = new SiteRedirects(site.getId());
    redirectEntries.forEach(redirect -> handleRedirectOperation(result, redirect, true));

    LOG.debug("Finished loading [{}] static and [{}] dynamic redirects for folder [{}]",
            result.getStaticRedirects().size(), result.getPatternRedirects().size(), redirectsFolder.getPath());

    return result;
  }


  private void handleRedirectOperation(SiteRedirects result, Redirect redirect, boolean add) {

    if (redirect.getSourceUrlType() == SourceUrlType.REGEX) {
      if (add) {
        result.addPatternRedirect(redirect);
      } else {
        result.removePatternRedirect(redirect);
      }
    } else if (redirect.getSourceUrlType() == SourceUrlType.PLAIN) {
      if (add) {
        result.addStaticRedirect(redirect);
      } else {
        result.removeStaticRedirect(redirect);
      }
    } else {
      LOG.error("Unknown SourceUrlType in redirect for source {} and target {}, ignoring redirect", redirect.getSource(),
              Optional.ofNullable(redirect.getTarget()).map(Content::getId).orElse(""));
    }
  }


  // HELPER METHODS

  /**
   * Fetch all redirects in the given folder using the {@link com.coremedia.cap.content.query.QueryService}.
   */
  private @NonNull Collection<Content> fetchRedirectDocumentsFromFolder(@NonNull Content folder) {

    return Optional.ofNullable(contentRepository.getQueryService().poseContentQuery(FETCH_REDIRECTS_QUERY, folder))
            .orElse(Collections.emptyList());
  }

  /**
   * Map the given list of redirect contents to the custom redirect data type.
   */
  private @NonNull List<Redirect> mapToRedirects(@NonNull Collection<Content> redirectContents, Site site) {

    //Append the site's root segment to each redirect-url which makes life easier for the RedirectFilter
    Optional<String> rootSegment = Optional.ofNullable(site.getSiteRootDocument())
            .map(r -> "/" + r.getString("segment"))
            .map(String::toLowerCase);
    if (rootSegment.isPresent()) {
      return redirectContents.stream()
          .filter(this::validate)
          .map(c -> new Redirect(c, rootSegment.get())).collect(Collectors.toList());
    }
    LOG.error("No root segment found for site [{}]", site.getId());
    return Collections.emptyList();
  }

  private Redirect mapToRedirect(@NonNull Content redirectContent, Site site) {
    List<Redirect> result = mapToRedirects(Collections.singletonList(redirectContent), site);
    if (!result.isEmpty()) {
      return result.get(0);
    }
    return null;
  }

  private boolean validate(Content redirect) {

    String sourceUrl = redirect.getString(SOURCE_URL);
    if (!StringUtils.hasText(sourceUrl)) {
      LOG.warn("redirect [{}] has no valid sourceUrl [{}]", redirect.getId(), sourceUrl);
      return false;
    }

    Content targetLink = redirect.getLink(TARGET_LINK);

    if (targetLink == null) {
      LOG.warn("redirect [{}] has no targetLink", redirect.getId());
      return false;
    }

    return true;
  }

  /**
   * Handles a repositoryEvent, delete, update or create.
   */
  private class RedirectJob implements Runnable {

    private final Site site;
    private final JobType jobType;
    private final Content content;

    public RedirectJob(Site site, JobType jobType, Content content) {
      this.site = site;
      this.jobType = jobType;
      this.content = content;
    }

    @Override
    public void run() {

      SiteRedirects existingRedirects = redirects.get(site);

      if (existingRedirects == null) {

        Future<Boolean> initialJob = initialJobs.get(site);

        if (initialJob == null) {
          LOG.error("No redirects for site [{}] found! Cannot process event for content [{}]",
              site.getSiteRootDocument().getPath(), content.getId());
          return;
        }

        try {
          LOG.info("RedirectJob for site [{}] and type [{}] and content [{}] has to wait.",
              site.getSiteRootDocument().getPath(), jobType, content.getId());
          initialJob.get(10, TimeUnit.MINUTES);
        } catch (Exception e) {
          LOG.error("Error during waiting for initialJob to complete for site [{}]",
              site.getSiteRootDocument().getPath(), e);
          return;
        }

        LOG.debug("RedirectJob for site [{}] and type [{}] and content [{}] can now be proceed.",
            site.getSiteRootDocument().getPath(), jobType, content.getId());
        //Try again
        existingRedirects = redirects.get(site);
      }

      if (existingRedirects == null) {
        LOG.error("Error for new redirectEvent, no existing redirects for site [{}]", site.getSiteRootDocument().getPath());
        return;
      }


      Redirect model = mapToRedirect(content, site);

      if (jobType.requiredDeletion()) {
        handleRedirectOperation(existingRedirects, model, false);
      }
      if (jobType.requiredCreation()) {
        handleRedirectOperation(existingRedirects, model, true);
      }
    }
  }

  @PostConstruct
  public void postConstruct() {

    sitesService.getSites().stream()
        .forEach(s -> initialJobs.put(s, redirectRecomputeThreadPool.submit(new RedirectInitialJob(s))));
  }

  private class RedirectInitialJob implements Callable<Boolean> {

    private final Site site;

    public RedirectInitialJob(Site site) {
      this.site = site;
    }

    @Override
    public Boolean call() {
      LOG.debug("Evaluating redirect cache key for site {}", site);

      SiteRedirects result;
      try {
        result = computeInitial(site);
        redirects.put(site, result);
        return true;
      } catch (Exception e) {
        LOG.error("Error during evaluating redirects for site [{}]. This should not happen, errors are supposed to be " +
            "handled in \"computeInternal()\"", site, e);
        return false;
      }
    }
  }
}
