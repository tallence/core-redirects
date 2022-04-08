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
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImpl from "../../../data/RedirectImpl";

interface RedirectSourceColumnConfig extends Config<Column> {
}

class RedirectSourceColumn extends Column {
  declare Config: RedirectSourceColumnConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.grid.redirectSourceColumn";

  constructor(config: Config<RedirectSourceColumn> = null) {
    super(ConfigUtils.apply(Config(RedirectSourceColumn, {
      header: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_source,
      width: 100,
      sortable: true,
      dataIndex: RedirectImpl.SOURCE,
    }), config));
  }
}

export default RedirectSourceColumn;
