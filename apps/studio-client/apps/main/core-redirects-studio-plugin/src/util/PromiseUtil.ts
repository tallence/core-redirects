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

import RemoteServiceMethod from "@coremedia/studio-client.client-core-impl/data/impl/RemoteServiceMethod";
import RemoteServiceMethodResponse from "@coremedia/studio-client.client-core-impl/data/impl/RemoteServiceMethodResponse";
import RemoteBean from "@coremedia/studio-client.client-core/data/RemoteBean";
import Deferred from "@jangaroo/ext-ts/Deferred";
import IPromise from "@jangaroo/ext-ts/IPromise";
import JSON from "@jangaroo/ext-ts/JSON";
import Class from "@jangaroo/runtime/Class";
import { AnyFunction } from "@jangaroo/runtime/types";

/**
 * Utility class for creating promises.
 */
class PromiseUtil {

  /**
   * Creates a promise that invalidates the given remote bean.
   *
   * @param remoteBean the bean to be invalidated.
   * @return The promise. Resolve method signature: <code>function(bean:RemoteBean):void</code>
   */
  static invalidateRemoteBean(remoteBean: RemoteBean): IPromise {
    const deferred = new Deferred();
    remoteBean.invalidate((result: RemoteBean): void =>
      deferred.resolve(result),
    );
    return deferred.promise;

  }

  /**
   * Creates a promise that loads the given remote bean.
   *
   * @param remoteBean the bean to be loaded
   * @return The promise. Resolve method signature: <code>function(bean:RemoteBean):void</code>
   */
  static loadRemoteBean(remoteBean: RemoteBean): IPromise {
    const deferred = new Deferred();
    remoteBean.load((loaded: RemoteBean): void =>
      deferred.resolve(loaded),
    );
    return deferred.promise;
  }

  /**
   * Creates a promise that performs a get request. The response text of the request is parsed to a json object. This
   * json object is used to create the given class.
   *
   * @param path the request path
   * @param params the params for the get request
   * @param responseClass the class to which the json response should be parsed
   * @return The promise. Resolve method signature: <code>function(responseClass:Class):void</code>
   */
  static getRequest(path: string, params: any, responseClass: Class): IPromise {
    const deferred = new Deferred();

    const rsm = new RemoteServiceMethod(path, "GET");
    rsm.request(
      params,
      (rsmr: RemoteServiceMethodResponse): void => {
        const jsonResponse = JSON.decode(rsmr.text);
        const constructor: VoidFunction = responseClass["bind"].apply(responseClass, [null, jsonResponse]);
        deferred.resolve(new constructor());
      },
      (rsmr: RemoteServiceMethodResponse): void =>
        deferred.reject(rsmr.getError()),
    );

    return deferred.promise;
  }

}

export default PromiseUtil;
