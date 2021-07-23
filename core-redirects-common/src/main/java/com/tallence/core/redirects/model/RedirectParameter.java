package com.tallence.core.redirects.model;

public abstract class RedirectParameter {

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
