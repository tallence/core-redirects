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
import CoreIcons_properties from "@coremedia/studio-client.core-icons/CoreIcons_properties";
import IconButton from "@coremedia/studio-client.ext.ui-components/components/IconButton";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import ButtonSkin from "@coremedia/studio-client.ext.ui-components/skins/ButtonSkin";
import ToolbarSkin from "@coremedia/studio-client.ext.ui-components/skins/ToolbarSkin";
import CollapsibleFormPanel from "@coremedia/studio-client.main.editor-components/sdk/premular/CollapsibleFormPanel";
import Container from "@jangaroo/ext-ts/container/Container";
import VBoxLayout from "@jangaroo/ext-ts/layout/container/VBox";
import Panel from "@jangaroo/ext-ts/panel/Panel";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../bundles/RedirectManagerStudioPlugin_properties";
import RedirectManagerEditorBase from "./RedirectManagerEditorBase";
import RedirectsOverviewGrid from "./grid/RedirectsOverviewGrid";

interface RedirectManagerEditorConfig extends Config<RedirectManagerEditorBase> {
}

class RedirectManagerEditor extends RedirectManagerEditorBase {
  declare Config: RedirectManagerEditorConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.redirectManagerEditorBase";

  constructor(config: Config<RedirectManagerEditor> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectManagerEditor, {
      id: RedirectManagerEditorBase.ID,
      title: RedirectManagerStudioPlugin_properties.redirectmanager_button_text,
      closable: true,
      iconCls: "tallence-icons tallence-icons--redirects",
      itemId: RedirectManagerEditorBase.ID,
      layout: "fit",
      items: [
        Config(Panel, {
          layout: Config(VBoxLayout, { align: "stretch" }),
          items: [
            Config(Container, {
              cls: "redirectmanager-center-panel",
              items: [
                Config(CollapsibleFormPanel, {
                  title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_list_text,
                  collapsible: false,
                  items: [
                    Config(RedirectsOverviewGrid, {
                      height: "100%",
                      selectedSiteIdVE: this.getSelectedSiteVE(),
                      mayNotWriteVE: this.getMayNotWriteVE(),
                      mayNotPublishVE: this.getMayNotPublishVE(),
                      mayNotUseRegexVE: this.getMayNotUseRegexVE(),
                      mayNotUseTargetUrlsVE: this.getMayNotUseTargetUrlsVE(),
                      siteIsNotSelectedVE: this.getSiteIsNotSelectedVE(),
                    }),
                  ],
                }),
              ],
            }),
          ],
          tbar: Config(Toolbar, {
            ui: ToolbarSkin.WORKAREA.getSkin(),
            items: [
              Config(IconButton, {
                itemId: "add",
                tooltip: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_new_tooltip,
                iconCls: CoreIcons_properties.create_content,
                handler: bind(this, this.createRedirect),
                ...ConfigUtils.append({
                  plugins: [
                    Config(BindPropertyPlugin, {
                      componentProperty: "disabled",
                      bindTo: this.getMayNotWriteVE(),
                    }),
                  ],
                }),
              }),
              Config(Separator),
              Config(IconButton, {
                itemId: "upload",
                tooltip: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_tooltip,
                iconCls: CoreIcons_properties.upload,
                handler: bind(this, this.csvUploadButtonHandler),
                ...ConfigUtils.append({
                  plugins: [
                    Config(BindPropertyPlugin, {
                      componentProperty: "disabled",
                      bindTo: this.getMayNotWriteVE(),
                    }),
                  ],
                }),
              }),
            ],
            defaultType: IconButton.xtype,
            defaults: Config<IconButton>({
              scale: "medium",
              ui: ButtonSkin.WORKAREA.getSkin(),
            }),
          }),
        }),

      ],

    }), config))());
  }
}

export default RedirectManagerEditor;
