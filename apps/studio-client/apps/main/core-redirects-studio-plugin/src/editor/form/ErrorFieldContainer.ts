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
import BindComponentsPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindComponentsPlugin";
import FieldContainer from "@jangaroo/ext-ts/form/FieldContainer";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import ErrorMessage from "./ErrorMessage";

interface ErrorFieldContainerConfig extends Config<FieldContainer>, Partial<Pick<ErrorFieldContainer,
  "errorMessagesVE" |
  "propertyName"
>> {
}

class ErrorFieldContainer extends FieldContainer {
  declare Config: ErrorFieldContainerConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.form.errorFieldContainer";

  #errorMessagesVE: ValueExpression = null;

  /**
   * A value expression containing a map with error codes.
   *
   * Example: {'source': ['invalid'] }
   */
  get errorMessagesVE(): ValueExpression {
    return this.#errorMessagesVE;
  }

  set errorMessagesVE(value: ValueExpression) {
    this.#errorMessagesVE = value;
  }

  #propertyName: string = null;

  /**
   * The key used to display the error messages for the given property name.
   */
  get propertyName(): string {
    return this.#propertyName;
  }

  set propertyName(value: string) {
    this.#propertyName = value;
  }

  constructor(config: Config<ErrorFieldContainer> = null) {
    super(ConfigUtils.apply(Config(ErrorFieldContainer, {
      plugins: [
        Config(BindComponentsPlugin, {
          valueExpression: config.errorMessagesVE.extendBy(config.propertyName),
          configBeanParameterName: "errorCode",
          template: Config(ErrorMessage),
        }),
      ],
    }), config));
  }
}

export default ErrorFieldContainer;
