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
import ContentTypes_properties from "@coremedia/studio-client.cap-base-models/content/ContentTypes_properties";
import StudioAppsImpl from "@coremedia/studio-client.app-context-models/apps/StudioAppsImpl";
import studioApps from "@coremedia/studio-client.app-context-models/apps/studioApps";
import CopyResourceBundleProperties
  from "@coremedia/studio-client.main.editor-components/configuration/CopyResourceBundleProperties";
import RegisterRestResource from "@coremedia/studio-client.main.editor-components/configuration/RegisterRestResource";
import AddTabbedDocumentFormsPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/plugins/AddTabbedDocumentFormsPlugin";
import TabbedDocumentFormDispatcher
  from "@coremedia/studio-client.main.editor-components/sdk/premular/TabbedDocumentFormDispatcher";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import resourceManager from "@jangaroo/runtime/l10n/resourceManager";
import RedirectManagerStudioPluginBase from "./RedirectManagerStudioPluginBase";
import RedirectContentTypes_properties from "./bundles/RedirectContentTypes_properties";
import RedirectImpl from "./data/RedirectImpl";
import RedirectsImpl from "./data/RedirectsImpl";
import RedirectForm from "./studioform/RedirectForm";
import IEditorContext from "@coremedia/studio-client.main.editor-components/sdk/IEditorContext";
import {cast} from "@jangaroo/runtime";
import OpenRedirectManagerEditorAction from "./action/OpenRedirectManagerEditorAction";
import WorkArea from "@coremedia/studio-client.main.editor-components/sdk/desktop/WorkArea";
import WorkAreaTabTypesPlugin
  from "@coremedia/studio-client.main.editor-components/sdk/desktop/WorkAreaTabTypesPlugin";
import ComponentBasedWorkAreaTabType
  from "@coremedia/studio-client.main.editor-components/sdk/desktop/ComponentBasedWorkAreaTabType";
import RedirectManagerEditor from "./editor/RedirectManagerEditor";

interface RedirectManagerStudioPluginConfig extends Config<RedirectManagerStudioPluginBase> {
}

class RedirectManagerStudioPlugin extends RedirectManagerStudioPluginBase {
  declare Config: RedirectManagerStudioPluginConfig;

  override init(editorContext: IEditorContext): void {
    super.init(editorContext);

    cast(StudioAppsImpl, studioApps._).getSubAppLauncherRegistry().registerSubAppLauncher("redirect-manager", (): void => {
      const openTagsAction = new OpenRedirectManagerEditorAction();
      openTagsAction.execute();
    });
  }

  constructor(config: Config<RedirectManagerStudioPlugin> = null) {
    super(ConfigUtils.apply(Config(RedirectManagerStudioPlugin, {

      rules: [

        Config(TabbedDocumentFormDispatcher, {
          plugins: [
            Config(AddTabbedDocumentFormsPlugin, {
              documentTabPanels: [
                Config(RedirectForm, { itemId: "Redirect" }),
              ],
            }),
          ],
        }),

        Config(WorkArea, {
          plugins: [
            Config(WorkAreaTabTypesPlugin, {
              tabTypes: [
                new ComponentBasedWorkAreaTabType({ tabComponent: Config(RedirectManagerEditor, { closable: true }) }),
              ],
            }),
          ],
        }),

      ],

      configuration: [

        new RegisterRestResource({ beanClass: RedirectsImpl }),
        new RegisterRestResource({ beanClass: RedirectImpl }),

        new CopyResourceBundleProperties({
          destination: resourceManager.getResourceBundle(null, ContentTypes_properties),
          source: resourceManager.getResourceBundle(null, RedirectContentTypes_properties),
        }),

      ],

    }), config));
  }
}

export default RedirectManagerStudioPlugin;
