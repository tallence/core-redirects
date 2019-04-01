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

import ext.IPromise;

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
   * Creates a promise that loads the redirects for the given site. As soon as all redirects are loaded, the result is
   * returned by the promise. All redirect beans are already loaded. The beans of the redirect targets are not loaded
   * and have to be loaded asynchronously if necessary.
   *
   * @param siteId the site id.
   * @param searchText the search text.
   * @param operation the read operation.
   * @return The promise. Resolve method signature: <code>function(response:RedirectsResponse):void</code>
   */
  function getRedirects(siteId:String, searchText:String, operation:ReadOperation):IPromise;

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
   * Creates a promise that validates a redirect and returns a validation result.
   *
   * @param siteId the site id.
   * @param redirectId the redirect id.
   * @param source the source.
   * @param targetId the id of the target
   * @param active true, if the redirect is published
   *
   * @return The promise. Resolve method signature: <code>function(response:ValidationResponse):void</code>
   */
  function validateRedirect(siteId:String,
                            redirectId:String,
                            source:String,
                            targetId:String,
                            active:Boolean):IPromise;

  /**
   * Resolve the permissions for the redirects in the selected site.
   */
  function resolvePermissions(siteId: String): IPromise;
}
}
