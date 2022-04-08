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
import SpacingBEMEntities from "@coremedia/studio-client.ext.ui-components/bem/SpacingBEMEntities";
import CollapsiblePanel from "@coremedia/studio-client.ext.ui-components/components/panel/CollapsiblePanel";
import BindComponentsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindComponentsPlugin";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import VerticalSpacingPlugin from "@coremedia/studio-client.ext.ui-components/plugins/VerticalSpacingPlugin";
import ButtonSkin from "@coremedia/studio-client.ext.ui-components/skins/ButtonSkin";
import ContainerSkin from "@coremedia/studio-client.ext.ui-components/skins/ContainerSkin";
import DisplayFieldSkin from "@coremedia/studio-client.ext.ui-components/skins/DisplayFieldSkin";
import WindowSkin from "@coremedia/studio-client.ext.ui-components/skins/WindowSkin";
import BrowsePlugin from "@coremedia/studio-client.main.editor-components/sdk/components/html5/BrowsePlugin";
import FileDropPlugin from "@coremedia/studio-client.main.editor-components/sdk/upload/FileDropPlugin";
import FileContainer from "@coremedia/studio-client.main.editor-components/sdk/upload/dialog/FileContainer";
import Button from "@jangaroo/ext-ts/button/Button";
import Container from "@jangaroo/ext-ts/container/Container";
import DisplayField from "@jangaroo/ext-ts/form/field/Display";
import HBoxLayout from "@jangaroo/ext-ts/layout/container/HBox";
import VBoxLayout from "@jangaroo/ext-ts/layout/container/VBox";
import Fill from "@jangaroo/ext-ts/toolbar/Fill";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectCsvHeaderDescriptionContainer from "./RedirectCsvHeaderDescriptionContainer";
import RedirectUploadWindowBase from "./RedirectUploadWindowBase";

interface RedirectUploadWindowConfig extends Config<RedirectUploadWindowBase>, Partial<Pick<RedirectUploadWindow,
  "selectedSiteIdVE"
>> {
}

class RedirectUploadWindow extends RedirectUploadWindowBase {
  declare Config: RedirectUploadWindowConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.upload.redirectUploadWindow";

  #selectedSiteIdVE: ValueExpression = null;

  get selectedSiteIdVE(): ValueExpression {
    return this.#selectedSiteIdVE;
  }

  set selectedSiteIdVE(value: ValueExpression) {
    this.#selectedSiteIdVE = value;
  }

  constructor(config: Config<RedirectUploadWindow> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectUploadWindow, {
      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_dropin_title,
      width: 800,
      minWidth: 800,
      height: 500,
      minHeight: 500,
      resizable: false,
      modal: true,
      constrainHeader: true,
      ui: WindowSkin.GRID_200.getSkin(),
      plugins: [
        Config(VerticalSpacingPlugin, { modifier: SpacingBEMEntities.VERTICAL_SPACING_MODIFIER_200 }),
      ],
      items: [
        Config(CollapsiblePanel, {
          title: "Example",
          itemId: "test",
          collapsible: false,
          items: [
            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header,
              ui: DisplayFieldSkin.BOLD.getSkin(),
            }),
            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example,
              ui: DisplayFieldSkin.EMBEDDED.getSkin(),
            }),
            Config(Container, {
              margin: "10 0 0 0",
              items: [
                Config(Container, {
                  items: [
                    Config(RedirectCsvHeaderDescriptionContainer, {
                      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_active_title,
                      text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_active_text,
                    }),
                    Config(RedirectCsvHeaderDescriptionContainer, {
                      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_sourceUrlType_title,
                      text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_sourceUrlType_text,
                    }),
                  ],
                }),
                Config(Container, {
                  margin: "0 0 0 10",
                  items: [
                    Config(RedirectCsvHeaderDescriptionContainer, {
                      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_sourceUrl_title,
                      text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_sourceUrl_text,
                    }),
                    Config(RedirectCsvHeaderDescriptionContainer, {
                      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_targetLink_title,
                      text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_targetLink_text,
                    }),
                  ],
                }),
                Config(Container, {
                  margin: "0 0 0 10",
                  items: [
                    Config(RedirectCsvHeaderDescriptionContainer, {
                      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_redirectType_title,
                      text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_redirectType_text,
                    }),
                    Config(RedirectCsvHeaderDescriptionContainer, {
                      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_description_title,
                      text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_description_text,
                    }),
                  ],
                }),
              ],
              layout: Config(HBoxLayout),
            }),
            Config(RedirectCsvHeaderDescriptionContainer, {
              title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_sourceParameters_title,
              text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_sourceParameters_text,
            }),
            Config(RedirectCsvHeaderDescriptionContainer, {
              title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_targetParameters_title,
              text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_example_header_targetParameters_text,
            }),
          ],
        }),
        Config(Container, {
          flex: 1,
          minHeight: 80,
          ui: ContainerSkin.FRAME_GRID_200.getSkin(),
          items: [
            Config(Button, {
              text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_text,
              handler: bind(this, this.uploadButtonHandler),
              scale: "small",
              ui: ButtonSkin.INLINE.getSkin(),
              plugins: [
                Config(BrowsePlugin, {
                  enableFileDrop: true,
                  multiple: false,
                  dropEl: this.el,
                }),
              ],
            }),
            Config(DisplayField, {
              value: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_dropin_label,
              ui: DisplayFieldSkin.ITALIC.getSkin(),
            }),
          ],
          layout: Config(VBoxLayout, {
            align: "center",
            pack: "center",
          }),
          plugins: [
            Config(FileDropPlugin, { dropHandler: bind(this, this.handleDrop) }),
          ],
        }),
        Config(Container, {
          items: [
          ],
          plugins: [
            Config(BindComponentsPlugin, {
              valueExpression: this.getFileListVE(),
              configBeanParameterName: "file",
              template: Config(FileContainer, { removeFileHandler: bind(this, this.removeFiles) }),
            }),
            Config(VerticalSpacingPlugin, { modifier: SpacingBEMEntities.VERTICAL_SPACING_MODIFIER_200 }),
          ],
        }),
      ],
      layout: Config(VBoxLayout, { align: "stretch" }),
      fbar: Config(Toolbar, {
        ...{ enableFocusableContainer: false },
        items: [
          Config(Fill),
          Config(Button, {
            text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_text,
            scale: "small",
            handler: bind(this, this.okPressed),
            ui: ButtonSkin.FOOTER_PRIMARY.getSkin(),
            plugins: [
              Config(BindPropertyPlugin, {
                componentProperty: "disabled",
                bindTo: this.getUploadButtonDisabledExpression(),
              }),
            ],
          }),
          Config(Button, {
            text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_cancel_button_text,
            scale: "small",
            handler: bind(this, this.close),
            ui: ButtonSkin.FOOTER_SECONDARY.getSkin(),
          }),
        ],
      }),

    }), config))());
  }
}

export default RedirectUploadWindow;
