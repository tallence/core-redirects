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

package com.tallence.core.redirects.studio.data {
public class RedirectSourceParameter extends RedirectTargetParameter {

  public static const OPERATOR:String = "operator";
  public static const OPERATOR_EQUALS:String = "EQUALS";

  public function RedirectSourceParameter(json:Object) {
    super(json);
    this[OPERATOR] = json[OPERATOR];
  }

  public function getOperator():String {
    return this[OPERATOR];
  }

  override public function getParametersAsMap():Object {
    var map:Object = super.getParametersAsMap();
    map[OPERATOR] = getOperator();
    return map;
  }
}
}
