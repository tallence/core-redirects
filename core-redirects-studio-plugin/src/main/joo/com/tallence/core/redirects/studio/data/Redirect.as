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
import com.coremedia.cap.undoc.content.Content;
import com.coremedia.ui.data.RemoteBean;

/**
 * A model representing a redirect.
 */
public interface Redirect extends RemoteBean {

  function deleteMe(callback:Function = null):void;

  function getId():String;

  function isActive():Boolean;

  function setActive(active:Boolean):void;

  function getTargetLink():Content;

  function getTargetUrl():String;

  function setTargetLink(content:Content):void;

  function setTargetUrl(targetUrl: String):void;

  function getTargetLinkName():String;

  function setTargetLinkName(name:String):void;

  function getCreationDate():Date;

  function setCreationDate(creationDate:Date):void;

  function getRedirectType():Number;

  function setRedirectType(redirectType:Number):void;

  function getDescription():String;

  function setDescription(description:String):void;

  function isImported():Boolean;

  function getSourceType():String;

  function setSourceType(sourceType:String):void;

  function getSource():String;

  function setSource(source:String):void;

  function getSiteId():String;

  function setSourceParameters(parameters:Array):void;

  function getSourceParameters():Array;

  function setTargetParameters(parameters:Array):void;

  function getTargetParameters():Array;

}
}
