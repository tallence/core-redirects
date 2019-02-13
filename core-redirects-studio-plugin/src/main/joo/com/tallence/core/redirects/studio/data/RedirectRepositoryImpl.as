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
import com.coremedia.cms.editor.sdk.components.html5.Uploader;
import com.coremedia.cms.editor.sdk.upload.FileWrapper;
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.beanFactory;
import com.coremedia.ui.data.error.RemoteError;
import com.coremedia.ui.data.impl.BeanFactoryImpl;
import com.coremedia.ui.data.impl.RemoteBeanImpl;
import com.coremedia.ui.data.impl.RemoteService;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;
import com.tallence.core.redirects.studio.util.NotificationUtil;
import com.tallence.core.redirects.studio.util.PromiseUtil;

import ext.IPromise;
import ext.JSON;
import ext.Promise;
import ext.data.operation.ReadOperation;
import ext.util.Sorter;

import js.XMLHttpRequest;

import mx.resources.ResourceManager;

[RestResource(uriTemplate="redirects")]
public class RedirectRepositoryImpl extends RemoteBeanImpl implements RedirectRepository {

  private static const DEFAULT_UPLOAD_SIZE:int = 67108864;
  private static const CREATE_URI_SEGMENT:String = "create";
  private static const VALIDATE_URI_SEGMENT:String = "validate";
  private static var instance:RedirectRepository;

  public function RedirectRepositoryImpl(path:String) {
    super(path);
  }

  public static function getInstance():RedirectRepository {
    if (!instance) {
      instance = RedirectRepository(beanFactory.getRemoteBean("redirects"));
    }
    return instance;
  }

  public function createRedirect(siteId:String, active:Boolean, targetLink:Content, description:String, source:String, sourceType:String, redirectType:String):void {
    var rsm:RemoteServiceMethod = new RemoteServiceMethod(this.getUriPath() + "/" + siteId + "/" + CREATE_URI_SEGMENT, "POST", true);
    rsm.request({
          active: active,
          targetLink: targetLink,
          description: description,
          source: source,
          sourceUrlType: sourceType,
          redirectType: redirectType
        },
        function success(rsmr:RemoteServiceMethodResponse):void {
          NotificationUtil.showInfo(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_new_success_text'));
        },
        function failure(rsmr:RemoteServiceMethodResponse):void {
          NotificationUtil.showError(ResourceManager.getInstance().getString('com.tallence.core.redirects.studio.bundles.RedirectManagerStudioPlugin', 'redirectmanager_editor_actions_new_error_text') + rsmr.getError());
        }
    );
  }

  public function getRedirects(siteId:String, searchText:String, operation:ReadOperation):IPromise {
    if (!siteId || 0 === siteId.length) {
      return Promise.resolve(new RedirectsResponse([], 0));
    }

    var bean:RemoteBean = beanFactory.getRemoteBean(this.getUriPath() + "/" + siteId + getQueryParams(searchText, operation));

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
   * @param remoteBean the loaded remote bean.
   * @return The promise. Resolve method signature: <code>function(response:RedirectsResponse):void</code>
   */
  private static function createRedirectsResponse(remoteBean:RemoteBean):IPromise {
    var response:RedirectsResponse = new RedirectsResponse(remoteBean.get("items"), remoteBean.get("total"));
    return Promise.resolve(response);
  }

  public function uploadRedirects(siteId:String,
                                  fileWrapper:FileWrapper,
                                  success:Function,
                                  error:Function):void {

    var upldr:Uploader = new Uploader(Uploader({
      maxFileSize: DEFAULT_UPLOAD_SIZE,
      timeout: 20000,
      url: RemoteService.calculateRequestURI("/api/redirects/" + siteId + "/upload"),
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

  public function validateSource(siteId:String,
                                 redirectId:String,
                                 source:String):IPromise {
    //Encode the source, it might contain characters like "&"
    var url:String = "/" + siteId + "/" + VALIDATE_URI_SEGMENT + "?siteId=" + siteId + "&source=" + encodeURIComponent(source);
    if (redirectId) {
      url = url + "&redirectId=" + redirectId;
    }

    return PromiseUtil.getRequest(this.getUriPath() + url, {}, ValidationResponse);
  }

  public function resolvePermissions(siteId: String): IPromise {
    if (!siteId || siteId == "") {
      // if no site is selected, nothing may be edited
      return Promise.resolve(new PermissionResponse());
    }
    return PromiseUtil.getRequest(this.getUriPath() + "/" + siteId + "/permissions", {}, PermissionResponse);
  }

  private static function getQueryParams(searchText:String, operation:ReadOperation):String {
    var limit:String = operation.getLimit().toString();
    var page:String = operation.getPage().toString();
    var sorters:Array = operation.getSorters();
    var sorter:String = sorters && sorters.length > 0 ? (sorters[0] as Sorter).getProperty() : RedirectImpl.SOURCE;
    var sortDirection:String = sorters && sorters.length > 0 ? (sorters[0] as Sorter).getDirection() : "ASC";


    var query:String = "?page=" + page;
    query = appendQueryParam(query, "pageSize", limit);
    query = appendQueryParam(query, "sorter", sorter);
    query = appendQueryParam(query, "sortDirection", sortDirection);
    query = appendQueryParam(query, "search", searchText);
    return query;
  }

  private static function appendQueryParam(query:String, param:String, value:String):String {
    return query + "&" + param + "=" + value;
  }

}

}
