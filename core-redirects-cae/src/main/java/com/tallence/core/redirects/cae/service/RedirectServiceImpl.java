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

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for handling redirects.
 */
@Service
public class RedirectServiceImpl implements RedirectService {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectServiceImpl.class);

  private final ContentRepository contentRepository;
  private final ConcurrentMap<Site, SiteRedirects> redirectsCache;
  private final RedirectUpdateTaskScheduler redirectUpdateTaskScheduler;
  private final SitesService sitesService;

  @Autowired
  public RedirectServiceImpl(ContentRepository contentRepository, ConcurrentMap<Site, SiteRedirects> redirectsCache,
                             RedirectUpdateTaskScheduler redirectUpdateTaskScheduler,
                             SitesService sitesService) {
    this.contentRepository = contentRepository;
    this.redirectsCache = redirectsCache;
    this.redirectUpdateTaskScheduler = redirectUpdateTaskScheduler;
    this.sitesService = sitesService;
  }

  @PostConstruct
  public void init() {
    // Prewarm redirect cache to prevent longer initial requests.
    sitesService.getSites().stream().filter(Site::isReadable).forEach(this::getRedirectsForSite);

    // Attach den content listener
    contentRepository.addContentRepositoryListener(new RedirectContentListener(redirectUpdateTaskScheduler));
  }

  /**
   * Get all redirects for the given site.
   * @see RedirectService
   */
  @NonNull
  @Override
  public SiteRedirects getRedirectsForSite(@Nullable Site site) {
    // If no site was found, return an empty result.
    if (site == null) {
      return new SiteRedirects();
    }
    try {
      return getRedirectsFor(site);
    } catch (Exception e) {
      LOG.error("Error during fetching redirects for site [{}]", site.getId(), e);
      return new SiteRedirects();
    }
  }

  private SiteRedirects getRedirectsFor(Site site) {
    if (!redirectsCache.containsKey(site)) {
      // Calc site (or fetch from drive)
      LOG.debug("Missing site {} in cache, queueing fetch", site);
      redirectUpdateTaskScheduler.runUpdate(site);
      // FIXME Possible add fetch from disk here (or another speed-fix)
    }
    return redirectsCache.get(site);
  }

}
