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
import com.coremedia.ui.data.RemoteBeanUtil;
import com.tallence.core.redirects.studio.data.Redirect;

/**
 * Utility class for {@link Redirect}s.
 */
public class RedirectsUtil {

  /**
   * Returns true, if the redirect the linked content is loaded.
   * @param redirect the redirect.
   * @return Boolean
   */
  public static function redirectIsAccessible(redirect:Redirect):Boolean {
    if (!RemoteBeanUtil.isAccessible(redirect)) {
      return false;
    }

    var targetLink:Content = redirect.getTargetLink();
    if (targetLink && !targetLink.isLoaded()) {
      targetLink.load();
      return false;
    }
    return true;
  }

}
}