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
import com.coremedia.cap.multisite.Site;
import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.cae.service.SiteRedirects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This tasks adds a redirect to the
 */
public class UpdateDocumentTask extends AbstractTask {

  private static final Logger LOG = LoggerFactory.getLogger(UpdateDocumentTask.class);

  private Content targetDoc;
  private Site targetSite;

  public UpdateDocumentTask(Map<Site, SiteRedirects> redirectsMap, Site targetSite, Content targetDoc) {
    super(redirectsMap);
    this.targetDoc = targetDoc;
    this.targetSite = targetSite;
  }

  @Override
  public void run() {
    String rootSegment = getRootSegment(targetSite);
    if (rootSegment != null && validate(targetDoc)) {
      Redirect redirect = new Redirect(targetDoc, rootSegment);
      redirectsMap.get(targetSite).addRedirect(redirect);
      LOG.debug("Added redirect {} to site {}", redirect, targetSite);
    }
  }
}
