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

import WorkArea from "@coremedia/studio-client.main.editor-components/sdk/desktop/WorkArea";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Action from "@jangaroo/ext-ts/Action";
import Panel from "@jangaroo/ext-ts/panel/Panel";
import { as } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import RedirectManagerEditor from "../editor/RedirectManagerEditor";
import RedirectManagerEditorBase from "../editor/RedirectManagerEditorBase";
import OpenRedirectManagerEditorAction from "./OpenRedirectManagerEditorAction";

interface OpenRedirectManagerEditorActionBaseConfig extends Config<Action> {
}

class OpenRedirectManagerEditorActionBase extends Action {
  declare Config: OpenRedirectManagerEditorActionBaseConfig;

  constructor(config: Config<OpenRedirectManagerEditorAction> = null) {
    config.handler = OpenRedirectManagerEditorActionBase.openRedirectAdmin;
    super(config);
  }

  static openRedirectAdmin(): void {
    const workArea = as(editorContext._.getWorkArea(), WorkArea);
    let redirectManagerTab = RedirectManagerEditorBase.getInstance();

    if (!redirectManagerTab) {
      const workAreaTabType = workArea.getTabTypeById(RedirectManagerEditor.xtype);
      workAreaTabType.createTab(null, (tab: Panel): void => {
        redirectManagerTab = as(tab, RedirectManagerEditor);
        workArea.addTab(workAreaTabType, redirectManagerTab);
      });
    }

    workArea.setActiveTab(redirectManagerTab);
  }
}

export default OpenRedirectManagerEditorActionBase;
