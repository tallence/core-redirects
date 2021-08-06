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
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.tallence.core.redirects.studio.data.RedirectImpl;

import ext.data.Store;
import ext.grid.GridPanel;

use namespace editorContext;

/**
 * Pageable grid component for redirects. Redirects can be filtered by source.
 * Redirects are edited or deleted via the context menu or can be edited with a double-click.
 */
[ResourceBundle('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin')]
[ResourceBundle('com.coremedia.cms.editor.Editor')]
public class RedirectsGridBase extends GridPanel {

  private var selectedSiteVE:ValueExpression;
  private var searchFieldVE:ValueExpression;

  public function RedirectsGridBase(config:RedirectsGrid = null) {
    super(config);
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
  protected function getSearchFieldVE(config:RedirectsGrid):ValueExpression {
    if (!searchFieldVE) {
      searchFieldVE = config.searchFieldVE ? config.searchFieldVE : ValueExpressionFactory.createFromValue("");
      searchFieldVE.addChangeListener(resetPage);
    }
    return searchFieldVE;
  }

  /**
   * Creates a store with a custom proxy to use the {@link com.tallence.core.redirects.studio.data.Redirects }
   * to load the redirects.
   * @return Store
   */
  protected function getRedirectsStore(config:RedirectsGrid):Store {
    var exactMatch:Boolean = config.exactMatch != undefined ? config.exactMatch : false;
    var proxy:RedirectProxy = new RedirectProxy(getSelectedSiteVE(config), getSearchFieldVE(config), config.selectedRedirect, exactMatch);

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

  protected function resetPage():void {
    getStore().loadPage(1);
  }

  override protected function onDestroy():void {
    if (selectedSiteVE) selectedSiteVE.removeChangeListener(resetPage);
    if (searchFieldVE) searchFieldVE.removeChangeListener(resetPage);
    super.onDestroy();
  }
}
}
