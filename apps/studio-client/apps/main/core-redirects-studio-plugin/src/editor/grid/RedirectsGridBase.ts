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
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import Store from "@jangaroo/ext-ts/data/Store";
import GridPanel from "@jangaroo/ext-ts/grid/Panel";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import RedirectImpl from "../../data/RedirectImpl";
import RedirectProxy from "./RedirectProxy";
import RedirectsGrid from "./RedirectsGrid";

interface RedirectsGridBaseConfig extends Config<GridPanel> {
}

/**
 * Pageable grid component for redirects. Redirects can be filtered by source.
 * Redirects are edited or deleted via the context menu or can be edited with a double-click.
 */
class RedirectsGridBase extends GridPanel {
  declare Config: RedirectsGridBaseConfig;

  #selectedSiteVE: ValueExpression = null;

  #searchFieldVE: ValueExpression = null;

  constructor(config: Config<RedirectsGrid> = null) {
    super(config);
  }

  /**
   * Returns the ValueExpression that stores the currently selected site. The value is used to filter the redirects. If
   * the value changes, the actual selected page must be reset to 1.
   * @return ValueExpression
   */
  protected getSelectedSiteVE(config: Config<RedirectsGrid>): ValueExpression {
    if (!this.#selectedSiteVE) {
      this.#selectedSiteVE = config.selectedSiteIdVE;
      this.#selectedSiteVE.addChangeListener(bind(this, this.resetPage));
    }
    return this.#selectedSiteVE;
  }

  /**
   * Creates a ValueExpression that stores the source url filter text. The value is used to filter the redirects. If
   * the value changes, the actual selected page must be reset to 1.
   * @return ValueExpression
   */
  protected getSearchFieldVE(config: Config<RedirectsGrid>): ValueExpression {
    if (!this.#searchFieldVE) {
      this.#searchFieldVE = config.searchFieldVE ? config.searchFieldVE : ValueExpressionFactory.createFromValue("");
      this.#searchFieldVE.addChangeListener(bind(this, this.resetPage));
    }
    return this.#searchFieldVE;
  }

  /**
   * Creates a store with a custom proxy to use the {@link com.tallence.core.redirects.studio.data.Redirects }
   * to load the redirects.
   * @return Store
   */
  protected getRedirectsStore(config: Config<RedirectsGrid>): Store {
    const exactMatch: boolean = config.exactMatch != undefined ? config.exactMatch : false;
    const proxy = new RedirectProxy(this.getSelectedSiteVE(config), this.getSearchFieldVE(config), config.selectedRedirect, exactMatch);

    const store = new Store(Config(Store, {
      pageSize: 20,
      sorters: [
        {
          property: RedirectImpl.SOURCE,
          direction: "ASC",
        },
      ],
      remoteSort: true,
      proxy: proxy,
    }));
    store.load();
    return store;
  }

  protected resetPage(): void {
    this.getStore().loadPage(1);
  }

  protected override onDestroy(): void {
    if (this.#selectedSiteVE) this.#selectedSiteVE.removeChangeListener(bind(this, this.resetPage));
    if (this.#searchFieldVE) this.#searchFieldVE.removeChangeListener(bind(this, this.resetPage));
    super.onDestroy();
  }
}

export default RedirectsGridBase;
