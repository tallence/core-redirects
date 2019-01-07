package com.tallence.core.redirects.cae.service.cache;

import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.multisite.Site;
import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.cae.service.SiteRedirects;
import com.tallence.core.redirects.model.SourceUrlType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * CacheKey for RedirectEntries in a folder.
 * <p>
 * The cache is configured, so that it behaves as follows:
 * <ul>
 * <li>It will always stay in cache</li>
 * <li>It will recompute immediately on invalidation</li>
 * <li>It will return the old value during recomputation and not wait</li>
 * </ul>
 * Do not instanciate directly, but use the {@link RedirectFolderCacheKeyFactory} instead.
 *
 * {@code testcode} sets a cachekey testmode with recomputeOnInvalidation = false and cacheClass = dummy
 */
public class RedirectFolderCacheKey extends CacheKey<SiteRedirects> {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectFolderCacheKey.class);

  private static final long CACHE_DURATION_ON_FOLDER_NOT_FOUND = 4;

  // This query fetches all redirects below a specific folder
  private static final String FETCH_REDIRECTS_QUERY = "TYPE " + Redirect.NAME + ": isInProduction AND BELOW ?0";

  private final QueryService queryService;
  private final ExecutorService redirectCacheKeyRecomputeThreadPool;
  private final String redirectsPath;
  private final Site site;
  private final boolean testmode;

  RedirectFolderCacheKey(QueryService queryService,
                         ExecutorService redirectCacheKeyRecomputeThreadPool,
                         String redirectsPath,
                         Site site,
                         boolean testmode) {
    this.queryService = queryService;
    this.redirectCacheKeyRecomputeThreadPool = redirectCacheKeyRecomputeThreadPool;
    this.redirectsPath = redirectsPath;
    this.site = site;
    this.testmode = testmode;
  }

  @Override
  public SiteRedirects evaluate(Cache cache) {
    LOG.debug("Evaluating redirect cache key for site {}", site);

    // Disable dependency tracking for the folder resolution, as we add the necessary dependencies ourselve and we don't
    // want any folder deps.
    Cache.disableDependencies();
    Content redirectsFolder = site.getSiteRootFolder().getChild(redirectsPath);
    if (redirectsFolder == null) {
      // In order to prevent the folder lookup from running on every request, we cache error results for a time.
      LOG.error("Configuration error! Missing redirects folder at {}/{}. Please create at least an empty folder. Caching for {} hours",
              site.getSiteRootFolder().getPath(), redirectsPath, CACHE_DURATION_ON_FOLDER_NOT_FOUND);
      Cache.enableDependencies();
      Cache.cacheFor(CACHE_DURATION_ON_FOLDER_NOT_FOUND, TimeUnit.HOURS);
      return new SiteRedirects();
    } else {
      LOG.debug("Reading redirects from folder {}", redirectsFolder.getPath());
    }
    // Fetch the redirect content from
    Collection<Content> redirectContents = fetchRedirectDocumentsFromFolder(redirectsFolder);
    // Re-enable dependencies
    Cache.enableDependencies();

    // In order to create dependencies on the redirects found, the conversion needs to happen after re-enabling the tracking.
    List<Redirect> redirectEntries = mapToRedirects(redirectContents);

    // Also add dependency on the children of the config folder, so that this cache key gets invalidated if rules are
    // added or removed.
    Cache.dependencyOn("children:" + IdHelper.parseContentId(redirectsFolder.getId()));

    // Collect and sort redirects by type
    final SiteRedirects result = new SiteRedirects(site.getId());
    redirectEntries.forEach(redirect -> {
      if (redirect.getSourceUrlType() == SourceUrlType.REGEX) {
        result.addPatternRedirect(redirect);
      } else if (redirect.getSourceUrlType() == SourceUrlType.ABSOLUTE) {
        result.addStaticRedirect(redirect);
      } else {
        LOG.error("Unknown SourceUrlType in redirect {}, ignoring redirect", redirect);
      }
    });

    return result;
  }


  // HELPER METHODS

  /**
   * Fetch all redirects in the given folder using the {@link com.coremedia.cap.content.query.QueryService}.
   */
  private @NonNull Collection<Content> fetchRedirectDocumentsFromFolder(@NonNull Content folder) {
    return Optional.ofNullable(queryService.poseContentQuery(FETCH_REDIRECTS_QUERY, folder)).orElse(Collections.emptyList());
  }

  /**
   * Map the given list of redirect contents to the custom redirect data type.
   */
  private @NonNull List<Redirect> mapToRedirects(@NonNull Collection<Content> redirectContents) {
    return redirectContents.stream().map(Redirect::new).collect(Collectors.toList());
  }


  // NEEDED FOR CACHE HANDLING AND CONFIGURATION

  @Override
  public String cacheClass(Cache cache, SiteRedirects value) {
    return testmode ? CACHE_CLASS_DEFAULT : CACHE_CLASS_ALWAYS_STAY_IN_CACHE;
  }

  @Override
  public Executor executorForRecomputation(Cache cache, SiteRedirects value) {
    return redirectCacheKeyRecomputeThreadPool;
  }

  @Override
  public boolean recomputeOnInvalidation(Cache cache, SiteRedirects value, int numDependents) {
    return !testmode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RedirectFolderCacheKey)) {
      return false;
    }
    RedirectFolderCacheKey that = (RedirectFolderCacheKey) o;
    return Objects.equals(site, that.site);
  }

  @Override
  public int hashCode() {
    return Objects.hash(site);
  }
}
