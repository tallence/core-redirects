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
package com.tallence.core.redirects.cae.service.tasks;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.cae.service.SiteRedirects;
import com.tallence.core.redirects.cae.service.util.PausableThreadPoolExecutorService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpdateSiteTask extends AbstractTask {

  private static final Logger LOG = LoggerFactory.getLogger(UpdateSiteTask.class);

  // This query fetches all redirects below a specific folder
  private static final String FETCH_REDIRECTS_QUERY = "TYPE " + Redirect.NAME + ": isInProduction AND BELOW ?0";

  private final ContentRepository contentRepository;
  private final String redirectsPath;
  private final Site site;
  private final PausableThreadPoolExecutorService executorService;

  public UpdateSiteTask(Map<Site, SiteRedirects> redirectsMap, ContentRepository contentRepository, String redirectsPath, Site targetSite, PausableThreadPoolExecutorService executorService) {
    super(redirectsMap);
    this.contentRepository = contentRepository;
    this.redirectsPath = redirectsPath;
    this.site = targetSite;
    this.executorService = executorService;
  }

  @Override
  public void run() {
    // Disable dependency tracking for the folder resolution, as we add the necessary dependencies ourselve and we don't
    // want any folder deps.
    Content redirectsFolder = site.getSiteRootFolder().getChild(redirectsPath);
    if (redirectsFolder == null) {
      // In order to prevent the folder lookup from running on every request, we cache error results for a time.
      LOG.error("Configuration error! Missing redirects folder at {}/{}. Please create at least an empty folder.", site.getSiteRootFolder().getPath(), redirectsPath);
      redirectsMap.put(site, new SiteRedirects(site.getId()));
      return;
    } else {
      LOG.debug("Reading redirects from folder {}", redirectsFolder.getPath());
    }
    // Fetch the redirect content from
    Collection<Content> redirectContents = fetchRedirectDocumentsFromFolder(redirectsFolder);

    //Prefetch to get data with just one server call
    contentRepository.prefetch(redirectContents);

    // In order to create dependencies on the redirects found, the conversion needs to happen after re-enabling the tracking.
    List<Redirect> redirectEntries = mapToRedirects(redirectContents, site);

    // Add redirects to model
    final SiteRedirects result = new SiteRedirects(site.getId());
    redirectEntries.forEach(result::addRedirect);

    LOG.debug("Finished loading [{}] static and [{}] dynamic redirects for folder [{}]",
            result.getStaticRedirects().size(), result.getPatternRedirects().size(), redirectsFolder.getPath());

    redirectsMap.put(site, result);

    // Unpause the regular updates
    executorService.resume();
  }

  /**
   * Fetch all redirects in the given folder using the {@link com.coremedia.cap.content.query.QueryService}.
   */
  @NonNull
  private Collection<Content> fetchRedirectDocumentsFromFolder(@NonNull Content folder) {
    return Optional.ofNullable(contentRepository.getQueryService().poseContentQuery(FETCH_REDIRECTS_QUERY, folder))
            .orElse(Collections.emptyList());
  }

  /**
   * Map the given list of redirect contents to the custom redirect data type.
   */
  @NonNull
  private List<Redirect> mapToRedirects(@NonNull Collection<Content> redirectContents, Site site) {
    //Append the site's root segment to each redirect-url which makes life easier for the RedirectFilter
    Optional<String> rootSegment = Optional.ofNullable(site.getSiteRootDocument())
            .map(r -> "/" + r.getString("segment"))
            .map(String::toLowerCase);

    if (rootSegment.isPresent()) {
      return redirectContents
              .stream()
              .filter(this::validate)
              .map(c -> new Redirect(c, rootSegment.get()))
              .collect(Collectors.toList());
    }
    LOG.error("No root segment found for site [{}]", site.getId());
    return Collections.emptyList();
  }

}
