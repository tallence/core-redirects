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
import com.coremedia.cms.editor.sdk.components.html5.BrowsePlugin;
import com.coremedia.cms.editor.sdk.editorContext;
import com.coremedia.cms.editor.sdk.upload.FileWrapper;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.coremedia.ui.logging.Logger;
import com.coremedia.ui.util.EventUtil;

use namespace editorContext;

/**
 * A window to select import file for redirects.
 */
public class RedirectUploadWindowBase extends StudioDialog {

  private var validationExpression:ValueExpression;
  private var fileListVE:ValueExpression;
  private var selectedSiteIdVE:ValueExpression;

  public function RedirectUploadWindowBase(config:RedirectUploadWindow = null) {
    selectedSiteIdVE = config.selectedSiteIdVE;
    super(config);
  }

  /**
   * The upload button handler, converts the selected files to FileWrapper objects.
   * @param browsePlugin the browse plugin used for the file selection and contains the file selection.
   */
  protected function uploadButtonHandler(browsePlugin:BrowsePlugin):void {
    var fileWrappers:Array = [];
    var fileList:* = browsePlugin.getFileList();
    for (var i:int = 0; i < fileList.length; i++) {
      fileWrappers.push(new FileWrapper(fileList.item(i)));
    }
    this.handleDrop(fileWrappers);
  }

  /**
   * Fired when a file object has been dropped on the target drop area.
   * The file drop plugin fire an event for each file that is dropped
   * and the corresponding action is handled here.
   */
  protected function handleDrop(files:Array):void {
    EventUtil.invokeLater(function ():void {
      //otherwise the progress bar does not appear :(
      if (files.length > 0) {
        //Currently only one document per upload is allowed.
        getFileListVE().setValue([files[0]]);
      }
    });
  }

  protected function getUploadButtonDisabledExpression():ValueExpression {
    if (!this.validationExpression) {
      this.validationExpression = ValueExpressionFactory.createFromFunction(function ():Boolean {
        var files:Array = getFileListVE().getValue();

        if (files.length == 1) {
          var file:FileWrapper = files[0];
          var name:String = file.getFile().name;

          if (file.getMimeType() != "text/csv") {
            //only log a warning, the mimeType seems to be different in some browsers.
            Logger.warn('The fileType of uploaded file is invalid: ' + file.getMimeType());
          }
          //Check for the file extension instead: does it end with '.csv'?
          return (name.length - name.indexOf('.csv')) != 4;
        }

        return true;
      });
    }
    return this.validationExpression;
  }

  protected function getFileListVE():ValueExpression {
    if (!fileListVE) {
      fileListVE = ValueExpressionFactory.createFromValue([]);
    }
    return fileListVE;
  }

  protected function removeFiles():void {
    getFileListVE().setValue([]);
  }

  protected function okPressed():void {
    var fileWrapper:FileWrapper = getFileListVE().getValue()[0];
    new RedirectUploadProgressDialog(RedirectUploadProgressDialog({
      fileWrapper: fileWrapper,
      selectedSiteIdVE: selectedSiteIdVE
    })).show();
    close();
  }

}
}