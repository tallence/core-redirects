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
import RedirectManagerStudioPlugin_properties from "../../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImpl from "../../../data/RedirectImpl";
import RedirectTypeColumnBase from "./RedirectTypeColumnBase";

interface RedirectTypeColumnConfig extends Config<RedirectTypeColumnBase> {
}

class RedirectTypeColumn extends RedirectTypeColumnBase {
  declare Config: RedirectTypeColumnConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.grid.redirectTypeColumn";

  constructor(config: Config<RedirectTypeColumn> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectTypeColumn, {
      header: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_type,
      renderer: bind(this, this.typeColRenderer),
      dataIndex: RedirectImpl.REDIRECT_TYPE,
      width: 100,
      sortable: false,
    }), config))());
  }
}

export default RedirectTypeColumn;
