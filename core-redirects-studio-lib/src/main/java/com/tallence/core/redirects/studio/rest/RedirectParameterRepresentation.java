package com.tallence.core.redirects.studio.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tallence.core.redirects.model.RedirectParameter;
import com.tallence.core.redirects.model.RedirectSourceParameter;

import java.util.Optional;

public class RedirectParameterRepresentation {

  private static final String SOURCE_PARAMETER_CLASS = "com.tallence.core.redirects.studio.data.RedirectSourceParameter";
  private static final String TARGET_PARAMETER_CLASS = "com.tallence.core.redirects.studio.data.RedirectTargetParameter";

  private final String name;
  private final String type;
  private final String value;
  private final String operator;

  public RedirectParameterRepresentation(RedirectParameter redirectParameter) {
    name = redirectParameter.getName();
    value = redirectParameter.getValue();

    Optional<RedirectSourceParameter> redirectSourceParameter = Optional.of(redirectParameter)
            .filter(RedirectSourceParameter.class::isInstance)
            .map(RedirectSourceParameter.class::cast);
    type = redirectSourceParameter.isPresent() ? SOURCE_PARAMETER_CLASS : TARGET_PARAMETER_CLASS;
    operator = redirectSourceParameter
            .map(RedirectSourceParameter::getOperator)
            .map(RedirectSourceParameter.Operator::name)
            .orElse(null);
  }

  @JsonProperty("$Type")
  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getOperator() {
    return operator;
  }
}
