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

package com.tallence.core.redirects.studio.service;

import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;

/**
 * A service that checks whether redirects can be created, deleted, or edited.
 */
public interface RedirectPermissionService {

    /**
     * Checks whether redirects can be read under the specified folder.
     *
     * @param rootFolder The site root folder for the redirects.
     * @return true, if the redirects can be read
     */
    boolean mayRead(Content rootFolder);

    /**
     * Checks whether the redirect can be created under the specified folder.
     *
     * @param rootFolder       The site root folder for the redirects.
     * @param updateProperties The properties of the redirect.
     * @return true, if the redirect can be created
     */
    boolean mayCreate(Content rootFolder, RedirectUpdateProperties updateProperties);

    /**
     * Checks whether the redirect can be deleted.
     *
     * @param redirect The redirect to be deleted.
     * @return true, if the redirect can be deleted
     */
    boolean mayDelete(Content redirect);

    /**
     * Checks whether the redirect can be edited.
     *
     * @param redirect         The redirect to be updated.
     * @param updateProperties The updated properties of the redirect.
     * @return true, if the redirect can be updated
     */
    boolean mayWrite(Content redirect, RedirectUpdateProperties updateProperties);

    RedirectRights resolveRights(Content rootFolder);

    /**
     * constructor and getters needs to be public for the json serializer.
     */
    class RedirectRights {
        final boolean mayWrite;
        final boolean mayPublish;
        final boolean mayUseRegex;
        final boolean mayUseTargetUrls;

        RedirectRights(boolean mayWrite, boolean mayPublish, boolean mayUseRegex, boolean mayUseTargetUrls) {
          this.mayWrite = mayWrite;
          this.mayPublish = mayPublish;
          this.mayUseRegex = mayUseRegex;
          this.mayUseTargetUrls = mayUseTargetUrls;
        }

        public boolean isMayWrite() {
            return mayWrite;
        }

        public boolean isMayUseRegex() {
            return mayUseRegex;
        }

      public boolean isMayUseTargetUrls() {
        return mayUseTargetUrls;
      }

      public boolean isMayPublish() {
        return mayPublish;
      }
    }

}
