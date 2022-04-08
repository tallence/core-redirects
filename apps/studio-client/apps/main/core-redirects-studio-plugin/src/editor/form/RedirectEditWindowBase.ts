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

import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import Bean from "@coremedia/studio-client.client-core/data/Bean";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import RemoteError from "@coremedia/studio-client.client-core/data/error/RemoteError";
import MessageBoxUtil from "@coremedia/studio-client.main.editor-components/sdk/util/MessageBoxUtil";
import Window from "@jangaroo/ext-ts/window/Window";
import { bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import Redirect from "../../data/Redirect";
import RedirectImpl from "../../data/RedirectImpl";
import ValidationResponse from "../../data/ValidationResponse";
import NotificationUtil from "../../util/NotificationUtil";
import RedirectsUtil from "../../util/RedirectsUtil";
import RedirectEditWindow from "./RedirectEditWindow";

interface RedirectEditWindowBaseConfig extends Config<Window> {
}

/**
 * A window to create or edit redirects.
 */
class RedirectEditWindowBase extends Window {
  declare Config: RedirectEditWindowBaseConfig;

  static #SOURCE_TYPE_DEFAULT: string = RedirectImpl.SOURCE_TYPE_PLAIN;

  static readonly #MESSAGE_BOX_DECISION_POSITIVE: string = "yes";

  #localModel: Bean = null;

  #redirect: Redirect = null;

  #isValidSourceVE: ValueExpression = null;

  #errorMessagesVE: ValueExpression = null;

  #selectedSiteIdVE: ValueExpression = null;

  #mayNotPublishVE: ValueExpression = null;

  constructor(config: Config<RedirectEditWindow> = null) {
    super(config);
    this.#redirect = config.redirect;
    this.#selectedSiteIdVE = config.selectedSiteIdVE;
    this.#mayNotPublishVE = config.mayNotPublishVE;
    this.#initLocalModel();
    this.#initValidationChangeListeners();
  }

  /**
   * Initializes the change listeners to validate the redirect. If the lifecycle satus of the target change, the
   * redirect must be validated again.
   */
  #initValidationChangeListeners(): void {
    this.getLocalModel().addPropertyChangeListener(RedirectImpl.ACTIVE, bind(this, this.validateRedirect));
    this.getLocalModel().addPropertyChangeListener(RedirectImpl.SOURCE, bind(this, this.validateRedirect));
    this.getLocalModel().addPropertyChangeListener(RedirectImpl.TARGET_LINK, bind(this, this.validateRedirect));
    this.getLocalModel().addPropertyChangeListener(RedirectImpl.TARGET_URL, bind(this, this.validateRedirect));
    const lifecycleStatusVE = ValueExpressionFactory.create(RedirectImpl.TARGET_LINK, this.getLocalModel()).extendBy("0", "lifecycleStatus");
    lifecycleStatusVE.addChangeListener(bind(this, this.validateRedirect));
    this.validateRedirect();
  }

  protected validateRedirect(): void {
    const siteId: string = this.#redirect ? this.#redirect.getSiteId() : this.#selectedSiteIdVE.getValue();
    const redirectId: string = this.#redirect ? this.#redirect.getUriPath().replace("redirect/", "").replace(siteId + "/", "") : "";
    const targetLink: Content = this.getLocalModel().get(RedirectImpl.TARGET_LINK)[0];
    const targetId: string = targetLink ? targetLink.getId() : "";
    const targetUrl: string = this.getLocalModel().get(RedirectImpl.TARGET_URL);
    const active: boolean = this.getLocalModel().get(RedirectImpl.ACTIVE);
    const sourceParameters: Array<any> = this.getLocalModel().get(RedirectImpl.SOURCE_PARAMETERS);
    RedirectsUtil
      .validateRedirect(siteId, redirectId, this.getLocalModel().get(RedirectImpl.SOURCE), targetId, targetUrl, active, sourceParameters)
      .then(bind(this, this.#handleValidationResponse), RedirectEditWindowBase.#validationErrorHandler);
  }

  #handleValidationResponse(response: ValidationResponse): void {
    this.getIsValidSourceVE().setValue(response.isValid());
    this.getErrorMessagesVE().setValue(response.getErrorCodes());
  }

  static #validationErrorHandler(error: RemoteError): void {
    NotificationUtil.showError(RedirectManagerStudioPlugin_properties.redirectmanager_validation_error + error);
  }

  /**
   * If a redirect is available, the local model will be initialized.
   */
  #initLocalModel(): void {
    const model = this.getLocalModel();
    if (this.#redirect) {
      model.set(RedirectImpl.ACTIVE, this.#redirect.isActive());
      model.set(RedirectImpl.TARGET_LINK, this.#redirect.getTargetLink() ? [this.#redirect.getTargetLink()] : []);
      model.set(RedirectImpl.DESCRIPTION, this.#redirect.getDescription());
      model.set(RedirectImpl.SOURCE, this.#redirect.getSource());
      model.set(RedirectImpl.TARGET_URL, this.#redirect.getTargetUrl());
      model.set(RedirectImpl.SOURCE_TYPE, this.#redirect.getSourceType());
      model.set(RedirectImpl.REDIRECT_TYPE, this.#redirect.getRedirectType());
      model.set(RedirectImpl.CREATION_DATE, this.#redirect.getCreationDate());
      model.set(RedirectImpl.SOURCE_PARAMETERS, [].concat(this.#redirect.getSourceParameters()));
      model.set(RedirectImpl.TARGET_PARAMETERS, [].concat(this.#redirect.getTargetParameters()));
    } else {
      //Set default values. The redirect is active by default, if the user has publication rights
      model.set(RedirectImpl.ACTIVE, !this.#mayNotPublishVE.getValue());
      model.set(RedirectImpl.SOURCE, "/");
      model.set(RedirectImpl.SOURCE_TYPE, RedirectEditWindowBase.#SOURCE_TYPE_DEFAULT);
      model.set(RedirectImpl.REDIRECT_TYPE, RedirectImpl.REDIRECT_TYPE_404);
      model.set(RedirectImpl.CREATION_DATE, new Date());
      model.set(RedirectImpl.SOURCE_PARAMETERS, []);
      model.set(RedirectImpl.TARGET_PARAMETERS, []);
    }
  }

  /**
   * If a redirect is available, the redirects will be updated. Otherwise a new redirect is created.
   */
  protected save(): void {
    const model = this.getLocalModel();

    const source: string = model.get(RedirectImpl.SOURCE);
    const redirectType: string = model.get(RedirectImpl.REDIRECT_TYPE);

    const endsWithEvenId: boolean = source && source.match(".+-.*[02468]") != null;

    if (source && endsWithEvenId && RedirectImpl.REDIRECT_TYPE_404 == redirectType) {

      MessageBoxUtil.showDecision(RedirectManagerStudioPlugin_properties.redirectmanager_decision_title,
        RedirectManagerStudioPlugin_properties.redirectmanager_decision_use404Type,
        RedirectManagerStudioPlugin_properties.redirectmanager_decision_ok,
        //when clicked on ok:
        (decision: string): void =>
          this.#processSaveWithDecision(decision, RedirectImpl.REDIRECT_TYPE_ALWAYS),
      );
    } else if (source && !endsWithEvenId && RedirectImpl.REDIRECT_TYPE_ALWAYS == redirectType) {
      MessageBoxUtil.showDecision(RedirectManagerStudioPlugin_properties.redirectmanager_decision_title,
        RedirectManagerStudioPlugin_properties.redirectmanager_decision_useAlwaysType,
        RedirectManagerStudioPlugin_properties.redirectmanager_decision_ok,
        //when clicked on ok:
        (decision: string): void =>
          this.#processSaveWithDecision(decision, RedirectImpl.REDIRECT_TYPE_404),
      );
    } else {
      this.#processSave();
    }
  }

  #processSaveWithDecision(decision: string, redirectType: string): void {
    if (decision == RedirectEditWindowBase.#MESSAGE_BOX_DECISION_POSITIVE) {
      this.getLocalModel().set(RedirectImpl.REDIRECT_TYPE, redirectType);
    }
    this.#processSave();
  }

  #processSave(): void {

    const model = this.getLocalModel();

    if (this.#redirect) {
      this.#redirect.setActive(model.get(RedirectImpl.ACTIVE));
      this.#redirect.setTargetLink(model.get(RedirectImpl.TARGET_LINK)[0]);
      this.#redirect.setTargetUrl(model.get(RedirectImpl.TARGET_URL));
      this.#redirect.setDescription(model.get(RedirectImpl.DESCRIPTION));
      this.#redirect.setSource(model.get(RedirectImpl.SOURCE));
      this.#redirect.setSourceType(model.get(RedirectImpl.SOURCE_TYPE));
      this.#redirect.setRedirectType(model.get(RedirectImpl.REDIRECT_TYPE));
      this.#redirect.setSourceParameters(model.get(RedirectImpl.SOURCE_PARAMETERS));
      this.#redirect.setTargetParameters(model.get(RedirectImpl.TARGET_PARAMETERS));
    } else {
      const siteId: string = this.#redirect ? this.#redirect.getSiteId() : this.#selectedSiteIdVE.getValue();
      const sourceType: string = model.get(RedirectImpl.SOURCE_TYPE);
      RedirectsUtil.createRedirect(
        siteId,
        model.get(RedirectImpl.ACTIVE),
        model.get(RedirectImpl.TARGET_LINK)[0],
        model.get(RedirectImpl.TARGET_URL),
        model.get(RedirectImpl.DESCRIPTION),
        model.get(RedirectImpl.SOURCE),
        //Default value, if the input field is hidden (because of missing permissions)
        sourceType ? sourceType : RedirectEditWindowBase.#SOURCE_TYPE_DEFAULT,
        model.get(RedirectImpl.REDIRECT_TYPE),
        model.get(RedirectImpl.SOURCE_PARAMETERS),
        model.get(RedirectImpl.TARGET_PARAMETERS),
      );
    }
    this.close();
  }

  /**
   * Returns a ValueExpression to enable or disable the save button.
   * If a redirect is invalid, the button should be disabled.
   * @return the value expression.
   */
  protected getSaveButtonDisabledVE(): ValueExpression {
    return ValueExpressionFactory.createFromFunction((): boolean => {
      const model = this.getLocalModel();
      const isValid: boolean = this.getIsValidSourceVE().getValue();

      const active: boolean = model.get(RedirectImpl.ACTIVE);
      const invalidPublishAttempt: boolean = active && this.#mayNotPublishVE.getValue();

      return invalidPublishAttempt || !isValid;
    });
  }

  /**
   * Returns a local model representing a redirect. Initializes the linked content with an empty array, otherwise
   * SingleLinkEditor could not initialize the content binding.
   * @return
   */
  protected getLocalModel(): Bean {
    if (!this.#localModel) {
      this.#localModel = beanFactory._.createLocalBean({});
      this.#localModel.set(RedirectImpl.TARGET_LINK, []);
    }
    return this.#localModel;
  }

  protected getIsValidSourceVE(): ValueExpression {
    if (!this.#isValidSourceVE) {
      this.#isValidSourceVE = ValueExpressionFactory.createFromValue(true);
    }
    return this.#isValidSourceVE;
  }

  protected getErrorMessagesVE(): ValueExpression {
    if (!this.#errorMessagesVE) {
      this.#errorMessagesVE = ValueExpressionFactory.createFromValue([]);
    }
    return this.#errorMessagesVE;
  }

}

export default RedirectEditWindowBase;
