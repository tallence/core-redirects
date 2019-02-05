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
import com.tallence.core.redirects.cae.service.tasks.RemoveDocumentTask;
import com.tallence.core.redirects.cae.service.tasks.UpdateDocumentTask;
import com.tallence.core.redirects.cae.service.tasks.UpdateSiteTask;
import com.tallence.core.redirects.cae.service.util.PausableThreadPoolExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class RedirectUpdateTaskScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectUpdateTaskScheduler.class);

  private final SitesService sitesService;
  private final ContentRepository contentRepository;
  private final ConcurrentMap<Site, SiteRedirects> redirectsCache;
  private final String redirectsPath;

  // A pool of single thread executors, one per site, so updates are queued per site.
  private final Map<Site, PausableThreadPoolExecutorService> executors = new HashMap<>();
  private final ExecutorService siteUpdateExecutor;

  @Autowired
  public RedirectUpdateTaskScheduler(SitesService sitesService,
                                     ContentRepository contentRepository,
                                     @Qualifier("redirectsCache") ConcurrentMap<Site, SiteRedirects> redirectsCache,
                                     @Value("${core.redirects.path}") String redirectsPath,
                                     @Value("${core.redirects.cache.parallel.site.recompute.threads:4}") int parallelSiteThreads) {
    this.sitesService = sitesService;
    this.contentRepository = contentRepository;
    this.redirectsCache = redirectsCache;
    this.redirectsPath = redirectsPath;
    siteUpdateExecutor = Executors.newFixedThreadPool(parallelSiteThreads);
  }

  /**
   * Update the redirect cache with the new/changed redirect.
   */
  public void runUpdate(Content redirect) {
    Site site = getSite(redirect);
    if (site != null) {
      PausableThreadPoolExecutorService executorService = executors.computeIfAbsent(site, o -> newSingleThreadExecutor());
      if (redirectsCache.containsKey(site)) {
        executorService.submit(new UpdateDocumentTask(redirectsCache, site, redirect));
      } else {
        // If the site of this redirect is not in the cache yet, we have to build an index for it
        submitSiteUpdate(site, executorService);
      }
    }
  }

  /**
   * Update the redirect cache with the new/changed site.
   */
  public void runUpdate(Site site) {
    if (site != null) {
      PausableThreadPoolExecutorService executorService = executors.computeIfAbsent(site, o -> newSingleThreadExecutor());
      // Before a site update, we can cancel all running jobs, as they will be overwritten anyway
      submitSiteUpdate(site, executorService);
    }
  }

  private void submitSiteUpdate(Site site, PausableThreadPoolExecutorService executorService) {
    List<Runnable> abortedTasks = new ArrayList<>();
    executorService.getQueue().drainTo(abortedTasks);
    executorService.pause();
    LOG.info("Re-indexing site {}, canceled {} pending tasks, paused queue", site, abortedTasks.size());
    siteUpdateExecutor.submit(new UpdateSiteTask(redirectsCache, contentRepository, redirectsPath, site, executorService));
  }

  /**
   * Update the redirect cache and remove the given redirect.
   */
  public void runRemove(Content redirect) {
    Site site = getSiteOfDeletedContent(redirect);
    if (site != null) {
      ExecutorService executorService = executors.computeIfAbsent(site, o -> newSingleThreadExecutor());
      executorService.submit(new RemoveDocumentTask(redirectsCache, site, redirect));
    }
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

  private PausableThreadPoolExecutorService newSingleThreadExecutor() {
    // Copy from Executors.newSingleThreadExecutor() without the wrapping, so we can get to the queue
    return new PausableThreadPoolExecutorService(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
  }
}
