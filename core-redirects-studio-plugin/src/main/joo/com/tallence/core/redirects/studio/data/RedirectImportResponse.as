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
 * Redirects import response containing the imported redirects and the error messages of the import.
 */
public class RedirectImportResponse {

  private var redirects:Array;
  private var errors:Array;

  public function RedirectImportResponse(redirects:Array, errors:Array) {
    this.redirects = redirects;
    this.errors = errors;
  }

  public function getRedirects():Array {
    return redirects;
  }

  public function getErrors():Array {
    return errors;
  }
}
}