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

  public Boolean getActive() {
    return getProperty(ACTIVE, Boolean.class);
  }

  public SourceUrlType getSourceUrlType() {
    return SourceUrlType.asSourceUrlType(getProperty(SOURCE_URL_TYPE, String.class));
  }

  public String getSource() {
    return getProperty(SOURCE, String.class);
  }

  public Content getTargetLink() {
    return getProperty(TARGET_LINK, Content.class);
  }

  public RedirectType getRedirectType() {
    return RedirectType.asRedirectType(getProperty(REDIRECT_TYPE, String.class));
  }

  public String getDescription() {
    return getProperty(DESCRIPTION, String.class);
  }

  public Boolean getImported() {
    return getProperty(IMPORTED, Boolean.class);
  }

  @SuppressWarnings("unchecked")
  private <T> T getProperty(String propertyName, Class<T> clazz) {
    if (properties.containsKey(propertyName) && clazz.isInstance(properties.get(propertyName))) {
      return (T) properties.get(propertyName);
    }
    return null;
  }

}