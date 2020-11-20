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

package com.tallence.core.redirects.studio.plugins {
import com.coremedia.cms.editor.sdk.util.UserUtil;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.plugins.NestedRulesPlugin;

use namespace editorContext;

public class AddRedirectManagerPluginBase extends NestedRulesPlugin {

  protected var groups:Array;

  public function AddRedirectManagerPluginBase(config:AddRedirectManagerPlugin = null) {
    super(config);
  }

  protected function checkPermission():Boolean {
    if (groups.length > 0) {
      for each(var group:String in groups) {
        if (UserUtil.isInGroup(group)) {
          return true;
        }
      }
      return false;
    }
    return true;
  }
}
}
