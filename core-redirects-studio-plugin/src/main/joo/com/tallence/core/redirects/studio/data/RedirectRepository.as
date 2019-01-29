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
import com.coremedia.cms.editor.sdk.upload.FileWrapper;
import com.coremedia.ui.data.RemoteBean;

import ext.data.operation.ReadOperation;

/**
 * A repository for redirects
 */
public interface RedirectRepository extends RemoteBean {

  /**
   * Creates a new {@link Redirect} with the given properties
   *
   * @param siteId
   * @param active
   * @param targetLink
   * @param description
   * @param source
   * @param sourceType
   * @param redirectType
   */
  function createRedirect(siteId:String,
                          active:Boolean,
                          targetLink:Content,
                          description:String,
                          source:String,
                          sourceType:String,
                          redirectType:String):void;

  /**
   * Returns the redirects for for the given site.
   *
   * @param siteId
   * @param searchText
   * @param operation
   * @return an array of redirects.
   */
  function getRedirects(siteId:String, searchText:String, operation:ReadOperation):RedirectsResponse;

  /**
   * Uploads a csv and imports all redirects.
   *
   * @param siteId the site id.
   * @param fileWrapper the file
   * @param success callback function for success
   * @param error callback function for error
   */
  function uploadRedirects(siteId:String,
                           fileWrapper:FileWrapper,
                           success:Function,
                           error:Function):void;

  /**
   * Validates the source property.
   *
   * @param siteId the site id.
   * @param redirectId the redirect id.
   * @param source the source.
   * @param callback callback funtion.
   */
  function validateSource(siteId:String,
                          redirectId:String,
                          source:String,
                          callback:Function):void;

  /**
   * Invalidates the redirects {@link RemoteBean}. After the validation a new request is sent to the backend
   * for the same search parameters.
   */
  function invalidateRedirects():void;

  /**
   * Resolve the permissions for the redirects in the selected site.
   */
  function resolvePermissions(siteId: String, callback: Function): void;
}
}