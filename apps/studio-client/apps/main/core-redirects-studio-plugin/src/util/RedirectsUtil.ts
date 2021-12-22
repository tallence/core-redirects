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

import Content from "@coremedia/studio-client.cap-rest-client/content/Content";
import BeanFactoryImpl from "@coremedia/studio-client.client-core-impl/data/impl/BeanFactoryImpl";
import RemoteService from "@coremedia/studio-client.client-core-impl/data/impl/RemoteService";
import RemoteServiceMethod from "@coremedia/studio-client.client-core-impl/data/impl/RemoteServiceMethod";
import RemoteServiceMethodResponse
  from "@coremedia/studio-client.client-core-impl/data/impl/RemoteServiceMethodResponse";
import RemoteBeanUtil from "@coremedia/studio-client.client-core/data/RemoteBeanUtil";
import beanFactory from "@coremedia/studio-client.client-core/data/beanFactory";
import RemoteError from "@coremedia/studio-client.client-core/data/error/RemoteError";
import Uploader from "@coremedia/studio-client.main.editor-components/sdk/components/html5/Uploader";
import FileWrapper from "@coremedia/studio-client.main.editor-components/sdk/upload/FileWrapper";
import IPromise from "@jangaroo/ext-ts/IPromise";
import JSON from "@jangaroo/ext-ts/JSON";
import ObjectUtil from "@jangaroo/ext-ts/Object";
import Promise from "@jangaroo/ext-ts/Promise";
import StringUtil from "@jangaroo/ext-ts/String";
import ReadOperation from "@jangaroo/ext-ts/data/operation/Read";
import Sorter from "@jangaroo/ext-ts/util/Sorter";
import {as} from "@jangaroo/runtime";
import int from "@jangaroo/runtime/int";
import {AnyFunction} from "@jangaroo/runtime/types";
import RedirectManagerStudioPlugin_properties from "../bundles/RedirectManagerStudioPlugin_properties";
import PermissionResponse from "../data/PermissionResponse";
import Redirect from "../data/Redirect";
import RedirectImpl from "../data/RedirectImpl";
import RedirectImportResponse from "../data/RedirectImportResponse";
import RedirectSourceParameter from "../data/RedirectSourceParameter";
import Redirects from "../data/Redirects";
import RedirectsResponse from "../data/RedirectsResponse";
import ValidationResponse from "../data/ValidationResponse";
import NotificationUtil from "./NotificationUtil";
import PromiseUtil from "./PromiseUtil";

/**
 * Utility class for {@link Redirect}s.
 */
class RedirectsUtil {

  static readonly #CREATE_URI_SEGMENT: string = "create";

  static readonly #DEFAULT_UPLOAD_SIZE: int = 67108864;

  /**
   * Returns true, if the redirect the linked content is loaded.
   * @param redirect the redirect.
   * @return Boolean
   */
  static redirectIsAccessible(redirect: Redirect): boolean {
    if (!redirect.isLoaded() && !RemoteBeanUtil.isAccessible(redirect)) {
      return false;
    }

    const targetLink: Content = redirect.getTargetLink();
    if (targetLink && !targetLink.isLoaded()) {
      targetLink.load();
      return false;
    }
    return true;
  }

