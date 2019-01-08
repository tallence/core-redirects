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

import com.coremedia.cache.Cache;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.tallence.core.redirects.cae.service.cache.RedirectFolderCacheKey;
import com.tallence.core.redirects.cae.service.cache.RedirectFolderCacheKeyFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Service for handling redirects.
 */
@Service
public class RedirectServiceImpl implements RedirectService {


  private final RedirectFolderCacheKeyFactory redirectFolderCacheKeyFactory;
  private final Cache cache;
  private final SitesService sitesService;

  @Autowired
  public RedirectServiceImpl(RedirectFolderCacheKeyFactory redirectFolderCacheKeyFactory,
                             Cache cache,
                             SitesService sitesService) {
    this.redirectFolderCacheKeyFactory = redirectFolderCacheKeyFactory;
    this.cache = cache;
    this.sitesService = sitesService;
  }

  @PostConstruct
  public void init() {
    // Prewarm redirect cache to prevent longer initial requests.
    sitesService.getSites().stream().filter(Site::isReadable).forEach(this::getRedirectsForSite);
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
    RedirectFolderCacheKey redirectsCacheKey = redirectFolderCacheKeyFactory.getCacheKeyFor(site);
    return cache.get(redirectsCacheKey);
  }
}
