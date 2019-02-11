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

import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.model.SourceUrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Holder class for the redirects of a specific site.
 * Keeps maps of the paths or patterns to their redirects.
 */
public class SiteRedirects {

  private static final Logger LOG = LoggerFactory.getLogger(SiteRedirects.class);

  private String siteId;
  private ConcurrentHashMap<String, Redirect> staticRedirects = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Pattern, Redirect> patternRedirects = new ConcurrentHashMap<>();

  public SiteRedirects() {
  }

  public SiteRedirects(String siteId) {
    this.siteId = siteId;
  }

  /**
   * Returns the list of static redirects.
   */
  public Map<String, Redirect> getStaticRedirects() {
    return staticRedirects;
  }

  /**
   * Returns the list of redirects with pattern source urls.
   */
  public Map<Pattern, Redirect> getPatternRedirects() {
    return patternRedirects;
  }

  /**
   * Adds the given redirect to the cache, if it is valid.
   */
  public void addRedirect(Redirect redirect) {
    if (redirect.getSourceUrlType() == SourceUrlType.PLAIN) {
      staticRedirects.entrySet().stream()
              .filter(e -> e.getValue().getContentId().equalsIgnoreCase(redirect.getContentId()))
              .forEach(e -> staticRedirects.remove(e.getKey()));

      staticRedirects.put(redirect.getSource(), redirect);

    } else if (redirect.getSourceUrlType() == SourceUrlType.REGEX) {

      patternRedirects.entrySet().stream()
              .filter(e -> e.getValue().getContentId().equalsIgnoreCase(redirect.getContentId()))
              .forEach(e -> patternRedirects.remove(e.getKey()));
      try {
        patternRedirects.put(Pattern.compile(redirect.getSource()), redirect);
      } catch (PatternSyntaxException e) {
        LOG.error("Unable to compile pattern on redirect {}, ignoring redirect", redirect);
      }

    } else {
      LOG.error("Illegal source type {} on redirect {}, ignoring redirect", redirect.getSourceUrlType(), redirect);
    }
  }

  /**
   * Removes the given redirect from the correct list.
   */
  public void removeRedirect(Redirect redirect) {
    if (redirect.getSourceUrlType() == SourceUrlType.PLAIN) {
      staticRedirects.remove(redirect.getSource());
    } else if (redirect.getSourceUrlType() == SourceUrlType.REGEX) {
      Pattern key = null;
      for (Map.Entry<Pattern, Redirect> entry : patternRedirects.entrySet()) {
        if (entry.getValue().getContentId().equals(redirect.getContentId())) {
          key = entry.getKey();
        }
      }
      patternRedirects.remove(key);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SiteRedirects that = (SiteRedirects) o;
    return Objects.equals(staticRedirects, that.staticRedirects) &&
            Objects.equals(patternRedirects, that.patternRedirects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(staticRedirects, patternRedirects);
  }

  @Override
  public String toString() {
    return "SiteRedirects{" +
            "siteId='" + siteId + '\'' +
            ", staticRedirects.size=" + staticRedirects.size() +
            ", patternRedirects.size=" + patternRedirects.size() +
            '}';
  }
}
