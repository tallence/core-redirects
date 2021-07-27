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
import com.coremedia.cap.struct.Struct;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectTargetParameter;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.coremedia.cap.util.CapStructUtil.getString;
import static com.coremedia.cap.util.CapStructUtil.getSubstructs;
import static com.tallence.core.redirects.model.RedirectParameter.*;
import static com.tallence.core.redirects.model.RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS;
import static com.tallence.core.redirects.model.RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS_OPERATOR;
import static com.tallence.core.redirects.model.RedirectTargetParameter.STRUCT_PROPERTY_TARGET_PARAMS;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Model for a Redirect (used instead of a ContentBean in order to keep the overhead low).
 * Keeps only the properties required by the CAE.
 */
public class Redirect {

  private static final Logger LOG = LoggerFactory.getLogger(Redirect.class);

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
  private final List<RedirectSourceParameter> sourceParameters;
  private final List<RedirectTargetParameter> targetParameters;

  public Redirect(Content redirect, String rootSegment) {
    contentId = redirect.getId();
    sourceUrlType = SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE));
    source = rootSegment + redirect.getString(SOURCE_URL);
    redirectType = RedirectType.asRedirectType(redirect.getString(REDIRECT_TYPE));
    target = redirect.getLink(TARGET_LINK);

    var urlParams = Optional.ofNullable(redirect.getStruct(PROPERTY_URL_PARAMS));
    sourceParameters = urlParams.map(u -> getSubstructs(u, STRUCT_PROPERTY_SOURCE_PARAMS))
            .map(sourceParams -> sourceParams.stream().map(this::buildSourceParam).filter(Objects::nonNull).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    targetParameters = urlParams.map(u -> getSubstructs(u, STRUCT_PROPERTY_TARGET_PARAMS))
            .map(sourceParams -> sourceParams.stream().map(this::buildTargetParam).filter(Objects::nonNull).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
  }

  private RedirectSourceParameter buildSourceParam(Struct sourceParam) {
    final var name = getString(sourceParam, STRUCT_PROPERTY_PARAMS_NAME);
    final var value = getString(sourceParam, STRUCT_PROPERTY_PARAMS_VALUE);
    final var operator = getString(sourceParam, STRUCT_PROPERTY_SOURCE_PARAMS_OPERATOR);
    if (isEmpty(name) || isEmpty(value) || isEmpty(operator)) {
      LOG.warn("Cannot parse sourceParam for redirect {}", contentId);
      return null;
    }
    try {
      final var parsedOperator = RedirectSourceParameter.Operator.valueOf(operator);
      return new RedirectSourceParameter(name, value, parsedOperator);
    } catch (IllegalArgumentException e) {
      LOG.warn("Cannot parse the operator {} for redirect {}", operator, contentId);
      return null;
    }
  }

  private RedirectTargetParameter buildTargetParam(Struct targetParam) {
    final var name = getString(targetParam, STRUCT_PROPERTY_PARAMS_NAME);
    final var value = getString(targetParam, STRUCT_PROPERTY_PARAMS_VALUE);
    if (isEmpty(name) || isEmpty(value)) {
      LOG.warn("Cannot parse targetParam for redirect {}", contentId);
      return null;
    }
    return new RedirectTargetParameter(name, value);
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

  public List<RedirectSourceParameter> getSourceParameters() {
    return sourceParameters;
  }

  public List<RedirectTargetParameter> getTargetParameters() {
    return targetParameters;
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
