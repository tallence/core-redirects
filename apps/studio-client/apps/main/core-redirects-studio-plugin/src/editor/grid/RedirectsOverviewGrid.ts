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
import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";
import LocalComboBox from "@coremedia/studio-client.ext.ui-components/components/LocalComboBox";
import BindListPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindListPlugin";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import ContextMenuPlugin from "@coremedia/studio-client.ext.ui-components/plugins/ContextMenuPlugin";
import ToolbarSkin from "@coremedia/studio-client.ext.ui-components/skins/ToolbarSkin";
import DataField from "@coremedia/studio-client.ext.ui-components/store/DataField";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import TextField from "@jangaroo/ext-ts/form/field/Text";
import Item from "@jangaroo/ext-ts/menu/Item";
import Menu from "@jangaroo/ext-ts/menu/Menu";
import Separator from "@jangaroo/ext-ts/menu/Separator";
import PagingToolbar from "@jangaroo/ext-ts/toolbar/Paging";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImpl from "../../data/RedirectImpl";
import RedirectsOverviewGridBase from "./RedirectsOverviewGridBase";
import RedirectCreationDateColumn from "./columns/RedirectCreationDateColumn";
import RedirectParametersColumn from "./columns/RedirectParametersColumn";
import RedirectSourceColumn from "./columns/RedirectSourceColumn";
import RedirectStatusColumn from "./columns/RedirectStatusColumn";
import RedirectTargetColumn from "./columns/RedirectTargetColumn";
import RedirectTypeColumn from "./columns/RedirectTypeColumn";

interface RedirectsOverviewGridConfig extends Config<RedirectsOverviewGridBase>, Partial<Pick<RedirectsOverviewGrid,
  "mayNotWriteVE" |
  "mayNotPublishVE" |
  "mayNotUseRegexVE" |
  "mayNotUseTargetUrlsVE" |
  "siteIsNotSelectedVE"
>> {
}

class RedirectsOverviewGrid extends RedirectsOverviewGridBase {
  declare Config: RedirectsOverviewGridConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.grid.redirectsOverviewGrid";

  #mayNotWriteVE: ValueExpression = null;

  get mayNotWriteVE(): ValueExpression {
    return this.#mayNotWriteVE;
  }

  set mayNotWriteVE(value: ValueExpression) {
    this.#mayNotWriteVE = value;
  }

  #mayNotPublishVE: ValueExpression = null;

  get mayNotPublishVE(): ValueExpression {
    return this.#mayNotPublishVE;
  }

  set mayNotPublishVE(value: ValueExpression) {
    this.#mayNotPublishVE = value;
  }

  #mayNotUseRegexVE: ValueExpression = null;

  get mayNotUseRegexVE(): ValueExpression {
    return this.#mayNotUseRegexVE;
  }

  set mayNotUseRegexVE(value: ValueExpression) {
    this.#mayNotUseRegexVE = value;
  }

  #mayNotUseTargetUrlsVE: ValueExpression = null;

  get mayNotUseTargetUrlsVE(): ValueExpression {
    return this.#mayNotUseTargetUrlsVE;
  }

  set mayNotUseTargetUrlsVE(value: ValueExpression) {
    this.#mayNotUseTargetUrlsVE = value;
  }

  #siteIsNotSelectedVE: ValueExpression = null;

  get siteIsNotSelectedVE(): ValueExpression {
    return this.#siteIsNotSelectedVE;
  }

  set siteIsNotSelectedVE(value: ValueExpression) {
    this.#siteIsNotSelectedVE = value;
  }

  constructor(config: Config<RedirectsOverviewGrid> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectsOverviewGrid, {
      dockedItems: [
        Config(Toolbar, {
          ui: ToolbarSkin.HEADER_GRID_100.getSkin(),
          items: [
            Config(TextField, {
              labelAlign: "left",
              width: 300,
              checkChangeBuffer: 1000,
              fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_grid_search_label,
              emptyText: RedirectManagerStudioPlugin_properties.redirectmanager_editor_grid_search_empty_text,
              plugins: [
                Config(BindPropertyPlugin, {
                  bindTo: this.getSearchFieldVE(config),
                  bidirectional: true,
                }),
                Config(BindPropertyPlugin, {
                  componentProperty: "disabled",
                  bindTo: config.siteIsNotSelectedVE,
                }),
              ],
            }),
            Config(LocalComboBox, {
              width: 400,
              labelWidth: 180,
              fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_grid_site_selector_label,
              valueField: "id",
              displayField: "name",
              anyMatch: true,
              valueNotFoundText: Editor_properties.HeaderToolbar_siteSelector_none_text,
              emptyText: Editor_properties.HeaderToolbar_siteSelector_empty_text,
              editable: false,
              encodeItems: true,
              plugins: [
                Config(BindListPlugin, {
                  sortField: "name",
                  bindTo: this.getSitesStoreVE(),
                  fields: [
                    Config(DataField, {
                      name: "id",
                      encode: false,
                    }),
                    Config(DataField, {
                      name: "name",
                      encode: false,
                      sortType: "asUCString",
                    }),
                  ],
                }),
                Config(BindPropertyPlugin, {
                  bidirectional: true,
                  bindTo: this.getSelectedSiteVE(config),
                }),
              ],
            }),
          ],
        }),
        Config(PagingToolbar, { id: RedirectsOverviewGridBase.TOOLBAR_ID }),
      ],
      ...ConfigUtils.prepend({
        plugins: [
          Config(ContextMenuPlugin, {
            contextMenu: Config(Menu, {
              items: [
                Config(Item, {
                  iconCls: CoreIcons_properties.pencil,
                  text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_grid_redirect_edit_label,
                  handler: bind(this, this.openEditWindow),
                  ...ConfigUtils.append({
                    plugins: [
                      Config(BindPropertyPlugin, {
                        componentProperty: "disabled",
                        bindTo: config.mayNotWriteVE,
                      }),
                    ],
                  }),
                }),
                Config(Item, {
                  iconCls: CoreIcons_properties.trash_bin,
                  text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_grid_redirect_delete_label,
                  handler: bind(this, this.deleteRedirect),
                  ...ConfigUtils.append({
                    plugins: [
                      Config(BindPropertyPlugin, {
                        componentProperty: "disabled",
                        bindTo: config.mayNotWriteVE,
                      }),
                    ],
                  }),
                }),
                Config(Separator),
                Config(Item, {
                  iconCls: CoreIcons_properties.type_object,
                  text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_grid_redirect_open_label,
                  handler: bind(this, this.openRedirectTarget),
                }),
              ],
            }),
          }),
        ],
      }),
      columns: [
        Config(RedirectStatusColumn),
        Config(RedirectSourceColumn),
        Config(RedirectTargetColumn),
        Config(RedirectCreationDateColumn),
        Config(RedirectTypeColumn),
        Config(RedirectParametersColumn, {
          header: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_sourceParameters,
          dataIndex: RedirectImpl.SOURCE_PARAMETERS,
        }),
        Config(RedirectParametersColumn, {
          header: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_targetParameters,
          dataIndex: RedirectImpl.TARGET_PARAMETERS,
        }),
      ],

    }), config))());
  }
}

export default RedirectsOverviewGrid;
