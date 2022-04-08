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
import MessageBoxUtil from "@coremedia/studio-client.main.editor-components/sdk/util/MessageBoxUtil";
import Ext from "@jangaroo/ext-ts";
import ObjectUtil from "@jangaroo/ext-ts/Object";
import FieldContainer from "@jangaroo/ext-ts/form/FieldContainer";
import { as, bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectSourceParameter from "../../data/RedirectSourceParameter";
import RedirectTargetParameter from "../../data/RedirectTargetParameter";
import RedirectSourceFieldContainer from "./RedirectSourceFieldContainer";

interface RedirectSourceFieldContainerBaseConfig extends Config<FieldContainer>, Partial<Pick<RedirectSourceFieldContainerBase,
  "errorMessagesVE" |
  "sourceUrlVE" |
  "sourceUrlParametersVE"
>> {
}

/**
 * A form for editing redirects. Changes are bind to the localModel.
 * The localModel can be passed as a configuration parameter.
 */
class RedirectSourceFieldContainerBase extends FieldContainer {
  declare Config: RedirectSourceFieldContainerBaseConfig;

  #errorMessagesVE: ValueExpression = null;

  get errorMessagesVE(): ValueExpression {
    return this.#errorMessagesVE;
  }

  set errorMessagesVE(value: ValueExpression) {
    this.#errorMessagesVE = value;
  }

  #sourceUrlVE: ValueExpression = null;

  get sourceUrlVE(): ValueExpression {
    return this.#sourceUrlVE;
  }

  set sourceUrlVE(value: ValueExpression) {
    this.#sourceUrlVE = value;
  }

  #sourceUrlParametersVE: ValueExpression = null;

  get sourceUrlParametersVE(): ValueExpression {
    return this.#sourceUrlParametersVE;
  }

  set sourceUrlParametersVE(value: ValueExpression) {
    this.#sourceUrlParametersVE = value;
  }

  constructor(config: Config<RedirectSourceFieldContainer> = null) {
    super(config);
    this.sourceUrlParametersVE = config.sourceUrlParametersVE;
    config.sourceUrlVE.addChangeListener(bind(this, this.#sourceUrlChangeListener));

  }

  /**
   * When the source url changes, the listener checks if the string contains url parameters. In this case the
   * parameters  are parsed and added as {@link RedirectSourceParameter}. Additionally the value expression for the
   * source url is overwritten so that the string with the parameters is no longer part of the url.
   */
  #sourceUrlChangeListener(update: ValueExpression): void {
    const sourceUrl: string = update.getValue();
    if (sourceUrl && sourceUrl.indexOf("?") != -1) {
      const queryString = sourceUrl.substring(sourceUrl.indexOf("?") + 1);

      const queryParams = ObjectUtil.fromQueryString(queryString);
      const params = ObjectUtil.getKeys(queryParams).map((key: string): RedirectSourceParameter =>
        RedirectSourceFieldContainerBase.#convertToRedirectParameter(key, queryParams[key]),
      );

      const parameters = [].concat(this.sourceUrlParametersVE.getValue()).concat(params);
      this.sourceUrlParametersVE.setValue(parameters);

      update.setValue(sourceUrl.substr(0, sourceUrl.indexOf("?")));
    }
  }

  static #convertToRedirectParameter(key: string, entry: any): RedirectSourceParameter {
    let value = "";
    if (entry instanceof Array) {
      value = (as(entry, Array))[0] as string;
    } else {
      value = String(entry);
    }
    const init: Record<string, any> = {};
    init[RedirectTargetParameter.NAME] = key;
    init[RedirectTargetParameter.VALUE] = value;
    init[RedirectSourceParameter.OPERATOR] = RedirectSourceParameter.OPERATOR_EQUALS;
    return new RedirectSourceParameter(init);
  }

  /**
   * Opens a dialog with descriptions for the source field.
   */
  showInfoDialog(): void {
    MessageBoxUtil.showInfo(
      RedirectManagerStudioPlugin_properties.redirectmanager_editor_help_source_title,
      RedirectManagerStudioPlugin_properties.redirectmanager_editor_help_source_text,
      Ext.emptyFn,
      false,
    );
  }

}

export default RedirectSourceFieldContainerBase;
