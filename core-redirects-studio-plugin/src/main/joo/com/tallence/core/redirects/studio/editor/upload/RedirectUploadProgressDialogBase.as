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

package com.tallence.core.redirects.studio.editor.upload {
import com.coremedia.cms.editor.sdk.components.StudioDialog;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.upload.FileWrapper;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.tallence.core.redirects.studio.data.RedirectImportResponse;
import com.tallence.core.redirects.studio.util.NotificationUtil;
import com.tallence.core.redirects.studio.util.RedirectsUtil;

import ext.ProgressBar;

import flash.events.TimerEvent;
import flash.utils.Timer;

import mx.resources.ResourceManager;

use namespace editorContext;

/**
 * A window to display the import result.
 */
public class RedirectUploadProgressDialogBase extends StudioDialog {

  protected static const PROGRESS_BAR_ITEM_ID:String = "progressBar";

  private var errorMessagesVE:ValueExpression;
  private var createdRedirectsVE:ValueExpression;
  private var uploadInProgressVE:ValueExpression;

  private var progressBar:ProgressBar;
  private var timer:Timer;

  [Bindable]
  public var fileWrapper:FileWrapper;

  [Bindable]
  public var selectedSiteIdVE:ValueExpression;

  public function RedirectUploadProgressDialogBase(config:RedirectUploadProgressDialog = null) {
    super(config);
    initProgressBar();
    RedirectsUtil.uploadRedirects(config.selectedSiteIdVE.getValue(), config.fileWrapper, uploadSuccessHandler, uploadErrorHandler);
  }

  /**
   * Init the progress bar and start a timer to animate the progress bar until the redirects are imported.
   */
  private function initProgressBar():void {
    progressBar = queryById(PROGRESS_BAR_ITEM_ID) as ProgressBar;
    timer = new Timer(400);
    timer.addEventListener(TimerEvent.TIMER, updateProgressBar);
    timer.start();
  }

  /**
   * Animates the progress bar.
   */
  private function updateProgressBar(e:TimerEvent):void {
    if (progressBar.getValue() >= 1) {
      progressBar.updateProgress(0, ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_csvupload_upload_inProgress_text'), true);
    } else {
      progressBar.updateProgress(progressBar.getValue() + 0.05, ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_csvupload_upload_inProgress_text'), true);
    }
  }

  private function uploadSuccessHandler(response:RedirectImportResponse):void {
    timer.stop();
    getUploadInProgressVE().setValue(false);
    getErrorMessagesVE().setValue(response.getErrors());
    getCreatedRedirectsVE().setValue(response.getRedirects());
  }

  private function uploadErrorHandler(errorMessage:String):void {
    timer.stop();
    getUploadInProgressVE().setValue(false);
    NotificationUtil.showError(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_csvupload_upload_failed_msg') + errorMessage);
    close();
  }

  protected function getCreatedRedirectsVE():ValueExpression {
    if (!createdRedirectsVE) {
      createdRedirectsVE = ValueExpressionFactory.createFromValue([]);
    }
    return createdRedirectsVE;
  }

  protected function getRedirectsVE():ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var redirects:Array = getCreatedRedirectsVE().getValue();
      if (redirects && redirects.every(RedirectsUtil.redirectIsAccessible)) {
        return redirects;
      }
      return undefined;
    })
  }

  protected static function getKeyForErrorMessage(errorMessage:Object):String {
    return errorMessage.csvEntry + "-" + errorMessage.errorCode;
  }

  protected function getErrorMessagesVE():ValueExpression {
    if (!errorMessagesVE) {
      errorMessagesVE = ValueExpressionFactory.createFromValue([]);
    }
    return errorMessagesVE;
  }

  protected function getUploadInProgressVE():ValueExpression {
    if (!uploadInProgressVE) {
      uploadInProgressVE = ValueExpressionFactory.createFromValue(true);
    }
    return uploadInProgressVE;
  }

  protected static function hiddenValueTransformer(value:Boolean):Boolean {
    return !value;
  }

}
}
