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
import EncodingUtil from "@coremedia/studio-client.client-core/util/EncodingUtil";
import BeanRecord from "@coremedia/studio-client.ext.ui-components/store/BeanRecord";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Site from "@coremedia/studio-client.multi-site-models/Site";
import RowSelectionModel from "@jangaroo/ext-ts/selection/RowModel";
import Format from "@jangaroo/ext-ts/util/Format";
import { as, bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import Redirect from "../../data/Redirect";
import RedirectEditWindow from "../form/RedirectEditWindow";
import RedirectsGrid from "./RedirectsGrid";
import RedirectsOverviewGrid from "./RedirectsOverviewGrid";

interface RedirectsOverviewGridBaseConfig extends Config<RedirectsGrid> {
}

/**
 * Pageable grid component for redirects. Redirects can be filtered by source.
 * Redirects are edited or deleted via the context menu or can be edited with a double-click.
 */
class RedirectsOverviewGridBase extends RedirectsGrid {
  declare Config: RedirectsOverviewGridBaseConfig;

  protected static readonly TOOLBAR_ID: string = "pagingToolbar";

  #mayNotUseRegexVE: ValueExpression = null;

  #mayNotUseTargetUrlsVE: ValueExpression = null;

  #mayNotPublishVE: ValueExpression = null;

  constructor(config: Config<RedirectsOverviewGrid> = null) {
    super(config);
    this.addListener("rowdblclick", bind(this, this.openEditWindow));

    this.#mayNotUseRegexVE = config.mayNotUseRegexVE;
    this.#mayNotUseTargetUrlsVE = config.mayNotUseTargetUrlsVE;
    this.#mayNotPublishVE = config.mayNotPublishVE;
  }

  /**
   * Creates a ValueExpression with an array of all registered sites.
   * @return
   */
  protected getSitesStoreVE(): ValueExpression {
    return ValueExpressionFactory.createFromFunction((): Array<any> => {
      const sitesStore = [];
      const sites = editorContext._.getSitesService().getSites();
      if (sites) {
        for (let i = 0; i < sites.length; i++) {
          const site: Site = sites[i];
          const pattern = Editor_properties.HeaderToolbar_siteSelector_displayName_pattern;
          const calculatedSiteName = Format.format(pattern,
            EncodingUtil.encodeForHTML(site.getName()),
            site.getLocale().getLanguageTag(),
            site.getLocale().getDisplayName());
          sitesStore.push({
            id: site.getId(),
            name: calculatedSiteName,
          });
        }
      }
      return sitesStore;
    });
  }

  protected openEditWindow(): void {
    const window = new RedirectEditWindow(Config(RedirectEditWindow, {
      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_edit_text,
      redirect: this.#getSelectedRedirect(),
      mayNotPublishVE: this.#mayNotPublishVE,
      mayNotUseRegexVE: this.#mayNotUseRegexVE,
      mayNotUseTargetUrlsVE: this.#mayNotUseTargetUrlsVE,
      selectedSiteIdVE: this.selectedSiteIdVE,
    }));
    window.show();
  }

  protected deleteRedirect(): void {
    this.#getSelectedRedirect().deleteMe(bind(this, this.resetPage));
  }

  protected openRedirectTarget(): void {
    const targetLink = this.#getSelectedRedirect().getTargetLink();
    editorContext._.getContentTabManager().openDocument(targetLink);
  }

  /**
   * Returns the selected redirect. The selection model always consists of one element,
   * because multiple selection is disabled.
   * @return Redirect
   */
  #getSelectedRedirect(): Redirect {
    const selections: Array<any> = as(this.getSelectionModel(), RowSelectionModel).getSelection();
    const selection: BeanRecord = selections && selections.length > 0 && selections[0];
    return selection ? as(selection.getBean(), Redirect) : null;
  }

}

export default RedirectsOverviewGridBase;
