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

package com.tallence.core.redirects.studio.model;

import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.repository.RedirectRepository;

import java.util.Map;
import java.util.Optional;

/**
 * This class is used by the {@link RedirectRepository}
 * to create new redirects, import redirects or edit existing redirects.
 */
public class RedirectUpdateProperties {

  public static final String ACTIVE = "active";
  public static final String SOURCE_URL_TYPE = "sourceUrlType";
  public static final String SOURCE = "source";
  public static final String TARGET_LINK = "targetLink";
  public static final String REDIRECT_TYPE = "redirectType";
  public static final String DESCRIPTION = "description";
  public static final String IMPORTED = "imported";


  private Map<String, Object> properties;

  public RedirectUpdateProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public Optional<Boolean> getActive() {
    return getProperty(ACTIVE, Boolean.class);
  }

  public Optional<SourceUrlType> getSourceUrlType() {
    Optional<String> type = getProperty(SOURCE_URL_TYPE, String.class);
    if (!type.isPresent()) {
      return Optional.empty();
    }
    SourceUrlType sourceUrlType = SourceUrlType.asSourceUrlType(type.get());
    if (sourceUrlType != null) {
      return Optional.of(sourceUrlType);
    }
    return Optional.empty();
  }

  public Optional<String> getSource() {
    return getProperty(SOURCE, String.class);
  }

  public Optional<Content> getTargetLink() {
    return getProperty(TARGET_LINK, Content.class);
  }

  public Optional<RedirectType> getRedirectType() {
    Optional<String> type = getProperty(REDIRECT_TYPE, String.class);
    if (!type.isPresent()) {
      return Optional.empty();
    }
    RedirectType redirectType = RedirectType.asRedirectType(type.get());
    if (redirectType != null) {
      return Optional.of(redirectType);
    }
    return Optional.empty();
  }

  public Optional<String> getDescription() {
    return getProperty(DESCRIPTION, String.class);
  }

  public Optional<Boolean> getImported() {
    return getProperty(IMPORTED, Boolean.class);
  }

  private <T> Optional<T> getProperty(String propertyName, Class<T> clazz) {
    if (properties.containsKey(propertyName) && clazz.isInstance(properties.get(propertyName))) {
      return Optional.of((T) properties.get(propertyName));
    }
    return Optional.empty();
  }

}