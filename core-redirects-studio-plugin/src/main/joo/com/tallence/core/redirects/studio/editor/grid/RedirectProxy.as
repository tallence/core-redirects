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
import com.coremedia.ui.store.BeanRecord;
import com.tallence.core.redirects.studio.data.Redirect;
import com.tallence.core.redirects.studio.data.RedirectImpl;
import com.tallence.core.redirects.studio.data.RedirectsResponse;
import com.tallence.core.redirects.studio.util.RedirectsUtil;

import ext.IPromise;
import ext.Promise;
import ext.data.ResultSet;
import ext.data.operation.Operation;
import ext.data.operation.ReadOperation;
import ext.data.proxy.DataProxy;

use namespace editorContext;

/**
 * A proxy for a Store to load redirects asynchronously.
 */
public class RedirectProxy extends DataProxy {

  private var selectedSiteVE:ValueExpression;
  private var searchFieldVE:ValueExpression;
  private var selectedRedirect:Redirect;
  private var exactMatch:Boolean;

  /**
   * Creates a RedirectsProxy
   *
   * @param selectedSiteVE ValueExpression holding the selected site id
   * @param searchFieldVE ValueExpression holding the search text
   * @param selectedRedirect the selected redirect to be excluded in the grid if the grid is to be used in the edit window
   * @param exactMatch true if the path of the redirect for the search should match exactly
   */
  public function RedirectProxy(selectedSiteVE:ValueExpression, searchFieldVE:ValueExpression, selectedRedirect:Redirect, exactMatch:Boolean) {
    super();
    this.selectedSiteVE = selectedSiteVE;
    this.searchFieldVE = searchFieldVE;
    this.selectedRedirect = selectedRedirect;
    this.exactMatch = exactMatch;
  }

  /**
   * The method is called by the redirects grid as soon as the search input changes, the selected page changes or the
   * refresh button of the table is clicked.
   *
   * @param operation the read operation of the grid.
   */
  override public function read(operation:Operation):void {
    loadRedirects(operation as ReadOperation)
        .then(createResultSet)
        .then(function (resultSet:ResultSet):void {
          operation.setResultSet(resultSet);
          operation.setSuccessful(true);
        });
  }

  /**
   * Converts the loaded {@link RedirectsResponse} into a {@link ResultSet}. The {@link ResultSet} is then used by the
   * grid to update the grid store and the pageable /searchable toolbar.
   *
   * @param redirectsResponse the loaded response.
   * @return The promise. Resolve method signature: <code>function(response:ResultSet):void</code>
   */
  private function createResultSet(redirectsResponse:RedirectsResponse):IPromise {
    var recordType:Class = BeanRecord.create(RedirectImpl.REDIRECT_PROPERTIES, false);

    var beanRecords:Array = [];
    redirectsResponse.getRedirects().forEach(function (redirect:Redirect):void {
      if (!selectedRedirect || selectedRedirect.getId() != redirect.getId()) {
        beanRecords.push(new recordType({bean: redirect}));
      }
    });

    var resultSet:ResultSet = new ResultSet();
    resultSet.setRecords(beanRecords);
    resultSet.setTotal(redirectsResponse.getTotal());
    return Promise.resolve(resultSet);
  }

  /**
   * Loads all redirects for the given read operation.
   * @param operation the read operation.
   *
   * @return The promise. Resolve method signature: <code>function(response:RedirectsResponse):void</code>
   */
  protected function loadRedirects(operation:ReadOperation):IPromise {
    var siteId:String = selectedSiteVE.getValue();
    var searchText:String = searchFieldVE.getValue();
    return RedirectsUtil.getRedirects(siteId, searchText, operation, exactMatch);
  }

}
}
