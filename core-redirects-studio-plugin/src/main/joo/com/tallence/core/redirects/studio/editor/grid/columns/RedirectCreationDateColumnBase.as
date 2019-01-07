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
import com.coremedia.cms.editor.sdk.editorContext;

import ext.DateUtil;
import ext.grid.column.Column;

use namespace editorContext;

[ResourceBundle('com.coremedia.icons.CoreIcons')]
public class RedirectCreationDateColumnBase extends Column {

  public function RedirectCreationDateColumnBase(config:RedirectCreationDateColumn = null) {
    super(config);
  }

  protected static function creationDateColRenderer(value:Date):String {
    var date:String = '';
    if (value) {
      date = DateUtil.format(value, "d.m.Y");
    }
    return date;
  }

}
}