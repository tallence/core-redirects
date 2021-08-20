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
package com.tallence.core.redirects.cae.service;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tallence.core.redirects.cae.service.tasks.DestroyDocumentTask;
import com.tallence.core.redirects.cae.service.tasks.RemoveDocumentTask;
import com.tallence.core.redirects.cae.service.tasks.UpdateDocumentTask;
import com.tallence.core.redirects.cae.service.tasks.UpdateSiteTask;
import com.tallence.core.redirects.cae.service.util.ControllingThreadPoolExecutorService;
import com.tallence.core.redirects.cae.service.util.PausableThreadPoolExecutorService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.*;

@Service
public class RedirectUpdateTaskScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectUpdateTaskScheduler.class);

  private final SitesService sitesService;
  private final ContentRepository contentRepository;
  private final ConcurrentMap<Site, SiteRedirects> redirectsCache;
  private final String redirectsPath;

  // A pool of single thread executors, one per site, so updates are queued per site.
  private final PausableThreadPoolExecutorService itemUpdateExecutor;
  private final ControllingThreadPoolExecutorService siteUpdateExecutor;

  // Test mode disables the multithreading here
  private boolean testMode = false;

  @Autowired
  public RedirectUpdateTaskScheduler(SitesService sitesService,
                                     ContentRepository contentRepository,
                                     @Qualifier("redirectsCache") ConcurrentMap<Site, SiteRedirects> redirectsCache,
                                     @Value("${core.redirects.path}") String redirectsPath,
                                     @Value("${core.redirects.cache.parallel.site.recompute.threads:}") Integer parallelSiteThreads,
                                     @Value("${core.redirects.cache.parallel.item.recompute.threads:4}") int parallelItemThreads) {
    this.sitesService = sitesService;
    this.contentRepository = contentRepository;
    this.redirectsCache = redirectsCache;
    this.redirectsPath = redirectsPath;
    itemUpdateExecutor = newPausableItemUpdateExecutor(parallelItemThreads);
    siteUpdateExecutor = newControllingThreadPoolExecutorService(sitesService, parallelSiteThreads, itemUpdateExecutor);
  }

  /**
   * Update the redirect cache with the new/changed redirect.
   */
  public void runUpdate(Content redirect) {
    Site site = getSite(redirect);
    if (site != null) {
      if (redirectsCache.containsKey(site)) {
        if (testMode) {
          new UpdateDocumentTask(redirectsCache, site, redirect).run();
        } else {
          itemUpdateExecutor.submit(new UpdateDocumentTask(redirectsCache, site, redirect));
        }
      } else {
        // If the site of this redirect is not in the cache yet, we have to build an index for it
        submitSiteUpdate(site);
      }
    }
  }

  /**
   * Update the redirect cache with the new/changed site.
   */
  public void runUpdate(Site site) {
    if (site != null) {
      // Before a site update, we can cancel all running jobs, as they will be overwritten anyway
      submitSiteUpdate(site);
    }
  }

  private void submitSiteUpdate(Site site) {
    itemUpdateExecutor.pause();
    LOG.info("Re-indexing site {}, paused item update queue", site);
    if (testMode) {
      new UpdateSiteTask(redirectsCache, contentRepository, redirectsPath, site, itemUpdateExecutor).run();
    } else {
      siteUpdateExecutor.submit(new UpdateSiteTask(redirectsCache, contentRepository, redirectsPath, site, itemUpdateExecutor));
    }
  }

  /**
   * Update the redirect cache and remove the given redirect.
   */
  public void runRemove(Content redirect) {
    Site site = getSiteOfDeletedContent(redirect);
    if (site != null && redirectsCache.containsKey(site)) {
      if (testMode) {
        new RemoveDocumentTask(redirectsCache, site, redirect).run();
      } else {
        itemUpdateExecutor.submit(new RemoveDocumentTask(redirectsCache, site, redirect));
      }
    }
  }

  /**
   * Destroys redirects. Only the id is available for destroyed contents...
   */
  public void runDestroy(String redirectId, Content folder) {

    Site site = getSite(folder);
    if (site != null && redirectsCache.containsKey(site)) {
      if (testMode) {
        new DestroyDocumentTask(redirectsCache, site, redirectId).run();
      } else {
        itemUpdateExecutor.submit(new DestroyDocumentTask(redirectsCache, site, redirectId));
      }
    }
  }


  // HELPER METHODS

  private PausableThreadPoolExecutorService newPausableItemUpdateExecutor(int maxThreadCount) {
    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("redirect-item-updates-%d").build();
    return new PausableThreadPoolExecutorService(2, maxThreadCount, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), namedThreadFactory);
  }

  private ControllingThreadPoolExecutorService newControllingThreadPoolExecutorService(SitesService sitesService,
                                                                                       @Nullable Integer parallelSiteThreads,
                                                                                       PausableThreadPoolExecutorService pausableThreadPoolExecutorService) {
    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("redirect-site-updates-%d").build();

    //If the number of threads has not been configured: try to run all threads at once, to fill the cache as fast as
    // possible. A SynchronousQueue wil try to pass the tasks to the pool, which can take as many threads as the number
    // of all sites.
    if (parallelSiteThreads == null) {
      int maximumPoolSize = sitesService.getSites().isEmpty() ? 1 : sitesService.getSites().size();
      return new ControllingThreadPoolExecutorService(1, maximumPoolSize, 1L,
              TimeUnit.SECONDS, new SynchronousQueue<>(),
              namedThreadFactory, pausableThreadPoolExecutorService);
    } else {
      //If the number of threads has been configured: use it as the core pool size and as the number of max threads,
      //combined with a LinkedBlockingDeque (which is unbounded). The configured number of threads are availble (core pool)
      // and potential new threads will be parked in the queue
      return new ControllingThreadPoolExecutorService(parallelSiteThreads, parallelSiteThreads, 1L,
              TimeUnit.SECONDS, new LinkedBlockingDeque<>(),
              namedThreadFactory, pausableThreadPoolExecutorService);
    }

  }

  private Site getSite(Content content) {
    Site site = sitesService.getContentSiteAspect(content).getSite();
    if (site == null) {
      // Nothing we can do here...
      LOG.error("No site found for document {}. Is the SiteMarker already published?", content);
    }
    return site;
  }

  private Site getSiteOfDeletedContent(Content deleted) {
    Optional<Site> foundSite = sitesService.getSites()
            .stream()
            .filter(site -> deleted.getLastPath().startsWith(site.getSiteRootFolder().getPath()))
            .findFirst();
    return foundSite.orElseGet(() -> {
      LOG.error("No site found (or found in cache) for deleted document {}.", deleted);
      return null;
    });
  }

  void setTestMode(boolean testMode) {
    this.testMode = testMode;
  }
}
