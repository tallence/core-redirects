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

package com.tallence.core.redirects.studio.editor.grid {
import com.coremedia.cap.undoc.content.Content;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.studio.multisite.models.sites.Site;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.EncodingUtil;
import com.tallence.core.redirects.studio.data.Redirect;
import com.tallence.core.redirects.studio.editor.form.RedirectEditWindow;

import ext.selection.RowSelectionModel;
import ext.util.Format;

use namespace editorContext;

/**
 * Pageable grid component for redirects. Redirects can be filtered by source.
 * Redirects are edited or deleted via the context menu or can be edited with a double-click.
 */
[ResourceBundle('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin')]
[ResourceBundle('com.coremedia.cms.editor.Editor')]
public class RedirectsOverviewGridBase extends RedirectsGrid {

  protected static const TOOLBAR_ID:String = "pagingToolbar";

  private var mayNotUseRegexVE:ValueExpression;
  private var mayNotUseTargetUrlsVE:ValueExpression;
  private var mayNotPublishVE:ValueExpression;

  public function RedirectsOverviewGridBase(config:RedirectsOverviewGrid = null) {
    super(config);
    addListener('rowDblclick', openEditWindow);

    this.mayNotUseRegexVE = config.mayNotUseRegexVE;
    this.mayNotUseTargetUrlsVE = config.mayNotUseTargetUrlsVE;
    this.mayNotPublishVE = config.mayNotPublishVE;
  }

  /**
   * Creates a ValueExpression with an array of all registered sites.
   * @return
   */
  protected function getSitesStoreVE():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var sitesStore:Array = [];
      var sites:Array = editorContext.getSitesService().getSites();
      if (sites) {
        for (var i:int = 0; i < sites.length; i++) {
          var site:Site = sites[i];
          var pattern:String = resourceManager.getString('com.coremedia.cms.editor.Editor', 'HeaderToolbar_siteSelector_displayName_pattern');
          var calculatedSiteName:String = Format.format(pattern,
                  EncodingUtil.encodeForHTML(site.getName()),
                  site.getLocale().getLanguageTag(),
                  site.getLocale().getDisplayName());
          sitesStore.push({id: site.getId(), name: calculatedSiteName});
        }
      }
      return sitesStore;
    });
  }

  protected function openEditWindow():void {
    var window:RedirectEditWindow = new RedirectEditWindow(RedirectEditWindow({
      title: resourceManager.getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_edit_text'),
      redirect: getSelectedRedirect(),
      mayNotPublishVE: this.mayNotPublishVE,
      mayNotUseRegexVE: this.mayNotUseRegexVE,
      mayNotUseTargetUrlsVE: this.mayNotUseTargetUrlsVE,
      selectedSiteIdVE: this.selectedSiteIdVE
    }));
    window.show()
  }

  protected function deleteRedirect():void {
    getSelectedRedirect().deleteMe(resetPage);
  }

  protected function openRedirectTarget():void {
    var targetLink:Content = getSelectedRedirect().getTargetLink();
    editorContext.getContentTabManager().openDocument(targetLink);
  }

  /**
   * Returns the selected redirect. The selection model always consists of one element,
   * because multiple selection is disabled.
   * @return Redirect
   */
  private function getSelectedRedirect():Redirect {
    var selections:Array = (getSelectionModel() as RowSelectionModel).getSelection();
    var selection:BeanRecord = selections && selections.length > 0 && selections[0];
    return selection ? selection.getBean() as Redirect : null;
  }


}
}
