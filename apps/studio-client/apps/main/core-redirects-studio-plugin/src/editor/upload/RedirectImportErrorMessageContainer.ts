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
import DisplayFieldSkin from "@coremedia/studio-client.ext.ui-components/skins/DisplayFieldSkin";
import Container from "@jangaroo/ext-ts/container/Container";
import DisplayField from "@jangaroo/ext-ts/form/field/Display";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";

interface RedirectImportErrorMessageContainerConfig extends Config<Container>, Partial<Pick<RedirectImportErrorMessageContainer,
  "errorMessage"
>> {
}

class RedirectImportErrorMessageContainer extends Container {
  declare Config: RedirectImportErrorMessageContainerConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.upload.redirectImportErrorMessageContainer";

  constructor(config: Config<RedirectImportErrorMessageContainer> = null) {
    super(ConfigUtils.apply(Config(RedirectImportErrorMessageContainer, {
      items: [
        Config(DisplayField, {
          value: config.errorMessage.csvEntry,
          ui: DisplayFieldSkin.BOLD.getSkin(),
        }),
        Config(DisplayField, { value: RedirectManagerStudioPlugin_properties[("redirectmanager_editor_actions_csvupload_import_error_" + config.errorMessage.errorCode)] }),
      ],

    }), config));
  }

  #errorMessage: any = null;

  get errorMessage(): any {
    return this.#errorMessage;
  }

  set errorMessage(value: any) {
    this.#errorMessage = value;
  }
}

export default RedirectImportErrorMessageContainer;
