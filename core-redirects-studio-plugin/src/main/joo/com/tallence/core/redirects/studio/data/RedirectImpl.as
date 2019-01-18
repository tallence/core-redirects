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

package com.tallence.core.redirects.studio.data {
import com.coremedia.cap.content.Content;
import com.coremedia.ui.data.impl.RemoteBeanImpl;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;
import com.tallence.core.redirects.studio.util.NotificationUtil;

import mx.resources.ResourceManager;

[RestResource(uriTemplate="redirect/{siteId:[^/]+}/{id:[^/]+}")]
public class RedirectImpl extends RemoteBeanImpl implements Redirect {

  public static const ACTIVE:String = "active";
  public static const CREATION_DATE:String = "creationDate";
  public static const REDIRECT_TYPE:String = "redirectType";
  public static const REDIRECT_TYPE_ALWAYS:String = "ALWAYS";
  public static const REDIRECT_TYPE_404:String = "AFTER_NOT_FOUND";
  public static const SOURCE:String = "source";
  public static const SOURCE_TYPE:String = "sourceUrlType";
  public static const SOURCE_TYPE_PLAIN:String = "PLAIN";
  public static const SOURCE_TYPE_REGEX:String = "REGEX";
  public static const TARGET_LINK:String = "targetLink";
  public static const DESCRIPTION:String = "description";
  public static const IMPORTED:String = "imported";
  public static const SITE_ID:String = "siteId";

  /**
   * List of all redirect properties, used by the grid.
   */
  public static const REDIRECT_PROPERTIES:Array = [
    ACTIVE,
    CREATION_DATE,
    REDIRECT_TYPE,
    SOURCE,
    SOURCE_TYPE,
    SOURCE_TYPE_PLAIN,
    SOURCE_TYPE_REGEX,
    TARGET_LINK,
    DESCRIPTION,
    IMPORTED,
    SITE_ID
  ];

  public function RedirectImpl(path:String) {
    super(path);
  }

  public function deleteMe(callback:Function = null):void {
    var rsm:RemoteServiceMethod = new RemoteServiceMethod(this.getUriPath(), 'DELETE');
    rsm.request(
        null,
        function success(rsmr:RemoteServiceMethodResponse):void {
          NotificationUtil.showInfo(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_delete_result_text_success'));
          callback.call(this);
        },
        function failure(rsmr:RemoteServiceMethodResponse):void {
          NotificationUtil.showError(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_delete_result_text_error_w_msg') + rsmr.getError());
        });
  }

  public function isActive():Boolean {
    return get(ACTIVE);
  }

  public function setActive(active:Boolean):void {
    set(ACTIVE, active);
  }

  public function getTargetLink():Content {
    return get(TARGET_LINK);
  }

  public function setTargetLink(content:Content):void {
    set(TARGET_LINK, content);
  }

  public function getCreationDate():Date {
    return get(CREATION_DATE);
  }

  public function setCreationDate(creationDate:Date):void {
    set(CREATION_DATE, creationDate);
  }

  public function getRedirectType():Number {
    return get(REDIRECT_TYPE);
  }

  public function setRedirectType(redirectType:Number):void {
    set(REDIRECT_TYPE, redirectType);
  }

  public function getDescription():String {
    return get(DESCRIPTION);
  }

  public function setDescription(description:String):void {
    set(DESCRIPTION, description);
  }

  public function isImported():Boolean {
    return get(IMPORTED);
  }

  public function getSourceType():String {
    return get(SOURCE_TYPE);
  }

  public function setSourceType(sourceType:String):void {
    set(SOURCE_TYPE, sourceType);
  }

  public function getSource():String {
    return get(SOURCE);
  }

  public function setSource(source:String):void {
    set(SOURCE, source);
  }

  public function getSiteId():String {
    return get(SITE_ID);
  }

  override protected function propertiesUpdated(overwrittenValues:Object, newValues:Object):void {
    NotificationUtil.showInfo(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_save_result_text_success'));
    super.propertiesUpdated(overwrittenValues, newValues);
  }

}
}