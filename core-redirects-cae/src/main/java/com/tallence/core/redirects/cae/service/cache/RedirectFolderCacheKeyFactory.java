package com.tallence.core.redirects.cae.service.cache;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.query.QueryService;
import com.coremedia.cap.multisite.Site;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * This factory is used to create configured instances of the {@link RedirectFolderCacheKey}.
 */
@Service
public class RedirectFolderCacheKeyFactory {

  private final QueryService queryService;
  private final ExecutorService redirectCacheKeyRecomputeThreadPool;
  private final String redirectsPath;
  private boolean testmode = false;

  @Autowired
  public RedirectFolderCacheKeyFactory(ContentRepository contentRepository,
                                       @Qualifier("getRedirectCacheKeyRecomputeThreadPool")
                                       ExecutorService redirectCacheKeyRecomputeThreadPool,
                                       @Value("${core.redirects.path}") String redirectsPath) {
    this.queryService = contentRepository.getQueryService();
    this.redirectCacheKeyRecomputeThreadPool = redirectCacheKeyRecomputeThreadPool;
    this.redirectsPath = redirectsPath;
  }

  public RedirectFolderCacheKey getCacheKeyFor(@NonNull Site site) {
    return new RedirectFolderCacheKey(queryService, redirectCacheKeyRecomputeThreadPool, redirectsPath, site, testmode);
  }

  // Enable testMode on cache keys
  public void setTestmode(boolean testmode) {
    this.testmode = testmode;
  }
}
