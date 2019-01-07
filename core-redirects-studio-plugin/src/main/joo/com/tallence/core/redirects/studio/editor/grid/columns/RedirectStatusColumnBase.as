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
import com.coremedia.cms.editor.sdk.columns.grid.IconColumn;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.store.BeanRecord;
import com.tallence.core.redirects.studio.data.Redirect;

import ext.data.Model;
import ext.data.Store;

use namespace editorContext;

[ResourceBundle('com.coremedia.icons.CoreIcons')]
public class RedirectStatusColumnBase extends IconColumn {

  public function RedirectStatusColumnBase(config:RedirectStatusColumn = null) {
    super(config);
  }

  override protected function calculateIconCls(value:*, metadata:*, record:Model, rowIndex:Number, colIndex:Number, store:Store):String {
    var redirect:Redirect = (record as BeanRecord).getBean() as Redirect;
    if (redirect.isActive()) {
      return resourceManager.getString('com.coremedia.icons.CoreIcons', 'checkbox_checked');
    }
    return resourceManager.getString('com.coremedia.icons.CoreIcons', 'checkbox_unchecked');
  }

}
}