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

package com.tallence.core.redirects.studio.editor {
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.tallence.core.redirects.studio.data.PermissionResponse;
import com.tallence.core.redirects.studio.data.RedirectRepositoryImpl;
import com.tallence.core.redirects.studio.editor.form.RedirectEditWindow;
import com.tallence.core.redirects.studio.editor.upload.RedirectUploadWindow;

import ext.Ext;
import ext.panel.Panel;

use namespace editorContext;

public class RedirectManagerEditorBase extends Panel {

  protected static const ID:String = "redirectManagerEditor";

  private var selectedSiteVE:ValueExpression;
  private var mayWriteVE:ValueExpression;
  private var mayUseRegexVE:ValueExpression;

  public function RedirectManagerEditorBase(config:RedirectManagerEditor = null) {
    super(config);

    getSelectedSiteVE().addChangeListener(resolveRights);
    //Call it in case the site is already selected
    resolveRights();
  }

  private function resolveRights(): void {
    var siteId:* = getSelectedSiteVE().getValue();

    //In case the request takes long or fails, the user has no rights
    getMayWriteVE().setValue(false);
    getMayUseRegexVE().setValue(false);

    RedirectRepositoryImpl.getInstance().resolvePermissions(siteId).then(function(response: PermissionResponse): void {
      getMayWriteVE().setValue(response.isMayWrite());
      getMayUseRegexVE().setValue(response.isMayUseRegex());
    });
  }

  public static function getInstance():RedirectManagerEditor {
    return Ext.getCmp(ID) as RedirectManagerEditor;
  }

  protected function createRedirect():void {
    var window:RedirectEditWindow = new RedirectEditWindow(RedirectEditWindow({
      title: resourceManager.getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_new_text'),
      selectedSiteIdVE: getSelectedSiteVE(),
      mayUseRegexVE: this.mayUseRegexVE
    }));
    window.show();
  }

  protected function csvUploadButtonHandler():void {
    var dialog:RedirectUploadWindow = new RedirectUploadWindow(
        RedirectUploadWindow({
          selectedSiteIdVE: getSelectedSiteVE()
        })
    );
    dialog.show();
  }

  /**
   * Creates a ValueExpression that stores the currently selected site.
   * @return ValueExpression
   */
  protected function getSelectedSiteVE():ValueExpression {
    if (!selectedSiteVE) {
      var preferredSite:Site = editorContext.getSitesService().getPreferredSite();
      selectedSiteVE = ValueExpressionFactory.createFromValue(preferredSite ? preferredSite.getId() : "");
    }
    return selectedSiteVE;
  }

  protected function getMayWriteVE():ValueExpression {
    if (!mayWriteVE) {
      mayWriteVE = ValueExpressionFactory.createFromValue(false);
    }
    return mayWriteVE;
  }

  protected function getMayUseRegexVE():ValueExpression {
    if (!mayUseRegexVE) {
      mayUseRegexVE = ValueExpressionFactory.createFromValue(false);
    }
    return mayUseRegexVE;
  }

  protected function getSiteIsNotSelectedVE():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Boolean {
      var siteId:String = getSelectedSiteVE().getValue();
      return !siteId || siteId == "";
    })
  }

}
}