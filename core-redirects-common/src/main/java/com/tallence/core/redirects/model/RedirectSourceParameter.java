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
