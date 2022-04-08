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
import IconButton from "@coremedia/studio-client.ext.ui-components/components/IconButton";
import BindListPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindListPlugin";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import BindSelectionPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindSelectionPlugin";
import CellEditPlugin from "@coremedia/studio-client.ext.ui-components/plugins/CellEditPlugin";
import DataField from "@coremedia/studio-client.ext.ui-components/store/DataField";
import TextField from "@jangaroo/ext-ts/form/field/Text";
import Column from "@jangaroo/ext-ts/grid/column/Column";
import FitLayout from "@jangaroo/ext-ts/layout/container/Fit";
import RowSelectionModel from "@jangaroo/ext-ts/selection/RowModel";
import Separator from "@jangaroo/ext-ts/toolbar/Separator";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import { AnyFunction } from "@jangaroo/runtime/types";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectSourceParameter from "../../data/RedirectSourceParameter";
import RedirectTargetParameter from "../../data/RedirectTargetParameter";
import RedirectParamtersGridBase from "./RedirectParamtersGridBase";

interface RedirectParametersGridConfig extends Config<RedirectParamtersGridBase>, Partial<Pick<RedirectParametersGrid,
  "errorMessagesVE" |
  "validateRedirectHandler"
>> {
}

class RedirectParametersGrid extends RedirectParamtersGridBase {
  declare Config: RedirectParametersGridConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.parameters.redirectParametersGrid";

  constructor(config: Config<RedirectParametersGrid> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectParametersGrid, {
      maxHeight: 150,
      minHeight: 25,
      margin: "0 0 0 130",
      width: "500px",
      height: "100px",

      tbar: Config(Toolbar, {
        items: [
          Config(IconButton, {
            iconCls: CoreIcons_properties.remove,
            disabled: true,
            handler: bind(this, this.deleteRow),
            plugins: [
              Config(BindPropertyPlugin, {
                componentProperty: "disabled",
                bindTo: this.getSelectedPositionsExpression(),
                transformer: RedirectParamtersGridBase.isEmptySelection,
              }),
            ],
          }),
          Config(Separator),
          Config(IconButton, {
            iconCls: CoreIcons_properties.add,
            handler: bind(this, this.addRow),
          }),
        ],
      }),

      ...ConfigUtils.append({
        plugins: [
          Config(BindSelectionPlugin, { selectedPositions: this.getSelectedPositionsExpression() }),
          Config(CellEditPlugin, { clicksToEdit: 2 }),
          Config(BindListPlugin, {
            bindTo: config.bindTo,
            fields: [
              Config(DataField, { name: RedirectTargetParameter.NAME }),
              Config(DataField, { name: RedirectTargetParameter.VALUE }),
              Config(DataField, { name: RedirectSourceParameter.OPERATOR }),
            ],
          }),
        ],
      }),
      columns: [
        Config(Column, {
          header: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_parameterName,
          dataIndex: RedirectTargetParameter.NAME,
          editor: Config(TextField, { allowBlank: false }),
        }),
        Config(Column, {
          header: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_parameterValue,
          flex: 1,
          dataIndex: RedirectTargetParameter.VALUE,
          editor: Config(TextField, { allowBlank: false }),
        }),
      ],
      layout: Config(FitLayout),
      selModel: new RowSelectionModel({}),

    }), config))());
  }

  #errorMessagesVE: ValueExpression = null;

  get errorMessagesVE(): ValueExpression {
    return this.#errorMessagesVE;
  }

  set errorMessagesVE(value: ValueExpression) {
    this.#errorMessagesVE = value;
  }

  #validateRedirectHandler: AnyFunction = null;

  get validateRedirectHandler(): AnyFunction {
    return this.#validateRedirectHandler;
  }

  set validateRedirectHandler(value: AnyFunction) {
    this.#validateRedirectHandler = value;
  }
}

export default RedirectParametersGrid;
