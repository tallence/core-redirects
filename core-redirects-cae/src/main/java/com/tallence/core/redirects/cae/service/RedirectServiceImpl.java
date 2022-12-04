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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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

  @Value("${core.redirects.cache-dir:.}") // defaults to pwd
  private String redirectsCacheDir;

  private RedirectContentListener repoListener;

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
    sitesService.getSites().stream().filter(Site::isReadable).forEach(this::initiateRedirects);

    // Attach the content listener
    repoListener = new RedirectContentListener(redirectUpdateTaskScheduler);

    // TODO the listener needs to be registered with the latest (successfully processed) timeStamp from the deserialized file
    contentRepository.addContentRepositoryListener(repoListener);
  }

  @PreDestroy
  public void destroy() {
    contentRepository.removeContentRepositoryListener(repoListener);

    sitesService.getSites().stream().filter(Site::isReadable).forEach(this::saveRedirects);
  }

  /**
   * Get all redirects for the given site.
   *
   * @see RedirectService
   */
  @NonNull
  @Override
  public SiteRedirects getRedirectsForSite(@Nullable Site site) {
    // If no site was found, return an empty result.
    if (site == null) {
      return new SiteRedirects();
    }
    SiteRedirects redirects = redirectsCache.get(site);
    if (redirects ==  null) {
      LOG.warn("No Redirects structure exists for site [{}]. Returning empty Redirects. This is probably caused by " +
              "requests received before the initial siteUpdate jobs was finished. Request-threads should not wait for " +
              "the job to finish to prevent an overflowing request-threadPool. Consider more time for the cae to warm " +
              "up before being put back in the load-balancing.", site.getId());
      return new SiteRedirects();
    } else {
      return redirects;
    }
  }

  //TODO the latest (successfully processed) timeStamp from the ContentListener needs to be serialized
  private void saveRedirects(Site site) {
    try {
      File file = diskFile(site);
      SiteRedirects siteRedirects = getRedirectsForSite(site);
      if (!siteRedirects.isEmpty()) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {

          out.writeObject(siteRedirects);
        } catch (Exception e) {
          LOG.error("could not save state to disk {}", file);
        }
      }
    } catch (Exception e) {
      LOG.error("could not init from disk snapshot for site with {}, error:" + e, site);
    }
  }

  private void initiateRedirects(Site site) {
    try {
      // Calc site (or fetch from drive)
      SiteRedirects siteRedirects = loadFromDisk(site);
      if (siteRedirects != null) {
        redirectsCache.put(site, siteRedirects);
      } // we trigger site update either to make sure we have a consistent state "fresh from CMS"
      LOG.debug("Trigger update for site {}", site);
      redirectUpdateTaskScheduler.runUpdate(site);
    } catch (Exception e) {
      LOG.error("Error during fetching redirects for site [{}]", site.getId(), e);
    }
  }

  private SiteRedirects loadFromDisk(Site site) {
    try {
      File file = diskFile(site);
      if (file.exists() && file.canRead()) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {

          SiteRedirects siteRedirects = (SiteRedirects) in.readObject();

          siteRedirects.init(contentRepository);

          return siteRedirects;
        } catch (ObjectStreamException e) {
          LOG.error("input from file {} not readable. error:" + e, file);
        } catch (Exception e) {
          LOG.error("could not init from disk snapshot with {}, error:" + e, file);
        }
      }
    } catch (Exception e) {
      LOG.error("could not init from disk snapshot for site with {}, error:" + e, site);
    }
    return null;
  }

  private File diskFile(Site site) throws IOException {
    File dir = new File(redirectsCacheDir);
    if (!dir.exists()) {
      FileUtils.forceMkdir(dir);
    }
    return new File(dir, "SiteRedirects_" + site.getId() + ".ser");
  }
}
