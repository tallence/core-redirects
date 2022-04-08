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

import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import editorContext from "@coremedia/studio-client.main.editor-components/sdk/editorContext";
import Ext from "@jangaroo/ext-ts";
import Panel from "@jangaroo/ext-ts/panel/Panel";
import { as, bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import RedirectManagerStudioPlugin_properties from "../bundles/RedirectManagerStudioPlugin_properties";
import PermissionResponse from "../data/PermissionResponse";
import RedirectsUtil from "../util/RedirectsUtil";
import RedirectManagerEditor from "./RedirectManagerEditor";
import RedirectEditWindow from "./form/RedirectEditWindow";
import RedirectUploadWindow from "./upload/RedirectUploadWindow";

interface RedirectManagerEditorBaseConfig extends Config<Panel> {
}

class RedirectManagerEditorBase extends Panel {
  declare Config: RedirectManagerEditorBaseConfig;

  protected static readonly ID: string = "redirectManagerEditor";

  #selectedSiteVE: ValueExpression = null;

  #mayNotWriteVE: ValueExpression = null;

  #mayNotPublishVE: ValueExpression = null;

  #mayNotUseRegexVE: ValueExpression = null;

  #mayNotUseTargetUrlsVE: ValueExpression = null;

  constructor(config: Config<RedirectManagerEditor> = null) {
    super(config);

    this.getSelectedSiteVE().addChangeListener(bind(this, this.#resolveRights));
    //Call it in case the site is already selected
    this.#resolveRights();
  }

  #resolveRights(): void {
    const siteId = this.getSelectedSiteVE().getValue();

    //In case the request takes long or fails, the user has no rights
    this.getMayNotWriteVE().setValue(true);
    this.getMayNotPublishVE().setValue(true);
    this.getMayNotUseRegexVE().setValue(true);

    RedirectsUtil.resolvePermissions(siteId).then((response: PermissionResponse): void => {
      this.getMayNotWriteVE().setValue(!response.isMayWrite());
      this.getMayNotPublishVE().setValue(!response.isMayPublish());
      this.getMayNotUseRegexVE().setValue(!response.isMayUseRegex());
      this.getMayNotUseTargetUrlsVE().setValue(!response.isMayUseTargetUrls());
    });
  }

  static getInstance(): RedirectManagerEditor {
    return as(Ext.getCmp(RedirectManagerEditorBase.ID), RedirectManagerEditor);
  }

  protected createRedirect(): void {
    const window = new RedirectEditWindow(Config(RedirectEditWindow, {
      title: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_new_text,
      selectedSiteIdVE: this.getSelectedSiteVE(),
      mayNotPublishVE: this.#mayNotPublishVE,
      mayNotUseRegexVE: this.#mayNotUseRegexVE,
      mayNotUseTargetUrlsVE: this.#mayNotUseTargetUrlsVE,
    }));
    window.show();
  }

  protected csvUploadButtonHandler(): void {
    const dialog = new RedirectUploadWindow(
      Config(RedirectUploadWindow, { selectedSiteIdVE: this.getSelectedSiteVE() }),
    );
    dialog.show();
  }

  /**
   * Creates a ValueExpression that stores the currently selected site.
   * @return ValueExpression
   */
  protected getSelectedSiteVE(): ValueExpression {
    if (!this.#selectedSiteVE) {
      const preferredSite = editorContext._.getSitesService().getPreferredSite();
      this.#selectedSiteVE = ValueExpressionFactory.createFromValue(preferredSite ? preferredSite.getId() : "");
    }
    return this.#selectedSiteVE;
  }

  protected getMayNotWriteVE(): ValueExpression {
    if (!this.#mayNotWriteVE) {
      this.#mayNotWriteVE = ValueExpressionFactory.createFromValue(false);
    }
    return this.#mayNotWriteVE;
  }

  protected getMayNotPublishVE(): ValueExpression {
    if (!this.#mayNotPublishVE) {
      this.#mayNotPublishVE = ValueExpressionFactory.createFromValue(false);
    }
    return this.#mayNotPublishVE;
  }

  protected getMayNotUseRegexVE(): ValueExpression {
    if (!this.#mayNotUseRegexVE) {
      this.#mayNotUseRegexVE = ValueExpressionFactory.createFromValue(false);
    }
    return this.#mayNotUseRegexVE;
  }

  protected getMayNotUseTargetUrlsVE(): ValueExpression {
    if (!this.#mayNotUseTargetUrlsVE) {
      this.#mayNotUseTargetUrlsVE = ValueExpressionFactory.createFromValue(false);
    }
    return this.#mayNotUseTargetUrlsVE;
  }

  protected getSiteIsNotSelectedVE(): ValueExpression {
    return ValueExpressionFactory.createFromFunction((): boolean => {
      const siteId: string = this.getSelectedSiteVE().getValue();
      return !siteId || siteId == "";
    });
  }

}

export default RedirectManagerEditorBase;
