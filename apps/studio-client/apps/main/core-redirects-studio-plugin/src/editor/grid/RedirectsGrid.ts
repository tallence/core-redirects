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
import TableView from "@jangaroo/ext-ts/view/Table";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import Redirect from "../../data/Redirect";
import RedirectsGridBase from "./RedirectsGridBase";

interface RedirectsGridConfig extends Config<RedirectsGridBase>, Partial<Pick<RedirectsGrid,
  "selectedSiteIdVE" |
  "searchFieldVE" |
  "selectedRedirect" |
  "exactMatch"
>> {
}

class RedirectsGrid extends RedirectsGridBase {
  declare Config: RedirectsGridConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.grid.redirectsGrid";

  #selectedSiteIdVE: ValueExpression = null;

  get selectedSiteIdVE(): ValueExpression {
    return this.#selectedSiteIdVE;
  }

  set selectedSiteIdVE(value: ValueExpression) {
    this.#selectedSiteIdVE = value;
  }

  #searchFieldVE: ValueExpression = null;

  get searchFieldVE(): ValueExpression {
    return this.#searchFieldVE;
  }

  set searchFieldVE(value: ValueExpression) {
    this.#searchFieldVE = value;
  }

  #selectedRedirect: Redirect = null;

  get selectedRedirect(): Redirect {
    return this.#selectedRedirect;
  }

  set selectedRedirect(value: Redirect) {
    this.#selectedRedirect = value;
  }

  #exactMatch: boolean = false;

  get exactMatch(): boolean {
    return this.#exactMatch;
  }

  set exactMatch(value: boolean) {
    this.#exactMatch = value;
  }

  /**
   * The base class for the grid used in two different places. The {@link RedirectsOverviewGrid} is used in the
   * overview and shows all redirects. The redirects are filtered by two filters (site and search). These two values
   * can be passed as variables.
   *
   * The {@link IdenticalRedirectsGrid} grid is used within the edit panel and shows redirects to the same source url.
   * Here the value expression for the site and the search is passed directly and can not be edited by the user.
   */
  constructor(config: Config<RedirectsGrid> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectsGrid, {
      enableColumnHide: false,
      enableColumnMove: false,
      margin: "10 10 10 10",
      height: 10,
      store: this.getRedirectsStore(config),
      forceFit: true,
      draggable: false,
      viewConfig: Config(TableView, {
        stripeRows: true,
        trackOver: true,
        loadMask: true,
        deferEmptyText: false,
        minHeight: 25,
        loadingText: RedirectManagerStudioPlugin_properties.redirectmanager_editor_list_loading,
        emptyText: ConfigUtils.asString(config.emptyText ? config.emptyText : RedirectManagerStudioPlugin_properties.redirectmanager_editor_list_nothing_found),
      }),

    }), config))());
  }
}

export default RedirectsGrid;
