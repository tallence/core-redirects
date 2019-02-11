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
        final boolean mayUseRegex;

        RedirectRights(boolean mayWrite, boolean mayUseRegex) {
            this.mayWrite = mayWrite;
            this.mayUseRegex = mayUseRegex;
        }

        public boolean isMayWrite() {
            return mayWrite;
        }

        public boolean isMayUseRegex() {
            return mayUseRegex;
        }
    }

}