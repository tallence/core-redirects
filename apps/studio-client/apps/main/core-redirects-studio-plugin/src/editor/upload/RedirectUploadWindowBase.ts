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

import Logger from "@coremedia/studio-client.client-core-impl/logging/Logger";
import ValueExpression from "@coremedia/studio-client.client-core/data/ValueExpression";
import ValueExpressionFactory from "@coremedia/studio-client.client-core/data/ValueExpressionFactory";
import EventUtil from "@coremedia/studio-client.client-core/util/EventUtil";
import StudioDialog from "@coremedia/studio-client.ext.base-components/dialogs/StudioDialog";
import BrowsePlugin from "@coremedia/studio-client.main.editor-components/sdk/components/html5/BrowsePlugin";
import FileWrapper from "@coremedia/studio-client.main.editor-components/sdk/upload/FileWrapper";
import Config from "@jangaroo/runtime/Config";
import RedirectUploadProgressDialog from "./RedirectUploadProgressDialog";
import RedirectUploadWindow from "./RedirectUploadWindow";

interface RedirectUploadWindowBaseConfig extends Config<StudioDialog> {
}

/**
 * A window to select import file for redirects.
 */
class RedirectUploadWindowBase extends StudioDialog {
  declare Config: RedirectUploadWindowBaseConfig;

  #validationExpression: ValueExpression = null;

  #fileListVE: ValueExpression = null;

  #selectedSiteIdVE: ValueExpression = null;

  constructor(config: Config<RedirectUploadWindow> = null) {
    super((()=>{
      this.#selectedSiteIdVE = config.selectedSiteIdVE;
      return config;
    })());
  }

  /**
   * The upload button handler, converts the selected files to FileWrapper objects.
   * @param browsePlugin the browse plugin used for the file selection and contains the file selection.
   */
  protected uploadButtonHandler(browsePlugin: any): void {
    const fileWrappers = [];
    const fileList: any = browsePlugin.getFileList();
    for (let i = 0; i < fileList.length; i++) {
      fileWrappers.push(new FileWrapper(fileList.item(i)));
    }
    this.handleDrop(fileWrappers);
  }

  /**
   * Fired when a file object has been dropped on the target drop area.
   * The file drop plugin fire an event for each file that is dropped
   * and the corresponding action is handled here.
   */
  protected handleDrop(files: Array<any>): void {
    EventUtil.invokeLater((): void => {
      //otherwise the progress bar does not appear :(
      if (files.length > 0) {
        //Currently only one document per upload is allowed.
        this.getFileListVE().setValue([files[0]]);
      }
    });
  }

  protected getUploadButtonDisabledExpression(): ValueExpression {
    if (!this.#validationExpression) {
      this.#validationExpression = ValueExpressionFactory.createFromFunction((): boolean => {
        const files: Array<any> = this.getFileListVE().getValue();

        if (files.length == 1) {
          const file: FileWrapper = files[0];
          const name: string = file.getFile().name;

          if (file.getMimeType() != "text/csv") {
            //only log a warning, the mimeType seems to be different in some browsers.
            Logger.warn("The fileType of uploaded file is invalid: " + file.getMimeType());
          }
          //Check for the file extension instead: does it end with '.csv'?
          return (name.length - name.indexOf(".csv")) != 4;
        }

        return true;
      });
    }
    return this.#validationExpression;
  }

  protected getFileListVE(): ValueExpression {
    if (!this.#fileListVE) {
      this.#fileListVE = ValueExpressionFactory.createFromValue([]);
    }
    return this.#fileListVE;
  }

  protected removeFiles(): void {
    this.getFileListVE().setValue([]);
  }

  protected okPressed(): void {
    const fileWrapper: FileWrapper = this.getFileListVE().getValue()[0];
    new RedirectUploadProgressDialog(Config(RedirectUploadProgressDialog, {
      fileWrapper: fileWrapper,
      selectedSiteIdVE: this.#selectedSiteIdVE,
    })).show();
    this.close();
  }

}

export default RedirectUploadWindowBase;
