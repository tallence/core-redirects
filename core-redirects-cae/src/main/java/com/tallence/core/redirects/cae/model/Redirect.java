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

  private final SourceUrlType sourceUrlType;
  private final String source;
  private final RedirectType redirectType;
  private final Content target;
  private final String id;

  public Redirect(Content redirect, String rootSegment) {
    this.sourceUrlType = SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE));
    this.id = redirect.getId();
    this.source = rootSegment + redirect.getString(SOURCE_URL);
    this.redirectType = RedirectType.asRedirectType(redirect.getString(REDIRECT_TYPE));
    this.target = redirect.getLink(TARGET_LINK);
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

  public String getId() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Redirect redirect = (Redirect) o;
    return sourceUrlType == redirect.sourceUrlType &&
            Objects.equals(source, redirect.source) &&
            redirectType == redirect.redirectType &&
            Objects.equals(target, redirect.target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceUrlType, source, redirectType, target);
  }
}
