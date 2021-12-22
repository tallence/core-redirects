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
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImpl from "../../data/RedirectImpl";
import IdenticalRedirectsGridBase from "./IdenticalRedirectsGridBase";
import RedirectParametersColumn from "./columns/RedirectParametersColumn";
import RedirectStatusColumn from "./columns/RedirectStatusColumn";
import RedirectTargetColumn from "./columns/RedirectTargetColumn";

interface IdenticalRedirectsGridConfig extends Config<IdenticalRedirectsGridBase> {
}

class IdenticalRedirectsGrid extends IdenticalRedirectsGridBase {
  declare Config: IdenticalRedirectsGridConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.grid.identicalRedirectsGrid";

  constructor(config: Config<IdenticalRedirectsGrid> = null) {
    super(ConfigUtils.apply(Config(IdenticalRedirectsGrid, {

      columns: [
        Config(RedirectStatusColumn),
        Config(RedirectTargetColumn),
        Config(RedirectParametersColumn, {
          header: RedirectManagerStudioPlugin_properties.redirectmanager_editor_field_sourceParameters,
          dataIndex: RedirectImpl.SOURCE_PARAMETERS,
        }),
      ],

    }), config));
  }
}

export default IdenticalRedirectsGrid;
