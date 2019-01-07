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
 * Redirects response containing the redirects for the page and the number of total records.
 */
public class RedirectsResponse {

  private var redirects:Array;
  private var total:Number;

  public function RedirectsResponse(redirects:Array, total:Number) {
    this.redirects = redirects;
    this.total = total;
  }

  public function getRedirects():Array {
    return redirects;
  }

  public function getTotal():Number {
    return total;
  }
}
}