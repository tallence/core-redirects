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

package com.tallence.core.redirects.studio.editor.form {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.error.RemoteError;
import com.tallence.core.redirects.studio.data.Redirect;
import com.tallence.core.redirects.studio.data.RedirectImpl;
import com.tallence.core.redirects.studio.data.ValidationResponse;
import com.tallence.core.redirects.studio.util.NotificationUtil;
import com.tallence.core.redirects.studio.util.RedirectsUtil;

import ext.window.Window;

import mx.resources.ResourceManager;

/**
 * A window to create or edit redirects.
 */
public class RedirectEditWindowBase extends Window {

  private static var SOURCE_TYPE_DEFAULT:String = RedirectImpl.SOURCE_TYPE_PLAIN;
  private static const MESSAGE_BOX_DECISION_POSITIVE:String = "yes";

  private var localModel:Bean;
  private var redirect:Redirect;
  private var isValidSourceVE:ValueExpression;
  private var errorMessagesVE:ValueExpression;
  private var selectedSiteIdVE:ValueExpression;
  private var mayNotPublishVE:ValueExpression;

  public function RedirectEditWindowBase(config:RedirectEditWindow = null) {
    super(config);
    redirect = config.redirect;
    selectedSiteIdVE = config.selectedSiteIdVE;
    mayNotPublishVE = config.mayNotPublishVE;
    initLocalModel();
    initValidationChangeListeners();
  }

  /**
   * Initializes the change listeners to validate the redirect. If the lifecycle satus of the target change, the
   * redirect must be validated again.
   */
  private function initValidationChangeListeners():void {
    getLocalModel().addPropertyChangeListener(RedirectImpl.ACTIVE, validateRedirect);
    getLocalModel().addPropertyChangeListener(RedirectImpl.SOURCE, validateRedirect);
    getLocalModel().addPropertyChangeListener(RedirectImpl.TARGET_LINK, validateRedirect);
    getLocalModel().addPropertyChangeListener(RedirectImpl.TARGET_URL, validateRedirect);
    var lifecycleStatusVE:ValueExpression = ValueExpressionFactory.create(RedirectImpl.TARGET_LINK, getLocalModel()).extendBy("0", "lifecycleStatus");
    lifecycleStatusVE.addChangeListener(validateRedirect);
    validateRedirect();
  }

  protected function validateRedirect():void {
    var siteId:String = redirect ? redirect.getSiteId() : selectedSiteIdVE.getValue();
    var redirectId:String = redirect ? redirect.getUriPath().replace("redirect/", "").replace(siteId + "/", "") : "";
    var targetLink:Content = getLocalModel().get(RedirectImpl.TARGET_LINK)[0];
    var targetId:String = targetLink ? targetLink.getId() : "";
    var targetUrl:String = getLocalModel().get(RedirectImpl.TARGET_URL);
    var active:Boolean = getLocalModel().get(RedirectImpl.ACTIVE);
    var sourceParameters:Array = getLocalModel().get(RedirectImpl.SOURCE_PARAMETERS);
    RedirectsUtil
        .validateRedirect(siteId, redirectId, getLocalModel().get(RedirectImpl.SOURCE), targetId, targetUrl, active, sourceParameters)
        .then(handleValidationResponse, validationErrorHandler);
  }

  private function handleValidationResponse(response:ValidationResponse):void {
    getIsValidSourceVE().setValue(response.isValid());
    getErrorMessagesVE().setValue(response.getErrorCodes());
  }

  private static function validationErrorHandler(error:RemoteError):void {
    NotificationUtil.showError(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_validation_error') + error);
  }

  /**
   * If a redirect is available, the local model will be initialized.
   */
  private function initLocalModel():void {
    var model:Bean = getLocalModel();
    if (redirect) {
      model.set(RedirectImpl.ACTIVE, redirect.isActive());
      model.set(RedirectImpl.TARGET_LINK, redirect.getTargetLink() ? [redirect.getTargetLink()] : []);
      model.set(RedirectImpl.DESCRIPTION, redirect.getDescription());
      model.set(RedirectImpl.SOURCE, redirect.getSource());
      model.set(RedirectImpl.TARGET_URL, redirect.getTargetUrl());
      model.set(RedirectImpl.SOURCE_TYPE, redirect.getSourceType());
      model.set(RedirectImpl.REDIRECT_TYPE, redirect.getRedirectType());
      model.set(RedirectImpl.CREATION_DATE, redirect.getCreationDate());
      model.set(RedirectImpl.SOURCE_PARAMETERS, [].concat(redirect.getSourceParameters()));
      model.set(RedirectImpl.TARGET_PARAMETERS, [].concat(redirect.getTargetParameters()));
    } else {
      //Set default values. The redirect is active by default, if the user has publication rights
      model.set(RedirectImpl.ACTIVE, !mayNotPublishVE.getValue());
      model.set(RedirectImpl.SOURCE, "/");
      model.set(RedirectImpl.SOURCE_TYPE, SOURCE_TYPE_DEFAULT);
      model.set(RedirectImpl.REDIRECT_TYPE, RedirectImpl.REDIRECT_TYPE_404);
      model.set(RedirectImpl.CREATION_DATE, new Date());
      model.set(RedirectImpl.SOURCE_PARAMETERS, []);
      model.set(RedirectImpl.TARGET_PARAMETERS, []);
    }
  }

  /**
   * If a redirect is available, the redirects will be updated. Otherwise a new redirect is created.
   */
  protected function save():void {
    var model:Bean = getLocalModel();

    var source:String = model.get(RedirectImpl.SOURCE);
    var redirectType:String = model.get(RedirectImpl.REDIRECT_TYPE);

    var endsWithEvenId: Boolean = source && source.match(".+-.*[02468]");

    if (source && endsWithEvenId && RedirectImpl.REDIRECT_TYPE_404 == redirectType) {

      MessageBoxUtil.showDecision(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_decision_title'),
              ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_decision_use404Type'),
              ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_decision_ok'),
              //when clicked on ok:
              function (decision: String):void {
                processSaveWithDecision(decision, RedirectImpl.REDIRECT_TYPE_ALWAYS);
              });
    } else if (source && !endsWithEvenId && RedirectImpl.REDIRECT_TYPE_ALWAYS == redirectType) {
      MessageBoxUtil.showDecision(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_decision_title'),
              ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_decision_useAlwaysType'),
              ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_decision_ok'),
              //when clicked on ok:
              function (decision: String):void {
                processSaveWithDecision(decision, RedirectImpl.REDIRECT_TYPE_404);
              });
    } else {
      processSave();
    }
  }

  private function processSaveWithDecision(decision:String, redirectType:String): void {
    if (decision == MESSAGE_BOX_DECISION_POSITIVE) {
      getLocalModel().set(RedirectImpl.REDIRECT_TYPE, redirectType);
    }
    processSave();
  }

  private function processSave(): void {

    var model:Bean = getLocalModel();

    if (redirect) {
      redirect.setActive(model.get(RedirectImpl.ACTIVE));
      redirect.setTargetLink(model.get(RedirectImpl.TARGET_LINK)[0]);
      redirect.setTargetUrl(model.get(RedirectImpl.TARGET_URL));
      redirect.setDescription(model.get(RedirectImpl.DESCRIPTION));
      redirect.setSource(model.get(RedirectImpl.SOURCE));
      redirect.setSourceType(model.get(RedirectImpl.SOURCE_TYPE));
      redirect.setRedirectType(model.get(RedirectImpl.REDIRECT_TYPE));
      redirect.setSourceParameters(model.get(RedirectImpl.SOURCE_PARAMETERS));
      redirect.setTargetParameters(model.get(RedirectImpl.TARGET_PARAMETERS));
    } else {
      var siteId:String = redirect ? redirect.getSiteId() : selectedSiteIdVE.getValue();
      var sourceType:String = model.get(RedirectImpl.SOURCE_TYPE);
      RedirectsUtil.createRedirect(
          siteId,
          model.get(RedirectImpl.ACTIVE),
          model.get(RedirectImpl.TARGET_LINK)[0],
          model.get(RedirectImpl.TARGET_URL),
          model.get(RedirectImpl.DESCRIPTION),
          model.get(RedirectImpl.SOURCE),
          //Default value, if the input field is hidden (because of missing permissions)
          sourceType ? sourceType : SOURCE_TYPE_DEFAULT,
          model.get(RedirectImpl.REDIRECT_TYPE),
          model.get(RedirectImpl.SOURCE_PARAMETERS),
          model.get(RedirectImpl.TARGET_PARAMETERS)
      );
    }
    close();
  }

  /**
   * Returns a ValueExpression to enable or disable the save button.
   * If a redirect is invalid, the button should be disabled.
   * @return the value expression.
   */
  protected function getSaveButtonDisabledVE():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Boolean {
      var model:Bean = getLocalModel();
      var isValid:Boolean = getIsValidSourceVE().getValue();

      var active:Boolean = model.get(RedirectImpl.ACTIVE);
      var invalidPublishAttempt: Boolean = active && mayNotPublishVE.getValue();

      return invalidPublishAttempt || !isValid;
    })
  }

  /**
   * Returns a local model representing a redirect. Initializes the linked content with an empty array, otherwise
   * SingleLinkEditor could not initialize the content binding.
   * @return
   */
  protected function getLocalModel():Bean {
    if (!localModel) {
      localModel = beanFactory.createLocalBean({});
      localModel.set(RedirectImpl.TARGET_LINK, []);
    }
    return localModel;
  }

  protected function getIsValidSourceVE():ValueExpression {
    if (!isValidSourceVE) {
      isValidSourceVE = ValueExpressionFactory.createFromValue(true);
    }
    return isValidSourceVE;
  }

  protected function getErrorMessagesVE():ValueExpression {
    if (!errorMessagesVE) {
      errorMessagesVE = ValueExpressionFactory.createFromValue([]);
    }
    return errorMessagesVE;
  }

}
}
