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

public class PermissionResponse {

  private var mayWrite:Boolean;
  private var mayPublish:Boolean;
  private var mayUseRegex:Boolean;
  private var mayUseTargetUrls:Boolean;

  public function PermissionResponse(jsonResponse:Object = null) {
    this.mayWrite = jsonResponse ? jsonResponse.mayWrite : false;
    this.mayPublish = jsonResponse ? jsonResponse.mayPublish : false;
    this.mayUseRegex = jsonResponse ? jsonResponse.mayUseRegex : false;
    this.mayUseTargetUrls = jsonResponse ? jsonResponse.mayUseTargetUrls : false;
  }

  public function isMayWrite():Boolean {
    return mayWrite;
  }

  public function isMayPublish():Boolean {
    return mayPublish;
  }

  public function isMayUseRegex():Boolean {
    return mayUseRegex;
  }

  public function isMayUseTargetUrls():Boolean {
    return mayUseTargetUrls;
  }
}
}
