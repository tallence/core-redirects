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

import com.tallence.core.redirects.cae.filter.RedirectFilter;
import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.model.SourceUrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Holder class for the redirects of a specific site.
 * Keeps maps of the paths or patterns to their redirects.
 */
public class SiteRedirects {

  private static final Logger LOG = LoggerFactory.getLogger(SiteRedirects.class);

  private String siteId;
  private final ConcurrentHashMap<String, List<Redirect>> plainRedirects = new ConcurrentHashMap<>();
  private final Object plainRedirectsMonitor = new Object();
  private final ConcurrentHashMap<Pattern, List<Redirect>> patternRedirects = new ConcurrentHashMap<>();
  private final Object patternRedirectsMonitor = new Object();

  public SiteRedirects() {
  }

  public SiteRedirects(String siteId) {
    this.siteId = siteId;
  }

  /**
   * Returns the list of plain redirects.
   */
  public Map<String, List<Redirect>> getPlainRedirects() {
    return plainRedirects;
  }

  /**
   * Returns the list of redirects with pattern source urls.
   */
  public Map<Pattern, List<Redirect>> getPatternRedirects() {
    return patternRedirects;
  }

  /**
   * Adds the given redirect to the cache, if it is valid.
   *
   * The url will be decoded because {@link javax.servlet.http.HttpServletRequest#getPathInfo} will
   * return a decoded pathInfo too. The decoding must not handle params, schemes, ports etc. because the lookup
   * in the {@link RedirectFilter} matches the source with the Request-PathInfo only
   */
  public void addRedirect(Redirect redirect) {
    if (redirect.getSourceUrlType() == SourceUrlType.PLAIN) {
      synchronized (plainRedirectsMonitor) {

        plainRedirects.values().forEach(redirects -> redirects.remove(redirect));

        final String key = URLDecoder.decode(redirect.getSource(), UTF_8);
        plainRedirects.putIfAbsent(key, new ArrayList<>());
        plainRedirects.get(key).add(redirect);
      }

    } else if (redirect.getSourceUrlType() == SourceUrlType.REGEX) {
      try {
        synchronized (patternRedirectsMonitor) {

          patternRedirects.values().forEach(redirects -> redirects.remove(redirect));

          final Pattern key = Pattern.compile(redirect.getSource());
          patternRedirects.putIfAbsent(key, new ArrayList<>());
          patternRedirects.get(key).add(redirect);
        }
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

    // Removes the whole map entry afterwards, if the list is empty as a result of the operation.
    if (redirect.getSourceUrlType() == SourceUrlType.PLAIN) {
      synchronized (plainRedirectsMonitor) {
        plainRedirects.entrySet().stream()
                .filter(e -> e.getValue().remove(redirect))
                .filter(e -> e.getValue().isEmpty())
                .forEach(e -> plainRedirects.remove(e.getKey()));
      }
    } else if (redirect.getSourceUrlType() == SourceUrlType.REGEX) {
      synchronized (patternRedirectsMonitor) {
        patternRedirects.entrySet().stream()
                .filter(e -> e.getValue().remove(redirect))
                .filter(e -> e.getValue().isEmpty())
                .forEach(e -> patternRedirects.remove(e.getKey()));
      }
    }
  }

  /**
   * Remove the redirect, identified by the given id.
   * The type is not known -> try both lists.
   */
  public void removeRedirect(String id) {

    synchronized (plainRedirectsMonitor) {
      plainRedirects.entrySet().stream()
          .filter(e -> e.getValue().removeIf(r -> id.equals(r.getContentId())))
          .filter(e -> e.getValue().isEmpty())
          .forEach(e -> plainRedirects.remove(e.getKey()));
    }

    synchronized (patternRedirectsMonitor) {
      patternRedirects.entrySet().stream()
              .filter(e -> e.getValue().removeIf(r -> id.equals(r.getContentId())))
              .filter(e -> e.getValue().isEmpty())
              .forEach(e -> patternRedirects.remove(e.getKey()));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SiteRedirects that = (SiteRedirects) o;
    return Objects.equals(plainRedirects, that.plainRedirects) &&
            Objects.equals(patternRedirects, that.patternRedirects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(plainRedirects, patternRedirects);
  }

  @Override
  public String toString() {
    return "SiteRedirects{" +
            "siteId='" + siteId + '\'' +
            ", plainRedirects.size=" + plainRedirects.size() +
            ", patternRedirects.size=" + patternRedirects.size() +
            '}';
  }
}
