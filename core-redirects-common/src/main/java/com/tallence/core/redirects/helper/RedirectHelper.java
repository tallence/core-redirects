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
package com.tallence.core.redirects.helper;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallence.core.redirects.model.RedirectParameter;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectTargetParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.coremedia.cap.util.CapStructUtil.getString;
import static com.coremedia.cap.util.CapStructUtil.getSubstructs;
import static com.tallence.core.redirects.model.RedirectParameter.PROPERTY_URL_PARAMS;
import static com.tallence.core.redirects.model.RedirectParameter.STRUCT_PROPERTY_PARAMS_NAME;
import static com.tallence.core.redirects.model.RedirectParameter.STRUCT_PROPERTY_PARAMS_VALUE;
import static com.tallence.core.redirects.model.RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS;
import static com.tallence.core.redirects.model.RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS_OPERATOR;
import static com.tallence.core.redirects.model.RedirectTargetParameter.STRUCT_PROPERTY_TARGET_PARAMS;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * This helper class provides methods to read source and target parameters from the struct of the redirect. Additionally,
 * methods are provided for the studio to parse source and target parameters from a JSON string.
 */
public class RedirectHelper {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectHelper.class);

  private RedirectHelper() {
    // util class
  }

  /**
   * Returns a list of {@link RedirectSourceParameter} parameters for the given redirect, or an empty list if no
   * parameters are stored in the struct.
   */
  public static List<RedirectSourceParameter> getSourceParameters(Content redirect) {
    return Optional.ofNullable(redirect.getStruct(PROPERTY_URL_PARAMS))
            .map(u -> getSubstructs(u, STRUCT_PROPERTY_SOURCE_PARAMS))
            .map(sourceParams -> sourceParams.stream().map(param -> buildSourceParam(param, redirect.getId())).filter(Objects::nonNull).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
  }

  /**
   * Returns a list of {@link RedirectTargetParameter} parameters for the given redirect, or an empty list if no
   * parameters are stored in the struct.
   */
  public static List<RedirectTargetParameter> getTargetParameters(Content redirect) {
    return Optional.ofNullable(redirect.getStruct(PROPERTY_URL_PARAMS))
            .map(u -> getSubstructs(u, STRUCT_PROPERTY_TARGET_PARAMS))
            .map(sourceParams -> sourceParams.stream().map(param -> buildTargetParam(param, redirect.getId())).filter(Objects::nonNull).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
  }

  private static RedirectSourceParameter buildSourceParam(Struct sourceParam, String contentId) {
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

  /**
   * This method tries to parse a {@link RedirectSourceParameter} from a json representation of a source parameter. If
   * this is not possible, null is returned and an error is logged.
   */
  public static RedirectSourceParameter parseRedirectSourceParameter(String value) {
    return parseRedirectParameter(value, RedirectSourceParameter.class);
  }

  private static <T extends RedirectParameter> T parseRedirectParameter(String value, Class<T> clazz) {
    try {
      return new ObjectMapper().readValue(value, clazz);
    } catch (JsonProcessingException e) {
      LOG.error("Could not parse redirect parameter for input string: {}", value);
      return null;
    }
  }

  /**
   * This method tries to parse a list of {@link RedirectSourceParameter} parameters from a json representation. If the
   * json is invalid or null an exception is thrown and must be handled. Currently this method is only invoked by the
   * importer and the importer handles this exception to return an error message to the studio client.
   */
  public static List<RedirectSourceParameter> parseRedirectSourceParameters(String value) throws JsonProcessingException {
    return parseRedirectParameters(value, new TypeReference<List<RedirectSourceParameter>>() {
    });
  }

  /**
   * This method tries to parse a list of {@link RedirectTargetParameter} parameters from a json representation. If the
   * json is invalid or null an exception is thrown and must be handled. Currently this method is only invoked by the
   * importer and the importer handles this exception to return an error message to the studio client.
   */
  public static List<RedirectTargetParameter> parseRedirectTargetParameters(String value) throws JsonProcessingException {
    return parseRedirectParameters(value, new TypeReference<List<RedirectTargetParameter>>() {
    });
  }

  private static <T extends RedirectParameter> List<T> parseRedirectParameters(String value, TypeReference<List<T>> typeReference) throws JsonProcessingException {
    return new ObjectMapper().readValue(value, typeReference);
  }

  private static RedirectTargetParameter buildTargetParam(Struct targetParam, String contentId) {
    final var name = getString(targetParam, STRUCT_PROPERTY_PARAMS_NAME);
    final var value = getString(targetParam, STRUCT_PROPERTY_PARAMS_VALUE);
    if (isEmpty(name) || isEmpty(value)) {
      LOG.warn("Cannot parse targetParam for redirect {}", contentId);
      return null;
    }
    return new RedirectTargetParameter(name, value);
  }

}
