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

package com.tallence.core.redirects.studio.action {

import com.coremedia.cms.editor.sdk.desktop.WorkArea;
import com.coremedia.cms.editor.sdk.desktop.WorkAreaTabType;
import com.coremedia.cms.editor.sdk.editorContext;
import com.tallence.core.redirects.studio.editor.RedirectManagerEditor;
import com.tallence.core.redirects.studio.editor.RedirectManagerEditorBase;

import ext.Action;
import ext.panel.Panel;

use namespace editorContext;

public class OpenRedirectManagerEditorActionBase extends Action {

  public function OpenRedirectManagerEditorActionBase(config:OpenRedirectManagerEditorAction = null) {
    config.handler = openRedirectAdmin;
    super(config);
  }

  public static function openRedirectAdmin():void {
    var workArea:WorkArea = editorContext.getWorkArea() as WorkArea;
    var redirectManagerTab:RedirectManagerEditor = RedirectManagerEditorBase.getInstance();

    if (!redirectManagerTab) {
      var workAreaTabType:WorkAreaTabType = workArea.getTabTypeById(RedirectManagerEditor.xtype);
      workAreaTabType.createTab(null, function (tab:Panel):void {
        redirectManagerTab = tab as RedirectManagerEditor;
        workArea.addTab(workAreaTabType, redirectManagerTab);
      });
    }

    workArea.setActiveTab(redirectManagerTab);
  }
}
}