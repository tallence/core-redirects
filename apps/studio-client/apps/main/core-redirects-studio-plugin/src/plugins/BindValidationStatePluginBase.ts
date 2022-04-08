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

import ValidationState from "@coremedia/studio-client.ext.ui-components/mixins/ValidationState";
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import Config from "@jangaroo/runtime/Config";
import BindValidationStatePlugin from "./BindValidationStatePlugin";

interface BindValidationStatePluginBaseConfig extends Config<BindPropertyPlugin>, Partial<Pick<BindValidationStatePluginBase,
  "propertyName"
>> {
}

class BindValidationStatePluginBase extends BindPropertyPlugin {
  declare Config: BindValidationStatePluginBaseConfig;

  #propertyName: string = null;

  get propertyName(): string {
    return this.#propertyName;
  }

  set propertyName(value: string) {
    this.#propertyName = value;
  }

  constructor(config: Config<BindValidationStatePlugin> = null) {
    super((()=>{
      this.propertyName = config.propertyName;
      return config;
    })());
  }

  protected computeValidationState(errorCodes: any): ValidationState {
    return errorCodes[this.propertyName] ? ValidationState.ERROR : null;
  }
}

export default BindValidationStatePluginBase;
