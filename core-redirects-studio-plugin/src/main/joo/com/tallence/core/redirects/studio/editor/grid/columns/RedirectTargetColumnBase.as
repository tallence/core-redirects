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

package com.tallence.core.redirects.studio.editor.grid.columns {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;

import ext.grid.column.Column;

use namespace editorContext;

[ResourceBundle('com.coremedia.icons.CoreIcons')]
public class RedirectTargetColumnBase extends Column {

  public function RedirectTargetColumnBase(config:RedirectTargetColumn = null) {
    super(config);
  }

  protected function targetColRenderer(value:Content):String {
    var name:String = '';
    if (value) {
      name = value.getName();
    } else {
      name = '<i>' + resourceManager.getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_list_error_no_target') + '</i>';
    }
    return name;
  }

}
}