package com.tallence.core.redirects.model;

import java.util.Objects;

public class RedirectSourceParameter extends RedirectParameter {

  public static final String STRUCT_PROPERTY_SOURCE_PARAMS = "sourceUrlParams";
  public static final String STRUCT_PROPERTY_SOURCE_PARAMS_OPERATOR = "operator";

  private RedirectSourceParameter.Operator operator;

  public RedirectSourceParameter() {
    // default constructor required for object mapper
  }

  public RedirectSourceParameter(String name, String value, Operator operator) {
    super(name, value);
    this.operator = operator;
  }

  public Operator getOperator() {
    return operator;
  }

  /**
   * Currently there is only the operator {@link RedirectSourceParameter.Operator#EQUALS} support. With this operator
   * every configured parameter must be set exactly at the request. In the future this could be extended by contains or
   * starts with as an operator type.
   */
  public enum Operator {
    EQUALS
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    RedirectSourceParameter that = (RedirectSourceParameter) o;
    return operator == that.operator;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), operator);
  }

}
