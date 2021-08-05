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

/**
 * The abstract class for both {@link RedirectSourceParameter} and {@link RedirectTargetParameter} classes. The class
 * contains the getter for the common variables and the constants to read the parameters from the struct.
 */
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
