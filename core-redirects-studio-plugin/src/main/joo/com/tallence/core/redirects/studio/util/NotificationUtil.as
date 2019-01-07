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
import com.coremedia.ui.mixins.ValidationState;

/**
 * Utility class to inform the user about failure or success messages.
 */
public class NotificationUtil {

  public static function showInfo(message:String):void {
    showRedirectProcessNotification(message, ValidationState.INFO);
  }

  public static function showError(message:String):void {
    showRedirectProcessNotification(message, ValidationState.ERROR);
  }

  private static function showRedirectProcessNotification(message:String, validationState:ValidationState):void {
    new RedirectProcessNotification(RedirectProcessNotification({
      title: message,
      validationState: validationState
    })).show();
  }

}
}