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
import BindPropertyPlugin from "@coremedia/studio-client.ext.ui-components/plugins/BindPropertyPlugin";
import ButtonSkin from "@coremedia/studio-client.ext.ui-components/skins/ButtonSkin";
import Editor_properties from "@coremedia/studio-client.main.editor-components/Editor_properties";
import Button from "@jangaroo/ext-ts/button/Button";
import Fill from "@jangaroo/ext-ts/toolbar/Fill";
import Toolbar from "@jangaroo/ext-ts/toolbar/Toolbar";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import ConfigUtils from "@jangaroo/runtime/ConfigUtils";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import Redirect from "../../data/Redirect";
import RedirectEditPanel from "./RedirectEditPanel";
import RedirectEditWindowBase from "./RedirectEditWindowBase";

interface RedirectEditWindowConfig extends Config<RedirectEditWindowBase>, Partial<Pick<RedirectEditWindow,
  "redirect" |
  "selectedSiteIdVE" |
  "mayNotPublishVE" |
  "mayNotUseRegexVE" |
  "mayNotUseTargetUrlsVE"
>> {
}

/*
The Windows is not "modal" because the editor might drag a document from the workarea and drop it to the redirect-target-field  */
class RedirectEditWindow extends RedirectEditWindowBase {
  declare Config: RedirectEditWindowConfig;

  static override readonly xtype: string = "com.tallence.core.redirects.studio.editor.form.redirectEditWindow";

  constructor(config: Config<RedirectEditWindow> = null) {
    super((()=> ConfigUtils.apply(Config(RedirectEditWindow, {
      autoScroll: true,
      width: "700px",
      items: [
        Config(RedirectEditPanel, {
          localModel: this.getLocalModel(),
          mayNotUseRegexVE: config.mayNotUseRegexVE,
          mayNotUseTargetUrlsVE: config.mayNotUseTargetUrlsVE,
          mayNotPublishVE: config.mayNotPublishVE,
          errorMessagesVE: this.getErrorMessagesVE(),
          selectedSiteIdVE: config.selectedSiteIdVE,
          validateRedirectHandler: bind(this, this.validateRedirect),
          redirect: config.redirect,
        }),
      ],
      fbar: Config(Toolbar, {
        items: [
          Config(Fill),
          Config(Button, {
            ui: ButtonSkin.FOOTER_PRIMARY.getSkin(),
            text: RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_save_text,
            handler: bind(this, this.save),
            scale: "small",
            plugins: [
              Config(BindPropertyPlugin, {
                componentProperty: "disabled",
                bindTo: this.getSaveButtonDisabledVE(),
              }),
            ],
          }),
          Config(Button, {
            ui: ButtonSkin.FOOTER_SECONDARY.getSkin(),
            text: Editor_properties.dialog_defaultCancelButton_text,
            handler: bind(this, this.close),
            scale: "small",
          }),
        ],
      }),

    }), config))());
  }

  #redirect: Redirect = null;

  get redirect(): Redirect {
    return this.#redirect;
  }

  set redirect(value: Redirect) {
    this.#redirect = value;
  }

  #selectedSiteIdVE: ValueExpression = null;

  get selectedSiteIdVE(): ValueExpression {
    return this.#selectedSiteIdVE;
  }

  set selectedSiteIdVE(value: ValueExpression) {
    this.#selectedSiteIdVE = value;
  }

  #mayNotPublishVE: ValueExpression = null;

  get mayNotPublishVE(): ValueExpression {
    return this.#mayNotPublishVE;
  }

  set mayNotPublishVE(value: ValueExpression) {
    this.#mayNotPublishVE = value;
  }

  #mayNotUseRegexVE: ValueExpression = null;

  get mayNotUseRegexVE(): ValueExpression {
    return this.#mayNotUseRegexVE;
  }

  set mayNotUseRegexVE(value: ValueExpression) {
    this.#mayNotUseRegexVE = value;
  }

  #mayNotUseTargetUrlsVE: ValueExpression = null;

  get mayNotUseTargetUrlsVE(): ValueExpression {
    return this.#mayNotUseTargetUrlsVE;
  }

  set mayNotUseTargetUrlsVE(value: ValueExpression) {
    this.#mayNotUseTargetUrlsVE = value;
  }
}

export default RedirectEditWindow;
