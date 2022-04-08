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

import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import BeanRecord from "@coremedia/studio-client.ext.ui-components/store/BeanRecord";
import IPromise from "@jangaroo/ext-ts/IPromise";
import Promise from "@jangaroo/ext-ts/Promise";
import ResultSet from "@jangaroo/ext-ts/data/ResultSet";
import Operation from "@jangaroo/ext-ts/data/operation/Operation";
import ReadOperation from "@jangaroo/ext-ts/data/operation/Read";
import DataProxy from "@jangaroo/ext-ts/data/proxy/Proxy";
import { as, bind } from "@jangaroo/runtime";
import Redirect from "../../data/Redirect";
import RedirectImpl from "../../data/RedirectImpl";
import RedirectsResponse from "../../data/RedirectsResponse";
import RedirectsUtil from "../../util/RedirectsUtil";

/**
 * A proxy for a Store to load redirects asynchronously.
 */
class RedirectProxy extends DataProxy {

  #selectedSiteVE: ValueExpression = null;

  #searchFieldVE: ValueExpression = null;

  #selectedRedirect: Redirect = null;

  #exactMatch: boolean = false;

  /**
   * Creates a RedirectsProxy
   *
   * @param selectedSiteVE ValueExpression holding the selected site id
   * @param searchFieldVE ValueExpression holding the search text
   * @param selectedRedirect the selected redirect to be excluded in the grid if the grid is to be used in the edit window
   * @param exactMatch true if the path of the redirect for the search should match exactly
   */
  constructor(selectedSiteVE: ValueExpression, searchFieldVE: ValueExpression, selectedRedirect: Redirect, exactMatch: boolean) {
    super();
    this.#selectedSiteVE = selectedSiteVE;
    this.#searchFieldVE = searchFieldVE;
    this.#selectedRedirect = selectedRedirect;
    this.#exactMatch = exactMatch;
  }

  /**
   * The method is called by the redirects grid as soon as the search input changes, the selected page changes or the
   * refresh button of the table is clicked.
   *
   * @param operation the read operation of the grid.
   */
  override read(operation: Operation): void {
    this.loadRedirects(as(operation, ReadOperation))
      .then(bind(this, this.#createResultSet))
      .then((resultSet: ResultSet): void => {
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
  #createResultSet(redirectsResponse: RedirectsResponse): IPromise {
    const recordType = BeanRecord.create(RedirectImpl.REDIRECT_PROPERTIES, false);

    const beanRecords = [];
    redirectsResponse.getRedirects().forEach((redirect: Redirect): void => {
      if (!this.#selectedRedirect || this.#selectedRedirect.getId() != redirect.getId()) {
        beanRecords.push(new recordType({ bean: redirect }));
      }
    });

    const resultSet = new ResultSet();
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
  protected loadRedirects(operation: ReadOperation): IPromise {
    const siteId: string = this.#selectedSiteVE.getValue();
    const searchText: string = this.#searchFieldVE.getValue();
    return RedirectsUtil.getRedirects(siteId, searchText, operation, this.#exactMatch);
  }

}

export default RedirectProxy;
