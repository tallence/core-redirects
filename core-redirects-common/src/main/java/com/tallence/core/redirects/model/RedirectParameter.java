package com.tallence.core.redirects.model;

public abstract class RedirectParameter {

  public static final String PROPERTY_URL_PARAMS = "urlParams";
  public static final String STRUCT_PROPERTY_PARAMS_NAME = "name";
  public static final String STRUCT_PROPERTY_PARAMS_VALUE = "value";

  private final String name;
  private final String value;

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

}
