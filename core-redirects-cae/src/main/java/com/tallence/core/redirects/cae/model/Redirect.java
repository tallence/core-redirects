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
package com.tallence.core.redirects.cae.model;

import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;

import java.util.Objects;

/**
 * Model for a Redirect (used instead of a ContentBean in order to keep the overhead low).
 * Keeps only the properties required by the CAE.
 */
public class Redirect {

  public static final String NAME = "Redirect";
  public static final String TARGET_LINK = "targetLink";
  public static final String SOURCE_URL = "source";

  private static final String SOURCE_URL_TYPE = "sourceUrlType";
  private static final String REDIRECT_TYPE = "redirectType";

  private final String contentId;
  private final SourceUrlType sourceUrlType;
  private final String source;
  private final RedirectType redirectType;
  private final Content target;

  public Redirect(Content redirect, String rootSegment) {
    contentId = redirect.getId();
    sourceUrlType = SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE));
    source = rootSegment + redirect.getString(SOURCE_URL);
    redirectType = RedirectType.asRedirectType(redirect.getString(REDIRECT_TYPE));
    target = redirect.getLink(TARGET_LINK);
  }

  /**
   * Returns the content id of the {@link Content} (of type Redirect) backing this model.
   */
  public String getContentId() {
    return contentId;
  }

  /**
   * Returns the {@link SourceUrlType} of the redirect.
   */
  public SourceUrlType getSourceUrlType() {
    return sourceUrlType;
  }

  /**
   * Returns the source url of the redirect.
   */
  public String getSource() {
    return source;
  }

  /**
   * Returns a {@link Content} to which the redirect links.
   */
  public Content getTarget() {
    return target;
  }

  /**
   * Returns the {@link RedirectType} of the redirect.
   */
  public RedirectType getRedirectType() {
    return redirectType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Redirect redirect = (Redirect) o;
    return contentId.equals(redirect.contentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contentId);
  }

  @Override
  public String toString() {
    return "Redirect{" +
            "contentId='" + contentId + '\'' +
            '}';
  }
}
