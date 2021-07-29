package com.tallence.core.redirects.model;

import java.util.Objects;

public abstract class RedirectParameter {

  public static final String PROPERTY_URL_PARAMS = "urlParams";
  public static final String STRUCT_PROPERTY_PARAMS_NAME = "name";
  public static final String STRUCT_PROPERTY_PARAMS_VALUE = "value";

  private String name;
  private String value;

  public RedirectParameter() {
    // default constructor required for object mapper
  }

  public RedirectParameter(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RedirectParameter that = (RedirectParameter) o;
    return Objects.equals(name, that.name) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }
}
