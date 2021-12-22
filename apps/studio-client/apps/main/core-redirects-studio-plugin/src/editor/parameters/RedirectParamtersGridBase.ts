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
import GridPanel from "@jangaroo/ext-ts/grid/Panel";
import Config from "@jangaroo/runtime/Config";
import RedirectSourceParameter from "../../data/RedirectSourceParameter";
import RedirectTargetParameter from "../../data/RedirectTargetParameter";
import RedirectParametersGrid from "./RedirectParametersGrid";

interface RedirectParamtersGridBaseConfig extends Config<GridPanel>, Partial<Pick<RedirectParamtersGridBase,
  "bindTo"
>> {
}

class RedirectParamtersGridBase extends GridPanel {
  declare Config: RedirectParamtersGridBaseConfig;

  #bindTo: ValueExpression = null;

  get bindTo(): ValueExpression {
    return this.#bindTo;
  }

  set bindTo(value: ValueExpression) {
    this.#bindTo = value;
  }

  #selectedPositionsExpression: ValueExpression = null;

  constructor(config: Config<RedirectParametersGrid> = null) {
    super(config);
    if (config.validateRedirectHandler) {
      // If the data in the grid changes, a validation of the redirect must be triggered if the grid is used for the
      // source parameters. This cannot be solved with a change listener in the edit window, since a change listener
      // does not trigger when values change within an object of a list.
      this.getStore().addListener("datachanged", config.validateRedirectHandler);
    }
  }

  protected static isEmptySelection(selection: Array<any>): boolean {
    return !selection || selection.length == 0;
  }

  protected addRow(): void {
    const init: Record<string, any> = {};
    init[RedirectTargetParameter.NAME] = "parameter";
    init[RedirectTargetParameter.VALUE] = "value";
    init[RedirectSourceParameter.OPERATOR] = RedirectSourceParameter.OPERATOR_EQUALS;
    const redirectParameterImpl = new RedirectSourceParameter(init);
    const parameters = [].concat(this.bindTo.getValue());
    parameters.push(redirectParameterImpl);
    this.bindTo.setValue(parameters);
  }

  protected deleteRow(): void {
    const selection: Array<any> = this.getSelectedPositionsExpression().getValue();
    if (selection && selection.length > 0) {
      const position: number = selection[0];
      const items = [].concat(this.bindTo.getValue());
      items.splice(position, 1);
      this.bindTo.setValue(items);
    }
  }

  protected getSelectedPositionsExpression(): ValueExpression {
    if (!this.#selectedPositionsExpression) {
      this.#selectedPositionsExpression = ValueExpressionFactory.createFromValue([]);
    }
    return this.#selectedPositionsExpression;
  }

}

export default RedirectParamtersGridBase;
