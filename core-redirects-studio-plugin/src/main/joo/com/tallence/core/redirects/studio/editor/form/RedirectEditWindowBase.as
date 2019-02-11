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
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.error.RemoteError;
import com.tallence.core.redirects.studio.data.Redirect;
import com.tallence.core.redirects.studio.data.RedirectImpl;
import com.tallence.core.redirects.studio.data.RedirectRepositoryImpl;
import com.tallence.core.redirects.studio.data.ValidationResponse;
import com.tallence.core.redirects.studio.util.NotificationUtil;

import ext.window.Window;

import mx.resources.ResourceManager;

use namespace editorContext;

/**
 * A window to create or edit redirects.
 */
public class RedirectEditWindowBase extends Window {

  private static var SOURCE_TYPE_DEFAULT:String = RedirectImpl.SOURCE_TYPE_PLAIN;

  private var localModel:Bean;
  private var redirect:Redirect;
  private var isValidSourceVE:ValueExpression;
  private var errorMessagesVE:ValueExpression;
  private var selectedSiteIdVE:ValueExpression;

  public function RedirectEditWindowBase(config:RedirectEditWindow = null) {
    super(config);
    redirect = config.redirect;
    selectedSiteIdVE = config.selectedSiteIdVE;
    initLocalModel();
    getLocalModel().addPropertyChangeListener(RedirectImpl.SOURCE, sourcePropertyChanged);
  }

  private function sourcePropertyChanged():void {
    var siteId:String = redirect ? redirect.getSiteId() : selectedSiteIdVE.getValue();
    var redirectId:String = redirect ? redirect.getUriPath().replace("redirect/", "").replace(siteId + "/", "") : null;
    RedirectRepositoryImpl.getInstance().validateSource(siteId, redirectId, getLocalModel().get(RedirectImpl.SOURCE))
        .then(handleValidationResponse, validationErrorHandler);
  }

  private function handleValidationResponse(response:ValidationResponse):void {
    getIsValidSourceVE().setValue(response.isValid());
    getErrorMessagesVE().setValue(response.getErrorCodes().map(function (errorCode:String):String {
      return ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_csvupload_import_error_' + errorCode);
    }));
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
      model.set(RedirectImpl.SOURCE_TYPE, redirect.getSourceType());
      model.set(RedirectImpl.REDIRECT_TYPE, redirect.getRedirectType());
      model.set(RedirectImpl.CREATION_DATE, redirect.getCreationDate());
    } else {
      model.set(RedirectImpl.SOURCE_TYPE, SOURCE_TYPE_DEFAULT);
      model.set(RedirectImpl.REDIRECT_TYPE, RedirectImpl.REDIRECT_TYPE_404);
      model.set(RedirectImpl.CREATION_DATE, new Date());
    }
  }

  /**
   * If a redirect is available, the redirects will be updates. Otherwise a new redirect is created.
   */
  protected function save():void {
    var model:Bean = getLocalModel();
    if (redirect) {
      redirect.setActive(model.get(RedirectImpl.ACTIVE));
      redirect.setTargetLink(model.get(RedirectImpl.TARGET_LINK)[0]);
      redirect.setDescription(model.get(RedirectImpl.DESCRIPTION));
      redirect.setSource(model.get(RedirectImpl.SOURCE));
      redirect.setSourceType(model.get(RedirectImpl.SOURCE_TYPE));
      redirect.setRedirectType(model.get(RedirectImpl.REDIRECT_TYPE));
    } else {
      var siteId:String = redirect ? redirect.getSiteId() : selectedSiteIdVE.getValue();
      var sourceType:String = model.get(RedirectImpl.SOURCE_TYPE);
      RedirectRepositoryImpl.getInstance().createRedirect(
          siteId,
          model.get(RedirectImpl.ACTIVE),
          model.get(RedirectImpl.TARGET_LINK)[0],
          model.get(RedirectImpl.DESCRIPTION),
          model.get(RedirectImpl.SOURCE),
          //Default value, if the input field is hidden (because of missing permissions)
          sourceType ? sourceType : SOURCE_TYPE_DEFAULT,
          model.get(RedirectImpl.REDIRECT_TYPE)
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
      return !isValid || (!model.get(RedirectImpl.SOURCE) || !model.get(RedirectImpl.TARGET_LINK) || (model.get(RedirectImpl.TARGET_LINK) as Array).length == 0);
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
