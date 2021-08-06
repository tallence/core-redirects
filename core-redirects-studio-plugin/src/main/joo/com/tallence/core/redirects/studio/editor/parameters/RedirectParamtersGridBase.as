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

package com.tallence.core.redirects.studio.editor.parameters {
import com.coremedia.ui.data.ValueExpression;
import com.coremedia.ui.data.ValueExpressionFactory;
import com.tallence.core.redirects.studio.data.RedirectSourceParameter;
import com.tallence.core.redirects.studio.data.RedirectTargetParameter;

import ext.grid.GridPanel;

public class RedirectParamtersGridBase extends GridPanel {

  [Bindable]
  public var bindTo:ValueExpression;

  private var selectedPositionsExpression:ValueExpression;

  public function RedirectParamtersGridBase(config:RedirectParametersGrid = null) {
    super(config);
    if (config.validateRedirectHandler) {
      // If the data in the grid changes, a validation of the redirect must be triggered if the grid is used for the
      // source parameters. This cannot be solved with a change listener in the edit window, since a change listener
      // does not trigger when values change within an object of a list.
      getStore().addListener("datachanged", config.validateRedirectHandler);
    }
  }

  protected static function isEmptySelection(selection:Array):Boolean {
    return !selection || selection.length == 0;
  }

  protected function addRow():void {
    var init:Object = {};
    init[RedirectTargetParameter.NAME] = "parameter";
    init[RedirectTargetParameter.VALUE] = "value";
    init[RedirectSourceParameter.OPERATOR] = RedirectSourceParameter.OPERATOR_EQUALS;
    var redirectParameterImpl:RedirectSourceParameter = new RedirectSourceParameter(init);
    var parameters:Array = [].concat(bindTo.getValue());
    parameters.push(redirectParameterImpl);
    bindTo.setValue(parameters);
  }

  protected function deleteRow():void {
    var selection:Array = getSelectedPositionsExpression().getValue();
    if (selection && selection.length > 0) {
      var position:Number = selection[0];
      var items:Array = [].concat(bindTo.getValue());
      items.splice(position, 1);
      bindTo.setValue(items);
    }
  }

  protected function getSelectedPositionsExpression():ValueExpression {
    if (!selectedPositionsExpression) {
      selectedPositionsExpression = ValueExpressionFactory.createFromValue([]);
    }
    return selectedPositionsExpression;
  }

}
}
