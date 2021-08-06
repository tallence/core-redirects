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
import com.tallence.core.redirects.model.RedirectParameter;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectTargetParameter;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.repository.RedirectRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * This class is used to create new redirects, import redirects or edit existing redirects.
 * It's offering typed getters to make the access to untyped properties (in the properties-map) more convenient.
 *
 * It also offers a validation-logic, which is placed here because of the access to the typed getters and the raw
 * properties map.
 */
public class RedirectUpdateProperties {

  public static final String ACTIVE = "active";
  public static final String SOURCE_URL_TYPE = "sourceUrlType";
  public static final String SOURCE = "source";
  public static final String TARGET_LINK = "targetLink";
  public static final String TARGET_URL = "targetUrl";
  public static final String REDIRECT_TYPE = "redirectType";
  public static final String DESCRIPTION = "description";
  public static final String IMPORTED = "imported";
  public static final String SOURCE_PARAMETERS = "sourceParameters";
  public static final String TARGET_PARAMETERS = "targetParameters";

  static final String INVALID_ACTIVE_VALUE = "active_invalid";
  static final String INVALID_SOURCE_URL_TYPE_VALUE = "sourceUrlType_invalid";
  static final String INVALID_SOURCE_VALUE = "source_invalid";
  static final String INVALID_SOURCE_WHITESPACE = "source_whitespace";
  static final String SOURCE_ALREADY_EXISTS = "source_already_exists";
  static final String INVALID_REDIRECT_TYPE_VALUE = "redirectType_invalid";
  static final String INVALID_DESCRIPTION_VALUE = "description_invalid";
  static final String MISSING_TARGET_LINK = "target_missing";
  static final String BOTH_TARGET = "target_both";
  static final String INVALID_TARGET_LINK = "target_invalid";
  static final String INVALID_TARGET_URL = "target_url_invalid";


  private final Map<String, Object> properties;
  private final RedirectRepository repository;
  private final String siteId;
  private final String redirectId;

  public RedirectUpdateProperties(Map<String, Object> properties, RedirectRepository repository, String siteId, String redirectId) {
    this.properties = properties;
    this.repository = repository;
    this.siteId = siteId;
    this.redirectId = redirectId;
  }

  public Boolean getActive() {
    return getProperty(ACTIVE, Boolean.class);
  }

  public SourceUrlType getSourceUrlType() {
    return SourceUrlType.asSourceUrlType(getProperty(SOURCE_URL_TYPE, String.class));
  }

  public String getSource() {
    return Optional.ofNullable(getProperty(SOURCE, String.class))
            .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
            .map(String::trim)
            .orElse(null);
  }

  public Content getTargetLink() {
    return getProperty(TARGET_LINK, Content.class);
  }

