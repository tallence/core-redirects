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
import StudioDialog from "@coremedia/studio-client.ext.base-components/dialogs/StudioDialog";
import FileWrapper from "@coremedia/studio-client.main.editor-components/sdk/upload/FileWrapper";
import ProgressBar from "@jangaroo/ext-ts/ProgressBar";
import TimerEvent from "@jangaroo/jooflash-core/flash/events/TimerEvent";
import Timer from "@jangaroo/jooflash-core/flash/utils/Timer";
import { as, bind } from "@jangaroo/runtime";
import Config from "@jangaroo/runtime/Config";
import RedirectManagerStudioPlugin_properties from "../../bundles/RedirectManagerStudioPlugin_properties";
import RedirectImportResponse from "../../data/RedirectImportResponse";
import NotificationUtil from "../../util/NotificationUtil";
import RedirectsUtil from "../../util/RedirectsUtil";
import RedirectUploadProgressDialog from "./RedirectUploadProgressDialog";

interface RedirectUploadProgressDialogBaseConfig extends Config<StudioDialog>, Partial<Pick<RedirectUploadProgressDialogBase,
  "fileWrapper" |
  "selectedSiteIdVE"
>> {
}

/**
 * A window to display the import result.
 */
class RedirectUploadProgressDialogBase extends StudioDialog {
  declare Config: RedirectUploadProgressDialogBaseConfig;

  protected static readonly PROGRESS_BAR_ITEM_ID: string = "progressBar";

  #errorMessagesVE: ValueExpression = null;

  #createdRedirectsVE: ValueExpression = null;

  #uploadInProgressVE: ValueExpression = null;

  #progressBar: ProgressBar = null;

  #timer: Timer = null;

  #fileWrapper: FileWrapper = null;

  get fileWrapper(): FileWrapper {
    return this.#fileWrapper;
  }

  set fileWrapper(value: FileWrapper) {
    this.#fileWrapper = value;
  }

  #selectedSiteIdVE: ValueExpression = null;

  get selectedSiteIdVE(): ValueExpression {
    return this.#selectedSiteIdVE;
  }

  set selectedSiteIdVE(value: ValueExpression) {
    this.#selectedSiteIdVE = value;
  }

  constructor(config: Config<RedirectUploadProgressDialog> = null) {
    super(config);
    this.#initProgressBar();
    RedirectsUtil.uploadRedirects(config.selectedSiteIdVE.getValue(), config.fileWrapper, bind(this, this.#uploadSuccessHandler), bind(this, this.#uploadErrorHandler));
  }

  /**
   * Init the progress bar and start a timer to animate the progress bar until the redirects are imported.
   */
  #initProgressBar(): void {
    this.#progressBar = as(this.queryById(RedirectUploadProgressDialogBase.PROGRESS_BAR_ITEM_ID), ProgressBar);
    this.#timer = new Timer(400);
    this.#timer.addEventListener(TimerEvent.TIMER, bind(this, this.#updateProgressBar));
    this.#timer.start();
  }

  /**
   * Animates the progress bar.
   */
  #updateProgressBar(e: TimerEvent): void {
    if (this.#progressBar.getValue() >= 1) {
      this.#progressBar.updateProgress(0, RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_upload_inProgress_text, true);
    } else {
      this.#progressBar.updateProgress(this.#progressBar.getValue() + 0.05, RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_upload_inProgress_text, true);
    }
  }

  #uploadSuccessHandler(response: RedirectImportResponse): void {
    this.#timer.stop();
    this.getUploadInProgressVE().setValue(false);
    this.getErrorMessagesVE().setValue(response.getErrors());
    this.getCreatedRedirectsVE().setValue(response.getRedirects());
  }

  #uploadErrorHandler(errorMessage: string): void {
    this.#timer.stop();
    this.getUploadInProgressVE().setValue(false);
    NotificationUtil.showError(RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_csvupload_upload_failed_msg + errorMessage);
    this.close();
  }

  protected getCreatedRedirectsVE(): ValueExpression {
    if (!this.#createdRedirectsVE) {
      this.#createdRedirectsVE = ValueExpressionFactory.createFromValue([]);
    }
    return this.#createdRedirectsVE;
  }

  protected getRedirectsVE(): ValueExpression {
    return ValueExpressionFactory.createFromFunction((): Array<any> => {
      const redirects: Array<any> = this.getCreatedRedirectsVE().getValue();
      if (redirects && redirects.every(RedirectsUtil.redirectIsAccessible)) {
        return redirects;
      }
      return undefined;
    });
  }

  protected static getKeyForErrorMessage(errorMessage: any): string {
    return errorMessage.csvEntry + "-" + errorMessage.errorCode;
  }

  protected getErrorMessagesVE(): ValueExpression {
    if (!this.#errorMessagesVE) {
      this.#errorMessagesVE = ValueExpressionFactory.createFromValue([]);
    }
    return this.#errorMessagesVE;
  }

  protected getUploadInProgressVE(): ValueExpression {
    if (!this.#uploadInProgressVE) {
      this.#uploadInProgressVE = ValueExpressionFactory.createFromValue(true);
    }
    return this.#uploadInProgressVE;
  }

  protected static hiddenValueTransformer(value: boolean): boolean {
    return !value;
  }

}

export default RedirectUploadProgressDialogBase;
