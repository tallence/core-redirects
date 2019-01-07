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

package com.tallence.core.redirects.studio {
import com.coremedia.cms.editor.configuration.StudioPlugin;
import com.coremedia.cms.editor.sdk.IEditorContext;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.tallence.core.redirects.studio.data.RedirectImpl;
import com.tallence.core.redirects.studio.data.RedirectRepositoryImpl;

public class RedirectManagerStudioPluginBase extends StudioPlugin {

  public function RedirectManagerStudioPluginBase(config:RedirectManagerStudioPlugin = null) {
    super(config)
  }

  override public function init(editorContext:IEditorContext):void {
    super.init(editorContext);
    initRemoteBeans();
  }

  private static function initRemoteBeans():void {
    BeanFactoryImpl(beanFactory).registerRemoteBeanClasses(RedirectRepositoryImpl);
    BeanFactoryImpl(beanFactory).registerRemoteBeanClasses(RedirectImpl);
  }

}
}