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
import RedirectManagerStudioPlugin_properties from "../../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImpl from "../../../data/RedirectImpl";
import RedirectTypeColumn from "./RedirectTypeColumn";

interface RedirectTypeColumnBaseConfig extends Config<Column> {
}

class RedirectTypeColumnBase extends Column {
  declare Config: RedirectTypeColumnBaseConfig;

  constructor(config: Config<RedirectTypeColumn> = null) {
    super(config);
  }

  protected typeColRenderer(value: string): string {
    if (value.toUpperCase() === RedirectImpl.REDIRECT_TYPE_ALWAYS) {
      return RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_type_value_0;
    } else if (value.toUpperCase() === RedirectImpl.REDIRECT_TYPE_404) {
      return RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_type_value_1;
    } else {
      return RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_type_invalid;
    }
  }

}

export default RedirectTypeColumnBase;
