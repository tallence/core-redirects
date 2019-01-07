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
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.sites.Site;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.store.BeanRecord;
import com.coremedia.ui.util.EncodingUtil;
import com.tallence.core.redirects.studio.data.Redirect;
import com.tallence.core.redirects.studio.data.RedirectImpl;
import com.tallence.core.redirects.studio.data.RedirectRepositoryImpl;
import com.tallence.core.redirects.studio.editor.form.RedirectEditWindow;

import ext.data.Store;
import ext.grid.GridPanel;
import ext.selection.RowSelectionModel;
import ext.toolbar.PagingToolbar;
import ext.toolbar.events.PagingToolbar_pageEvent;
import ext.util.Format;

use namespace editorContext;

/**
 * Pageable grid component for redirects. Redirects can be filtered by source.
 * Redirects are edited or deleted via the context menu or can be edited with a double-click.
 */
[ResourceBundle('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin')]
[ResourceBundle('com.coremedia.cms.editor.Editor')]
public class RedirectsGridBase extends GridPanel {

  protected static const TOOLBAR_ID:String = "pagingToolbar";

  private var selectedSiteVE:ValueExpression;
  private var searchFieldVE:ValueExpression;

  public function RedirectsGridBase(config:RedirectsGrid = null) {
    super(config);
    addListener('rowDblclick', openEditWindow);

    // force reload of redirects
    var toolbar:PagingToolbar = queryById(TOOLBAR_ID) as PagingToolbar;
    toolbar.on("beforechange", function (event:PagingToolbar_pageEvent):void {
      RedirectRepositoryImpl.getInstance().invalidateRedirects();
    });
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

  /**
   * Returns the ValueExpression that stores the currently selected site. The value is used to filter the redirects. If
   * the value changes, the actual selected page must be reset to 1.
   * @return ValueExpression
   */
  protected function getSelectedSiteVE(config:RedirectsGrid):ValueExpression {
    if (!selectedSiteVE) {
      selectedSiteVE = config.selectedSiteIdVE;
      selectedSiteVE.addChangeListener(resetPage)
    }
    return selectedSiteVE;
  }

  /**
   * Creates a ValueExpression that stores the source url filter text. The value is used to filter the redirects. If
   * the value changes, the actual selected page must be reset to 1.
   * @return ValueExpression
   */
  protected function getSearchFieldVE():ValueExpression {
    if (!searchFieldVE) {
      searchFieldVE = ValueExpressionFactory.createFromValue("");
      searchFieldVE.addChangeListener(resetPage);
    }
    return searchFieldVE;
  }

  /**
   * Creates a store with a custom proxy to use the {@link com.tallence.core.redirects.studio.data.RedirectRepository }
   * to load the redirects.
   * @return Store
   */
  protected function getRedirectsStore(config:RedirectsGrid):Store {
    var proxy:RedirectProxy = new RedirectProxy(getSelectedSiteVE(config), getSearchFieldVE());

    var store:Store = new Store(Store({
      pageSize: 20,
      sorters: [
        {
          property: RedirectImpl.SOURCE,
          direction: 'ASC'
        }
      ],
      remoteSort: true,
      proxy: proxy
    }));
    store.load();
    return store;
  }

  protected function openEditWindow():void {
    var window:RedirectEditWindow = new RedirectEditWindow(RedirectEditWindow({
      title: resourceManager.getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_edit_text'),
      redirect: getSelectedRedirect()
    }));
    window.show()
  }

  protected function deleteRedirect():void {
    getSelectedRedirect().deleteMe(resetPage);
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

  private function resetPage():void {
    RedirectRepositoryImpl.getInstance().invalidateRedirects();
    getStore().loadPage(1);
  }

  override protected function onDestroy():void {
    if (selectedSiteVE) selectedSiteVE.removeChangeListener(resetPage);
    if (searchFieldVE) searchFieldVE.removeChangeListener(resetPage);
    super.onDestroy();
  }
}
}