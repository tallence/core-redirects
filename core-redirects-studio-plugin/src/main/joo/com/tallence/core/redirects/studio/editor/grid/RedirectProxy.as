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
import com.tallence.core.redirects.studio.data.RedirectRepositoryImpl;
import com.tallence.core.redirects.studio.data.RedirectsResponse;

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

  /**
   * Creates a RedirectsProxy
   *
   * @param selectedSiteVE ValueExpression holding the selected site id
   * @param searchFieldVE ValueExpression holding the search text
   */
  public function RedirectProxy(selectedSiteVE:ValueExpression, searchFieldVE:ValueExpression) {
    super();
    this.selectedSiteVE = selectedSiteVE;
    this.searchFieldVE = searchFieldVE;
  }

  /**
   * Loads all redirects for the given operation.
   * @param operation the read operation
   */
  override public function read(operation:Operation):void {
    loadRedirects(operation as ReadOperation)
        .then(createResultSet)
        .then(function (resultSet:ResultSet):void {
          operation.setResultSet(resultSet);
          operation.setSuccessful(true);
        });
  }

  private function createResultSet(redirectsResponse:RedirectsResponse):IPromise {
    var recordType:Class = BeanRecord.create(RedirectImpl.REDIRECT_PROPERTIES, false);

    var beanRecords:Array = [];
    redirectsResponse.getRedirects().forEach(function (redirect:Redirect):void {
      beanRecords.push(new recordType({bean: redirect}));
    });

    var resultSet:ResultSet = new ResultSet();
    resultSet.setRecords(beanRecords);
    resultSet.setTotal(redirectsResponse.getTotal());
    return Promise.resolve(resultSet);
  }

  protected function loadRedirects(operation:ReadOperation):IPromise {
    var siteId:String = selectedSiteVE.getValue();
    var searchText:String = searchFieldVE.getValue();
    return RedirectRepositoryImpl.getInstance().getRedirects(siteId, searchText, operation);
  }

}
}