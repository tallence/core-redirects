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
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import BindValidationStatePluginBase from "./BindValidationStatePluginBase";

interface BindValidationStatePluginConfig extends Config<BindValidationStatePluginBase> {
}

class BindValidationStatePlugin extends BindValidationStatePluginBase {
  declare Config: BindValidationStatePluginConfig;

  /**
   * This plugin can be used to bind the validation state to a property editor in the redirect edit panel. A value
   * expression containing the error codes should be passed as the bindTo. If the object contains entries for the
   * specified property name, the validation state is set to error.
   */
  constructor(config: Config<BindValidationStatePlugin> = null) {
    super((()=> ConfigUtils.apply(Config(BindValidationStatePlugin, {
      componentProperty: "validationState",
      transformer: bind(this, this.computeValidationState),

    }), config))());
  }
}

export default BindValidationStatePlugin;
