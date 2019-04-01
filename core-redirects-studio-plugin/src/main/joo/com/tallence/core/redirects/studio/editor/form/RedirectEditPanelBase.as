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
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.Bean;
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.tallence.core.redirects.studio.data.RedirectImpl;

import ext.Ext;
import ext.container.Container;

use namespace editorContext;

/**
 * A form for editing redirects. Changes are bind to the localModel.
 * The localModel can be passed as a configuration parameter.
 */
public class RedirectEditPanelBase extends Container {

  public function RedirectEditPanelBase(config:RedirectEditPanel = null) {
    super(config);
  }

  /**
   * Opens a dialog with descriptions for the source field.
   */
  public function showInfoDialog():void {
    MessageBoxUtil.showInfo(
        resourceManager.getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_help_source_title'),
        resourceManager.getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_help_source_text'),
        Ext.emptyFn,
        false
    );
  }

  /**
   * Creates a ValueExpression which returns an array of css modifiers.
   * If no content is linked to the redirect, the field must be highlighted.
   *
   * @param localModel the bean.
   * @param errorCodesVE value expression containing the error codes
   * @return an array of css modifiers.
   */
  protected function getSourceFieldModifiers(localModel:Bean, errorCodesVE:ValueExpression):ValueExpression {
    return ValueExpressionFactory.createFromFunction(function ():Array {
      var modifiers:Array = [];
      var source:String = localModel.get(RedirectImpl.SOURCE);
      var isValid:Boolean = errorCodesVE.extendBy(RedirectImpl.SOURCE);
      if (!source || source.length == 0 || !isValid) {
        modifiers.push("empty");
      }
      return modifiers;
    });
  }

  protected static function getBindTo(localModel:Bean, propertyName:String):ValueExpression {
    return ValueExpressionFactory.create(propertyName, localModel);
  }

}
}
