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

package com.tallence.core.redirects.studio.util {
import com.coremedia.cap.content.Content;
import com.coremedia.cms.editor.sdk.components.html5.Uploader;
import com.coremedia.cms.editor.sdk.upload.FileWrapper;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.RemoteBeanUtil;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.error.RemoteError;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.coremedia.ui.data.impl.RemoteService;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;
import com.tallence.core.redirects.studio.data.PermissionResponse;
import com.tallence.core.redirects.studio.data.Redirect;
import com.tallence.core.redirects.studio.data.RedirectImpl;
import com.tallence.core.redirects.studio.data.RedirectImportResponse;
import com.tallence.core.redirects.studio.data.RedirectSourceParameter;
import com.tallence.core.redirects.studio.data.Redirects;
import com.tallence.core.redirects.studio.data.RedirectsResponse;
import com.tallence.core.redirects.studio.data.ValidationResponse;

import ext.IPromise;
import ext.JSON;
import ext.ObjectUtil;
import ext.Promise;
import ext.StringUtil;
import ext.data.operation.ReadOperation;
import ext.util.Sorter;

import js.XMLHttpRequest;

import mx.resources.ResourceManager;

/**
 * Utility class for {@link Redirect}s.
 */
public class RedirectsUtil {

  private static const CREATE_URI_SEGMENT:String = "create";
  private static const DEFAULT_UPLOAD_SIZE:int = 67108864;

  /**
   * Returns true, if the redirect the linked content is loaded.
   * @param redirect the redirect.
   * @return Boolean
   */
  public static function redirectIsAccessible(redirect:Redirect):Boolean {
    if (!redirect.isLoaded() && !RemoteBeanUtil.isAccessible(redirect)) {
      return false;
    }

    var targetLink:Content = redirect.getTargetLink();
    if (targetLink && !targetLink.isLoaded()) {
      targetLink.load();
      return false;
    }
    return true;
  }

  /**
   * Creates a new {@link Redirect} with the given properties
   */
  public static function createRedirect(siteId:String, active:Boolean, targetLink:Content, targetUrl: String,
                                        description:String, source:String, sourceType:String, redirectType:String,
                                        sourceParameters:Array, targetParameters:Array):void {
    var rsm:RemoteServiceMethod = new RemoteServiceMethod("redirects/" + siteId + "/" + CREATE_URI_SEGMENT, "POST", true);
    rsm.request({
              active: active,
              targetLink: targetLink,
              targetUrl: targetUrl,
              description: description,
              source: source,
              sourceUrlType: sourceType,
              redirectType: redirectType,
              sourceParameters: sourceParameters,
              targetParameters: targetParameters
            },
            function success(rsmr:RemoteServiceMethodResponse):void {
              NotificationUtil.showInfo(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_new_success_text'));
            },
            function failure(rsmr:RemoteServiceMethodResponse):void {
              NotificationUtil.showError(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_new_error_text') + rsmr.getError());
            }
    );
  }

  /**
   * Creates a promise that loads the redirects for the given site. As soon as all redirects are loaded, the result is
   * returned by the promise. All redirect beans are already loaded. The beans of the redirect targets are not loaded
   * and have to be loaded asynchronously if necessary.
   *
   * @param siteId the site id.
   * @param searchText the search text.
   * @param operation the read operation.
   * @param exactMatch true if the path of the redirect for the search should match exactly
   * @return The promise. Resolve method signature: <code>function(response:RedirectsResponse):void</code>
   */
  public static function getRedirects(siteId:String, searchText:String, operation:ReadOperation, exactMatch:Boolean):IPromise {
    if (!siteId || 0 === siteId.length) {
      return Promise.resolve(new RedirectsResponse([], 0));
    }

    var bean:RemoteBean = beanFactory.getRemoteBean("redirects/" + siteId + getQueryParams(searchText, operation, exactMatch));

    // A RemoteBean is used to load the redirects. Once a RemoteBean has been loaded, property data is cached. However,
    // a new request should be sent to the server when the reload button of the grid is activated or after a redirect is
    // created. To force reloading, the <code>invalidate()</code> method is used. The invalidation is also processed
    // asynchronously, so that afterwards the remote bean can be reloaded.
    return PromiseUtil
            .invalidateRemoteBean(bean)
            .then(PromiseUtil.loadRemoteBean)
            .then(createRedirectsResponse);
  }

  /**
   * Converts the response of the remote bean request into a {@link RedirectsResponse}.
   *
   * @param redirects the loaded redirects remote bean.
   * @return The promise. Resolve method signature: <code>function(response:RedirectsResponse):void</code>
   */
  private static function createRedirectsResponse(redirects:Redirects):IPromise {
    var response:RedirectsResponse = new RedirectsResponse(redirects.getItems(), redirects.getTotal());
    return Promise.resolve(response);
  }


  private static function getQueryParams(searchText:String, operation:ReadOperation, exactMatch:Boolean):String {
    var limit:String = operation.getLimit().toString();
    var page:String = operation.getPage().toString();
    var sorters:Array = operation.getSorters();
    var sorter:String = sorters && sorters.length > 0 ? (sorters[0] as Sorter).getProperty() : RedirectImpl.SOURCE;
    var sortDirection:String = sorters && sorters.length > 0 ? (sorters[0] as Sorter).getDirection() : "ASC";

    var queryParams:Object = {};
    queryParams["page"] = page;
    queryParams["pageSize"] = limit;
    queryParams["sorter"] = sorter;
    queryParams["sortDirection"] = sortDirection;
    queryParams["search"] = searchText;
    queryParams["exactMatch"] = exactMatch;

    return "?" + ObjectUtil.toQueryString(queryParams);
  }

  /**
   * Uploads a csv and imports all redirects.
   *
   * @param siteId the site id.
   * @param fileWrapper the file
   * @param success callback function for success
   * @param error callback function for error
   */
  public static function uploadRedirects(siteId:String,
                                         fileWrapper:FileWrapper,
                                         success:Function,
                                         error:Function):void {

    var upldr:Uploader = new Uploader(Uploader({
      maxFileSize: DEFAULT_UPLOAD_SIZE,
      timeout: 20000,
      url: RemoteService.calculateRequestURI("/rest/api/redirects/" + siteId + "/upload"),
      method: 'POST'
    }));

    upldr.addListener('uploadcomplete', function (_uploader:Uploader, response:XMLHttpRequest):void {
      BeanFactoryImpl.resolveBeans(JSON.decode(response.responseText));

      //Hack for html4 upload.
      if (response.status === 200) {
        var importResponse:Object = JSON.decode(response.responseText);
        var created:Array = BeanFactoryImpl.resolveBeans(importResponse.created) as Array;
        success.call(null, new RedirectImportResponse(created, importResponse.errorMessages));
      } else {
        error.call(null, response.statusText + ' (code ' + response.status + ')');
      }
    });

    upldr.addListener('uploadfailure', function (_uploader:Uploader, response:XMLHttpRequest):void {
      try {
        var result:RemoteError = new RemoteError(JSON.decode(response.responseText));
        error.call(null, result.message);
      } catch (e:*) {
        error.call(null, response.responseText);
      }
    });

    var file:* = fileWrapper.getFile();
    upldr.upload(file);
  }

  /**
   * Creates a promise that validates a redirect and returns a validation result.
   *
   * @param siteId the site id.
   * @param redirectId the redirect id.
   * @param source the source.
   * @param targetId the id of the target
   * @param targetUrl the targetUrl
   * @param active true, if the redirect is published
   * @param sourceParameters a list of SourceUrlParameters
   *
   * @return The promise. Resolve method signature: <code>function(response:ValidationResponse):void</code>
   */
  public static function validateRedirect(siteId:String,
                                          redirectId:String,
                                          source:String,
                                          targetId:String,
                                          targetUrl:String,
                                          active:Boolean,
                                          sourceParameters:Array):IPromise {
    var urlTemplate:String = "/{0}/validate";

    var joined:String = sourceParameters
            .map(function (parameter:RedirectSourceParameter):Object {
              return parameter.getParametersAsMap();})
            .map(function (map:Object):String {
              return JSON.encodeValue(map);
            })
            .map(encodeURIComponent)
            .join(",");

    var params:Object = {
      source: source,
      redirectId: redirectId,
      targetId: targetId,
      targetUrl: targetUrl,
      active: active,
      sourceParameters: joined
    };

    var url:String = StringUtil.format(urlTemplate, siteId);
    return PromiseUtil.getRequest("redirects" + url, params, ValidationResponse);
  }

  /**
   * Resolve the permissions for the redirects in the selected site.
   */
  public static function resolvePermissions(siteId:String):IPromise {
    if (!siteId || siteId == "") {
      // if no site is selected, nothing may be edited
      return Promise.resolve(new PermissionResponse());
    }
    return PromiseUtil.getRequest("redirects/" + siteId + "/permissions", {}, PermissionResponse);
  }


}
}
