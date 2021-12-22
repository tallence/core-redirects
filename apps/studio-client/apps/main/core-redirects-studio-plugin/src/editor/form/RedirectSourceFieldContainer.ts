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
import StatefulTextField from "@coremedia/studio-client.ext.ui-components/components/StatefulTextField";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import HBoxLayout from "@jangaroo/ext-ts/layout/container/HBox";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImpl from "../../data/RedirectImpl";
import BindValidationStatePlugin from "../../plugins/BindValidationStatePlugin";
import RedirectSourceFieldContainerBase from "./RedirectSourceFieldContainerBase";

interface RedirectSourceFieldContainerConfig extends Config<RedirectSourceFieldContainerBase> {
}

class RedirectSourceFieldContainer extends RedirectSourceFieldContainerBase {
  declare Config: RedirectSourceFieldContainerConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.form.redirectSourceFieldContainer";

  constructor(config: Config<RedirectSourceFieldContainer> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectSourceFieldContainer, {
      fieldLabel: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_source,
      items: [
        Config(StatefulTextField, {
          width: 490,
          checkChangeBuffer: 400,
          ...ConfigUtils.append({
            plugins: [
              Config(BindValidationStatePlugin, {
                bindTo: config.errorMessagesVE,
                propertyName: RedirectImpl.SOURCE,
              }),
              Config(BindPropertyPlugin, {
                bidirectional: true,
                bindTo: config.sourceUrlVE,
              }),
            ],
          }),
        }),
        Config(IconButton, {
          iconCls: CoreIcons_properties.help,
          tooltip: RedirectManagerStudioPlugin_properties.redirectmanager_editor_help_tooltip,
          handler: bind(this, this.showInfoDialog),
        }),
      ],
      layout: Config(HBoxLayout),

    }), config))());
  }
}

export default RedirectSourceFieldContainer;
