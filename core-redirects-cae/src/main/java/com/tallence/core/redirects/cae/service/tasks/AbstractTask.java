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
import com.tallence.core.redirects.cae.service.SiteRedirects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

import static com.tallence.core.redirects.cae.model.Redirect.*;

/**
 * Common methods for redirect cache update tasks.
 */
abstract class AbstractTask implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractTask.class);

  final Map<Site, SiteRedirects> redirectsMap;

  AbstractTask(Map<Site, SiteRedirects> redirectsMap) {
    this.redirectsMap = redirectsMap;
  }

  String getRootSegment(Site site) {
    return Optional.ofNullable(site.getSiteRootDocument())
            .map(r -> "/" + r.getString("segment"))
            .map(String::toLowerCase)
            .orElse(null);
  }

  boolean validate(Content redirect) {

    if (redirect.isDeleted() || redirect.isDestroyed()) {
      LOG.warn("Redirect [{}] was destroyed or deleted in the meantime. Ignore it.", redirect.getId());
      return false;
    }

    String sourceUrl = redirect.getString(SOURCE_URL);
    if (!StringUtils.hasText(sourceUrl)) {
      LOG.warn("redirect [{}] has no valid sourceUrl [{}]", redirect.getId(), sourceUrl);
      return false;
    }

    Content targetLink = redirect.getLink(TARGET_LINK);
    String targetUrl = redirect.getString(TARGET_URL);

    if (targetLink == null && StringUtils.isEmpty(targetUrl)) {
      LOG.warn("redirect [{}] has no targetLink or targetUrl", redirect.getId());
      return false;
    }

    return true;
  }
}
