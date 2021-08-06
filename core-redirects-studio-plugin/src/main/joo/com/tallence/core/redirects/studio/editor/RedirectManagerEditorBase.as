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
import com.coremedia.cms.studio.multisite.models.sites.Site;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.tallence.core.redirects.studio.data.PermissionResponse;
import com.tallence.core.redirects.studio.editor.form.RedirectEditWindow;
import com.tallence.core.redirects.studio.editor.upload.RedirectUploadWindow;
import com.tallence.core.redirects.studio.util.RedirectsUtil;

import ext.Ext;
import ext.panel.Panel;

use namespace editorContext;

public class RedirectManagerEditorBase extends Panel {

  protected static const ID:String = "redirectManagerEditor";

  private var selectedSiteVE:ValueExpression;
  private var mayNotWriteVE:ValueExpression;
  private var mayNotPublishVE:ValueExpression;
  private var mayNotUseRegexVE:ValueExpression;
  private var mayNotUseTargetUrlsVE:ValueExpression;

  public function RedirectManagerEditorBase(config:RedirectManagerEditor = null) {
    super(config);

    getSelectedSiteVE().addChangeListener(resolveRights);
    //Call it in case the site is already selected
    resolveRights();
  }

  private function resolveRights(): void {
    var siteId:* = getSelectedSiteVE().getValue();

    //In case the request takes long or fails, the user has no rights
    getMayNotWriteVE().setValue(true);
    getMayNotPublishVE().setValue(true);
    getMayNotUseRegexVE().setValue(true);

    RedirectsUtil.resolvePermissions(siteId).then(function(response: PermissionResponse): void {
      getMayNotWriteVE().setValue(!response.isMayWrite());
      getMayNotPublishVE().setValue(!response.isMayPublish());
      getMayNotUseRegexVE().setValue(!response.isMayUseRegex());
      getMayNotUseTargetUrlsVE().setValue(!response.isMayUseTargetUrls());
    });
  }

  public static function getInstance():RedirectManagerEditor {
    return Ext.getCmp(ID) as RedirectManagerEditor;
  }

  protected function createRedirect():void {
    var window:RedirectEditWindow = new RedirectEditWindow(RedirectEditWindow({
      title: resourceManager.getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_new_text'),
      selectedSiteIdVE: getSelectedSiteVE(),
      mayNotPublishVE: this.mayNotPublishVE,
      mayNotUseRegexVE: this.mayNotUseRegexVE,
      mayNotUseTargetUrlsVE: this.mayNotUseTargetUrlsVE
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

  protected function getMayNotWriteVE():ValueExpression {
    if (!mayNotWriteVE) {
      mayNotWriteVE = ValueExpressionFactory.createFromValue(false);
    }
    return mayNotWriteVE;
  }

  protected function getMayNotPublishVE():ValueExpression {
    if (!mayNotPublishVE) {
      mayNotPublishVE = ValueExpressionFactory.createFromValue(false);
    }
    return mayNotPublishVE;
  }

  protected function getMayNotUseRegexVE():ValueExpression {
    if (!mayNotUseRegexVE) {
      mayNotUseRegexVE = ValueExpressionFactory.createFromValue(false);
    }
    return mayNotUseRegexVE;
  }

  protected function getMayNotUseTargetUrlsVE():ValueExpression {
    if (!mayNotUseTargetUrlsVE) {
      mayNotUseTargetUrlsVE = ValueExpressionFactory.createFromValue(false);
    }
    return mayNotUseTargetUrlsVE;
  }

  protected function getSiteIsNotSelectedVE():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Boolean {
      var siteId:String = getSelectedSiteVE().getValue();
      return !siteId || siteId == "";
    })
  }

}
}
