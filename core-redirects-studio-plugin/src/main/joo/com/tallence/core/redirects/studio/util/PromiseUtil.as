package com.tallence.core.redirects.studio.util {
import com.coremedia.ui.data.RemoteBean;
import com.coremedia.ui.data.impl.RemoteServiceMethod;
import com.coremedia.ui.data.impl.RemoteServiceMethodResponse;

import ext.Deferred;
import ext.IPromise;
import ext.JSON;
import ext.Promise;

/**
 * Utility class for creating promises.
 */
public class PromiseUtil {

  /**
   * Creates a promise that loads a list of remote beans.
   *
   * @param remoteBeans The beans to be loaded
   * @return The promise. Resolve method signature: <code>function(loaded:Array):void</code>
   */
  public static function loadRemoteBeans(remoteBeans:Array):IPromise {
    return Promise.all(remoteBeans.map(function (content:RemoteBean):IPromise {
      return loadRemoteBean(content);
    }));
  }

  /**
   * Creates a promise that invalidates the given remote bean.
   *
   * @param remoteBean the bean to be invalidated.
   * @return The promise. Resolve method signature: <code>function(bean:RemoteBean):void</code>
   */
  public static function invalidateRemoteBean(remoteBean:RemoteBean):IPromise {
    var deferred:Deferred = new Deferred();
    remoteBean.invalidate(function (result:RemoteBean):void {
      deferred.resolve(result);
    });
    return deferred.promise;

  }

  /**
   * Creates a promise that loads the given remote bean.
   *
   * @param remoteBean the bean to be loaded
   * @return The promise. Resolve method signature: <code>function(bean:RemoteBean):void</code>
   */
  public static function loadRemoteBean(remoteBean:RemoteBean):IPromise {
    var deferred:Deferred = new Deferred();
    remoteBean.load(function (loaded:RemoteBean):void {
      deferred.resolve(loaded);
    });
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
  public static function getRequest(path:String, params:Object, responseClass:Class):IPromise {
    var deferred:Deferred = new Deferred();

    var rsm:RemoteServiceMethod = new RemoteServiceMethod(path, "GET");
    rsm.request(
        params,
        function success(rsmr:RemoteServiceMethodResponse):void {
          var jsonResponse:Object = JSON.decode(rsmr.text);
          var constructor:Function = responseClass.bind.apply(responseClass, [null, jsonResponse]);
          deferred.resolve(new constructor());
        },
        function failure(rsmr:RemoteServiceMethodResponse):void {
          deferred.reject(rsmr.getError());
        });

    return deferred.promise;
  }

}
}
