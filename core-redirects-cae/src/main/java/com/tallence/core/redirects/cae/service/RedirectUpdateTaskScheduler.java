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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
                                     @Value("${core.redirects.cache.parallel.site.recompute.threads:4}") int parallelSiteThreads,
                                     @Value("${core.redirects.cache.parallel.item.recompute.threads:4}") int parallelItemThreads) {
    this.sitesService = sitesService;
    this.contentRepository = contentRepository;
    this.redirectsCache = redirectsCache;
    this.redirectsPath = redirectsPath;
    itemUpdateExecutor = newPausableItemUpdateExecutor(parallelItemThreads);
    siteUpdateExecutor = newControllingThreadPoolExecutorService(parallelSiteThreads, itemUpdateExecutor);
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
    if (site != null) {
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
    if (site != null) {
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
    return new PausableThreadPoolExecutorService(1, maxThreadCount, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), namedThreadFactory);
  }

  private ControllingThreadPoolExecutorService newControllingThreadPoolExecutorService(int maxThreadCount, PausableThreadPoolExecutorService pausableThreadPoolExecutorService) {
    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("redirect-site-updates-%d").build();
    return new ControllingThreadPoolExecutorService(1, maxThreadCount, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), namedThreadFactory, pausableThreadPoolExecutorService);
  }

  private Site getSite(Content content) {
    Site site = sitesService.getContentSiteAspect(content).getSite();
    if (site == null || !redirectsCache.containsKey(site)) {
      // Nothing we can do here...
      LOG.error("No site found (or found in cache) for document {}. This is probably a serious error.", content);
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
