package com.tallence.core.redirects.model;

public class RedirectTargetParameter extends RedirectParameter {

  public static final String STRUCT_PROPERTY_TARGET_PARAMS = "targetUrlParams";

  public RedirectTargetParameter() {
    // default constructor required for object mapper
  }

  public RedirectTargetParameter(String name, String value) {
    super(name, value);
  }

}
