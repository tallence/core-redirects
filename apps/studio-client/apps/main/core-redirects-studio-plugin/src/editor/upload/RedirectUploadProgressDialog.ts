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
import SpacingBEMEntities from "@coremedia/studio-client.ext.ui-components/bem/SpacingBEMEntities";
import BindComponentsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindComponentsPlugin";
import BindListPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindListPlugin";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import VerticalSpacingPlugin from "@coremedia/studio-client.ext.ui-components/plugins/VerticalSpacingPlugin";
import ButtonSkin from "@coremedia/studio-client.ext.ui-components/skins/ButtonSkin";
import DisplayFieldSkin from "@coremedia/studio-client.ext.ui-components/skins/DisplayFieldSkin";
import WindowSkin from "@coremedia/studio-client.ext.ui-components/skins/WindowSkin";
import DataField from "@coremedia/studio-client.ext.ui-components/store/DataField";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import ProgressBar from "@jangaroo/ext-ts/ProgressBar";
import Button from "@jangaroo/ext-ts/button/Button";
import Container from "@jangaroo/ext-ts/container/Container";
import DisplayField from "@jangaroo/ext-ts/form/field/Display";
import GridPanel from "@jangaroo/ext-ts/grid/Panel";
import VBoxLayout from "@jangaroo/ext-ts/layout/container/VBox";
import Fill from "@jangaroo/ext-ts/toolbar/Fill";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import TableView from "@jangaroo/ext-ts/view/Table";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImpl from "../../data/RedirectImpl";
import RedirectParametersColumn from "../grid/columns/RedirectParametersColumn";
import RedirectSourceColumn from "../grid/columns/RedirectSourceColumn";
import RedirectStatusColumn from "../grid/columns/RedirectStatusColumn";
import RedirectTargetColumn from "../grid/columns/RedirectTargetColumn";
import RedirectTypeColumn from "../grid/columns/RedirectTypeColumn";
import RedirectImportErrorMessageContainer from "./RedirectImportErrorMessageContainer";
import RedirectUploadProgressDialogBase from "./RedirectUploadProgressDialogBase";

interface RedirectUploadProgressDialogConfig extends Config<RedirectUploadProgressDialogBase> {
}

class RedirectUploadProgressDialog extends RedirectUploadProgressDialogBase {
  declare Config: RedirectUploadProgressDialogConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.upload.redirectUploadProgressDialog";

  constructor(config: Config<RedirectUploadProgressDialog> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectUploadProgressDialog, {
      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_text,
      modal: true,
      width: 800,
      minWidth: 800,
      minHeight: 155,
      closable: false,
      resizable: false,
      constrainHeader: true,
      ui: WindowSkin.GRID_200.getSkin(),
      plugins: [
        Config(VerticalSpacingPlugin, { modifier: SpacingBEMEntities.VERTICAL_SPACING_MODIFIER_200 }),
      ],
      items: [
        Config(ProgressBar, {
          itemId: RedirectUploadProgressDialogBase.PROGRESS_BAR_ITEM_ID,
          plugins: [
            Config(BindPropertyPlugin, {
              componentProperty: "hidden",
              bindTo: this.getUploadInProgressVE(),
              transformer: RedirectUploadProgressDialogBase.hiddenValueTransformer,
            }),
          ],
        }),

        Config(Container, {
          plugins: [
            Config(BindPropertyPlugin, {
              componentProperty: "hidden",
              bindTo: this.getUploadInProgressVE(),
            }),
          ],
          items: [
            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_import_result_title,
              padding: "0 0 5 0",
              ui: DisplayFieldSkin.BOLD.getSkin(),
            }),
            Config(GridPanel, {
              forceFit: true,
              enableColumnHide: false,
              enableColumnMove: false,
              draggable: false,
              plugins: [
                Config(BindListPlugin, {
                  bindTo: this.getRedirectsVE(),
                  fields: [
                    Config(DataField, { name: RedirectImpl.ACTIVE }),
                    Config(DataField, { name: RedirectImpl.SOURCE }),
                    Config(DataField, { name: RedirectImpl.TARGET_LINK_NAME }),
                    Config(DataField, { name: RedirectImpl.REDIRECT_TYPE }),
                    Config(DataField, { name: RedirectImpl.SOURCE_PARAMETERS }),
                    Config(DataField, { name: RedirectImpl.TARGET_PARAMETERS }),
                  ],
                }),
              ],
              columns: [
                Config(RedirectStatusColumn),
                Config(RedirectSourceColumn),
                Config(RedirectTargetColumn),
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
              viewConfig: Config(TableView, {
                stripeRows: true,
                maxHeight: 200,
                scrollable: true,
                trackOver: true,
                loadMask: true,
                deferEmptyText: false,
                emptyText: RedirectManagerStudioPlugin_properties.redirectmanager_editor_list_loading,
              }),
            }),

            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_import_result_not_created_msg,
              padding: "0 0 5 0",
              ui: DisplayFieldSkin.BOLD.getSkin(),
            }),
            Config(Container, {
              maxHeight: 300,
              scrollable: true,
              plugins: [
                Config(BindComponentsPlugin, {
                  configBeanParameterName: "errorMessage",
                  getKey: RedirectUploadProgressDialogBase.getKeyForErrorMessage,
                  valueExpression: this.getErrorMessagesVE(),
                  template: Config(RedirectImportErrorMessageContainer),
                }),
              ],
            }),

          ],
        }),
      ],
      layout: Config(VBoxLayout, { align: "stretch" }),
      fbar: Config(Toolbar, {
        ...{ enableFocusableContainer: false },
        items: [
          Config(Fill),
          Config(Button, {
            text: Editor_properties.dialog_defaultCloseButton_text,
            scale: "small",
            handler: bind(this, this.close),
            ui: ButtonSkin.FOOTER_PRIMARY.getSkin(),
            plugins: [
              Config(BindPropertyPlugin, {
                componentProperty: "disabled",
                bindTo: this.getUploadInProgressVE(),
              }),
            ],
          }),
        ],
      }),

    }), config))());
  }
}

export default RedirectUploadProgressDialog;