  /**
   * Creates a new {@link Redirect} with the given properties
   */
  static createRedirect(siteId: string, active: boolean, targetLink: Content, targetUrl: string,
    description: string, source: string, sourceType: string, redirectType: string,
    sourceParameters: Array<any>, targetParameters: Array<any>): void {
    const rsm = new RemoteServiceMethod("redirects/" + siteId + "/" + RedirectsUtil.#CREATE_URI_SEGMENT, "POST", true);
    rsm.request({
      active: active,
      targetLink: targetLink,
      targetUrl: targetUrl,
      description: description,
      source: source,
      sourceUrlType: sourceType,
      redirectType: redirectType,
      sourceParameters: sourceParameters,
      targetParameters: targetParameters,
    },
    (rsmr: RemoteServiceMethodResponse): void =>
      NotificationUtil.showInfo(RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_new_success_text)
    ,
    (rsmr: RemoteServiceMethodResponse): void =>
      NotificationUtil.showError(RedirectManagerStudioPlugin_properties.redirectmanager_editor_actions_new_error_text + rsmr.getError()),

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
  static getRedirects(siteId: string, searchText: string, operation: ReadOperation, exactMatch: boolean): IPromise {
    if (!siteId || 0 === siteId.length) {
      return Promise.resolve(new RedirectsResponse([], 0));
    }

    const bean = beanFactory._.getRemoteBean("redirects/" + siteId + RedirectsUtil.#getQueryParams(searchText, operation, exactMatch));

    // A RemoteBean is used to load the redirects. Once a RemoteBean has been loaded, property data is cached. However,
    // a new request should be sent to the server when the reload button of the grid is activated or after a redirect is
    // created. To force reloading, the <code>invalidate()</code> method is used. The invalidation is also processed
    // asynchronously, so that afterwards the remote bean can be reloaded.
    return PromiseUtil
      .invalidateRemoteBean(bean)
      .then(PromiseUtil.loadRemoteBean)
      .then(RedirectsUtil.#createRedirectsResponse);
  }

  /**
   * Converts the response of the remote bean request into a {@link RedirectsResponse}.
   *
   * @param redirects the loaded redirects remote bean.
   * @return The promise. Resolve method signature: <code>function(response:RedirectsResponse):void</code>
   */
  static #createRedirectsResponse(redirects: Redirects): IPromise {
    const response = new RedirectsResponse(redirects.getItems(), redirects.getTotal());
    return Promise.resolve(response);
  }

  static #getQueryParams(searchText: string, operation: ReadOperation, exactMatch: boolean): string {
    const limit = operation.getLimit().toString();
    const page = operation.getPage().toString();
    const sorters: Array<any> = operation.getSorters();
    let sorter;
    let sortDirection;
    if (sorters && sorters.length) {
      let sorterConf = as(sorters[0], Sorter);
      if (!sorterConf.isInstance) {
        return;
      }
      sorter = sorterConf.getProperty();
      sortDirection = sorterConf.getDirection();

    } else {
      sorter = RedirectImpl.SOURCE;
      sortDirection = "ASC";
    }

    const queryParams: Record<string, any> = {};
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
  static uploadRedirects(siteId: string,
    fileWrapper: FileWrapper,
    success: AnyFunction,
    error: AnyFunction): void {

    const upldr = new Uploader({
      maxFileSize: RedirectsUtil.#DEFAULT_UPLOAD_SIZE,
      timeout: 20000,
      url: RemoteService.calculateRequestURI("/rest/api/redirects/" + siteId + "/upload"),
      method: "POST",
    });

    upldr.addListener("uploadcomplete", (_uploader: Uploader, response: XMLHttpRequest): void => {
      BeanFactoryImpl.resolveBeans(JSON.decode(response.responseText));

      //Hack for html4 upload.
      if (response.status === 200) {
        const importResponse = JSON.decode(response.responseText);
        const created = as(BeanFactoryImpl.resolveBeans(importResponse.created), Array);
        success.call(null, new RedirectImportResponse(created, importResponse.errorMessages));
      } else {
        error.call(null, response.statusText + " (code " + response.status + ")");
      }
    });

    upldr.addListener("uploadfailure", (_uploader: Uploader, response: XMLHttpRequest): void => {
      try {
        const result = new RemoteError(JSON.decode(response.responseText));
        error.call(null, result.message);
      } catch (e) {
        error.call(null, response.responseText);
      }
    });

    const file = fileWrapper.getFile();
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
  static validateRedirect(siteId: string,
    redirectId: string,
    source: string,
    targetId: string,
    targetUrl: string,
    active: boolean,
    sourceParameters: Array<any>): IPromise {
    const urlTemplate = "/{0}/validate";

    const joined = sourceParameters
      .map((parameter: RedirectSourceParameter): any =>
        parameter.getParametersAsMap())
      .map((map: any): string =>
        JSON.encodeValue(map),
      )
      .map(encodeURIComponent)
      .join(",");

    const params: Record<string, any> = {
      source: source,
      redirectId: redirectId,
      targetId: targetId,
      targetUrl: targetUrl,
      active: active,
      sourceParameters: joined,
    };

    const url = StringUtil.format(urlTemplate, siteId);
    return PromiseUtil.getRequest("redirects" + url, params, ValidationResponse);
  }

  /**
   * Resolve the permissions for the redirects in the selected site.
   */
  static resolvePermissions(siteId: string): IPromise {
    if (!siteId || siteId == "") {
      // if no site is selected, nothing may be edited
      return Promise.resolve(new PermissionResponse());
    }
    return PromiseUtil.getRequest("redirects/" + siteId + "/permissions", {}, PermissionResponse);
  }

}

export default RedirectsUtil;
