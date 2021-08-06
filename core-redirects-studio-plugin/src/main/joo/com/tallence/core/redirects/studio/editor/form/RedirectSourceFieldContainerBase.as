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
import com.coremedia.cms.editor.sdk.util.MessageBoxUtil;
import com.coremedia.ui.data.ValueExpression;
import com.tallence.core.redirects.studio.data.RedirectSourceParameter;
import com.tallence.core.redirects.studio.data.RedirectTargetParameter;

import ext.Ext;
import ext.ObjectUtil;
import ext.form.FieldContainer;

/**
 * A form for editing redirects. Changes are bind to the localModel.
 * The localModel can be passed as a configuration parameter.
 */
public class RedirectSourceFieldContainerBase extends FieldContainer {

  [Bindable]
  public var errorMessagesVE:ValueExpression;

  [Bindable]
  public var sourceUrlVE:ValueExpression;

  [Bindable]
  public var sourceUrlParametersVE:ValueExpression;

  public function RedirectSourceFieldContainerBase(config:RedirectSourceFieldContainer = null) {
    super(config);
    this.sourceUrlParametersVE = config.sourceUrlParametersVE;
    config.sourceUrlVE.addChangeListener(sourceUrlChangeListener);

  }

  /**
   * When the source url changes, the listener checks if the string contains url parameters. In this case the
   * parameters  are parsed and added as {@link RedirectSourceParameter}. Additionally the value expression for the
   * source url is overwritten so that the string with the parameters is no longer part of the url.
   */
  private function sourceUrlChangeListener(update:ValueExpression):void {
    var sourceUrl:String = update.getValue();
    if (sourceUrl && sourceUrl.indexOf("?") != -1) {
      var queryString:String = sourceUrl.substring(sourceUrl.indexOf('?') + 1);

      var queryParams:Object = ObjectUtil.fromQueryString(queryString);
      var params:Array = ObjectUtil.getKeys(queryParams).map(function (key:String):RedirectSourceParameter {
        return convertToRedirectParameter(key, queryParams[key]);
      });

      var parameters:Array = [].concat(sourceUrlParametersVE.getValue()).concat(params);
      sourceUrlParametersVE.setValue(parameters);

      update.setValue(sourceUrl.substr(0, sourceUrl.indexOf('?')));
    }
  }

  private static function convertToRedirectParameter(key:String, entry:Object):RedirectSourceParameter {
    var value:String = "";
    if (entry instanceof Array) {
      value = (entry as Array) && (entry as Array)[0];
    } else {
      value = String(entry);
    }
    var init:Object = {};
    init[RedirectTargetParameter.NAME] = key;
    init[RedirectTargetParameter.VALUE] = value;
    init[RedirectSourceParameter.OPERATOR] = RedirectSourceParameter.OPERATOR_EQUALS;
    return new RedirectSourceParameter(init);
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

}
}
