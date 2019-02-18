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

import com.coremedia.cap.multisite.Site;
import com.tallence.core.redirects.cae.service.SiteRedirects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Removes destroyed Redirects from the cache.
 */
public class DestroyDocumentTask extends AbstractTask {

  private static final Logger LOG = LoggerFactory.getLogger(DestroyDocumentTask.class);

  private String targetDocId;

  public DestroyDocumentTask(Map<Site, SiteRedirects> redirectsMap, String targetDocId) {
    super(redirectsMap);
    this.targetDocId = targetDocId;
  }

  @Override
  public void run() {

    redirectsMap.values().forEach(siteRedirects -> {
      siteRedirects.removeRedirect(targetDocId);
      LOG.info("Removed {} from redirect cache of site {}", targetDocId);
    });
  }

}