  public String getTargetUrl() {
    return Optional.ofNullable(getProperty(TARGET_URL, String.class))
            .map(String::trim).orElse(null);
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

  public boolean urlParametersChanged() {
    return sourceParametersChanged() || targetParametersChanged();
  }

  public boolean targetParametersChanged() {
    return properties.containsKey(TARGET_PARAMETERS);
  }

  public boolean sourceParametersChanged() {
    return properties.containsKey(SOURCE_PARAMETERS);
  }

  public List<RedirectSourceParameter> getSourceParameters() {
    List<RedirectSourceParameter> sourceParameters = new ArrayList<>();
    List<?> list = getListProperty(SOURCE_PARAMETERS);

    for (Object item : list) {
      if (item instanceof RedirectSourceParameter) {
        sourceParameters.add((RedirectSourceParameter) item);
      } else if (item instanceof Map) {
        Map<String, String> properties = (Map<String, String>) item;
        sourceParameters.add(new RedirectSourceParameter(
                properties.get(RedirectParameter.STRUCT_PROPERTY_PARAMS_NAME),
                properties.get(RedirectParameter.STRUCT_PROPERTY_PARAMS_VALUE),
                RedirectSourceParameter.Operator.valueOf(properties.get(RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS_OPERATOR))
        ));
      }
    }

    return sourceParameters;
  }

  public List<RedirectTargetParameter> getTargetParameters() {
    List<RedirectTargetParameter> targetParameters = new ArrayList<>();
    List<?> list = getListProperty(TARGET_PARAMETERS);

    for (Object item : list) {
      if (item instanceof RedirectSourceParameter) {
        targetParameters.add((RedirectTargetParameter) item);
      } else if (item instanceof Map) {
        Map<String, String> properties = (Map<String, String>) item;
        targetParameters.add(new RedirectTargetParameter(
                properties.get(RedirectParameter.STRUCT_PROPERTY_PARAMS_NAME),
                properties.get(RedirectParameter.STRUCT_PROPERTY_PARAMS_VALUE))
        );
      }
    }

    return targetParameters;
  }

  @SuppressWarnings("unchecked")
  private <T> T getProperty(String propertyName, Class<T> clazz) {
    if (properties.containsKey(propertyName) && clazz.isInstance(properties.get(propertyName))) {
      return (T) properties.get(propertyName);
    }
    return null;
  }

  private List<?> getListProperty(String propertyName) {
    return Optional.ofNullable(properties.get(propertyName))
            .filter(List.class::isInstance)
            .map(List.class::cast)
            .orElse(List.of());
  }

  /**
   * @see #validate(boolean)
   */
  public Map<String, String> validate() {
    return validate(false);
  }

  /**
   * Validating the properties data.
   * @param update true means, that the properties are used for updating existing data. Which does not require mandatory
   *               properties such as "active" or "targetId" which are not available if they are not changed by the update-request.
   * @return a list of errors mapped to the corresponding field-name
   */
  public Map<String, String> validate(boolean update) {

    Map<String, String> errors = new HashMap<>();

    //If the properties are used to create a new redirect, active is required
    if (!update && getActive() == null) {
      errors.put(ACTIVE, INVALID_ACTIVE_VALUE);
    } else if (getActive() == null && properties.get(ACTIVE) != null) {
      //if the property contains a value, but it does not match "true" or "false". Might occur in the csv-upload.
      errors.put(ACTIVE, INVALID_ACTIVE_VALUE);
    }

    //If the properties are used to create a new redirect, SourceUrlType is required
    if (!update && getSourceUrlType() == null) {
      errors.put(SOURCE_URL_TYPE, INVALID_SOURCE_URL_TYPE_VALUE);
    } else if (getSourceUrlType() == null && properties.get(SOURCE_URL_TYPE) != null) {
      //if the property contains a value, but it does not match any SourceUrlType-enum-value. Might occur in the csv-upload.
      errors.put(SOURCE_URL_TYPE, INVALID_SOURCE_URL_TYPE_VALUE);
    }

    String source = getSource();
    //If the properties are used to create a new redirect, source is required
    if (!update && isEmpty(source)) {
      errors.put(SOURCE, INVALID_SOURCE_VALUE);
    }
    if (isNotEmpty(source)) {
      if (!sourceIsValid(source)) {
        errors.put(SOURCE, INVALID_SOURCE_VALUE);
      } else if (sourceHasWhitespaces(source)) {
        errors.put(SOURCE, INVALID_SOURCE_WHITESPACE);
      } else if (isNotBlank(redirectId) && repository.sourceAlreadyExists(siteId, redirectId, source, getSourceParameters()) ||
              isBlank(redirectId) && repository.sourceAlreadyExists(siteId, source, getSourceParameters())) {
        errors.put(SOURCE, SOURCE_ALREADY_EXISTS);
      }
    }


    Content targetLink = getTargetLink();
    final String targetUrl = getTargetUrl();
    //If the properties are used to create a new redirect, the targetLink is required
    if (!update) {
      if (targetLink == null && isBlank(targetUrl)) {
        errors.put(TARGET_LINK, MISSING_TARGET_LINK);
      } else if (targetLink != null && isNotEmpty(targetUrl)) {
        errors.put(TARGET_URL, BOTH_TARGET);
      }
    }
    if (targetLink != null && Boolean.TRUE.equals(getActive()) && repository.targetIsInvalid(targetLink)) {
      errors.put(TARGET_LINK, INVALID_TARGET_LINK);
    }
    if (isNotEmpty(targetUrl)) {
      try {
        final var uriComponents = UriComponentsBuilder.fromUriString(targetUrl).build();
        if (isBlank(uriComponents.getScheme()) || isBlank(uriComponents.getHost())) {
          throw new IllegalArgumentException();
        }
      } catch (Exception e) {
        errors.put(TARGET_URL, INVALID_TARGET_URL);
      }
    }

    //If the properties are used to create a new redirect, RedirectType is required
    if (!update && getRedirectType() == null) {
      errors.put(REDIRECT_TYPE, INVALID_REDIRECT_TYPE_VALUE);
    } else if (getRedirectType() == null && properties.get(REDIRECT_TYPE) != null) {
      //if the property contains a value, but it does not match any RedirectType-enum-value. Might occur in the csv-upload.
      errors.put(REDIRECT_TYPE, INVALID_REDIRECT_TYPE_VALUE);
    }

    if (getDescription() != null && getDescription().length() > 1024) {
      errors.put(DESCRIPTION, INVALID_DESCRIPTION_VALUE);
    }

    return errors;
  }

  private static boolean sourceIsValid(String source) {
    return isNotEmpty(source) && source.startsWith("/") && source.length() < 512;
  }

  private static boolean sourceHasWhitespaces(String source) {
    return isNotEmpty(source) && !source.matches("\\S+"); //only non-whitespace characters
  }

}
