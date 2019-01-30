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

/**
 * Redirect validation response containing the error codes if the redirect is invalid.
 */
public class ValidationResponse {

  private var valid:Boolean;
  private var errorCodes:Array;

  public function ValidationResponse(jsonResponse:Object) {
    this.valid = jsonResponse.valid;
    this.errorCodes = jsonResponse.errorCodes;
  }

  public function isValid():Boolean {
    return valid;
  }

  public function getErrorCodes():Array {
    return errorCodes;
  }
}
}