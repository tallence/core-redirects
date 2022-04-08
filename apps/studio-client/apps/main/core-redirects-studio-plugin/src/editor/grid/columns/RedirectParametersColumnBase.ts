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

import Column from "@jangaroo/ext-ts/grid/column/Column";
import Config from "@jangaroo/runtime/Config";
import RedirectTargetParameter from "../../../data/RedirectTargetParameter";
import RedirectParametersColumn from "./RedirectParametersColumn";

interface RedirectParametersColumnBaseConfig extends Config<Column> {
}

class RedirectParametersColumnBase extends Column {
  declare Config: RedirectParametersColumnBaseConfig;

  constructor(config: Config<RedirectParametersColumn> = null) {
    super(config);
  }

  protected typeColRenderer(parameters: Array<any>): string {
    let value: string = parameters && parameters.length > 0 ? "?" : "";

    value = value + parameters.map((parameter: RedirectTargetParameter): string =>
      parameter.getName() + "=" + parameter.getValue(),
    ).join("&");

    return value;
  }

}

export default RedirectParametersColumnBase;
