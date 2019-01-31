package com.tallence.core.redirects.studio.service;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.authorization.AccessControl;
import com.coremedia.cap.content.authorization.Right;
import com.coremedia.cap.springframework.security.impl.CapUserDetails;
import com.coremedia.cap.user.User;
import com.coremedia.cap.user.UserRepository;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.SOURCE_URL_TYPE;

/**
 * Default implementation of a {@link RedirectPermissionService}.
 * With this implementation, the system checks whether the read and write authorizations are present on the folder when
 * creating, editing, deleting, and reading forwards. Redirects with a regex can only be edited, deleted or created by
 * administrators.
 */
public class RedirectPermissionServiceImpl implements RedirectPermissionService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private ContentType redirectContentType;

    @Autowired
    public RedirectPermissionServiceImpl(ContentRepository contentRepository, UserRepository userRepository) {
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        redirectContentType = contentRepository.getContentType("Redirect");
    }

    @Override
    public boolean mayRead(Content rootFolder) {
        return contentRepository.getAccessControl().mayPerform(rootFolder, this.redirectContentType, Right.READ);
    }

    @Override
    public boolean mayCreate(Content rootFolder, RedirectUpdateProperties updateProperties) {
        return mayPerformWriteAndPublish(rootFolder) && isAllowedForRegex(isAdministrator(), updateProperties.getSourceUrlType());
    }

    @Override
    public boolean mayDelete(Content redirect) {
        //Only admins may delete regex redirects
        boolean administrator = isAdministrator();
        return mayPerformDeleteAndPublish(redirect) &&
                isAllowedForRegex(administrator, SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE)));
    }

    @Override
    public boolean mayWrite(Content redirect, RedirectUpdateProperties updateProperties) {
        //Only admins may edit regex redirects
        boolean administrator = isAdministrator();
        return mayPerformWriteAndPublish(redirect) &&
                isAllowedForRegex(administrator, updateProperties.getSourceUrlType()) &&
                isAllowedForRegex(administrator, SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE)));
    }

    @Override
    public RedirectRights resolveRights(Content rootFolder) {
        return new RedirectRights(mayPerformWriteAndPublish(rootFolder), isAdministrator());
    }

    private boolean isAllowedForRegex(boolean mayUseRegex, SourceUrlType sourceType) {
        return mayUseRegex || !SourceUrlType.REGEX.equals(sourceType);
    }

    private boolean mayPerformWriteAndPublish(Content content) {
        AccessControl accessControl = contentRepository.getAccessControl();
        return accessControl.mayPerform(content, redirectContentType, Right.WRITE) &&
                accessControl.mayPerform(content, redirectContentType, Right.PUBLISH);
    }

    private boolean mayPerformDeleteAndPublish(Content content) {
        AccessControl accessControl = contentRepository.getAccessControl();
        return accessControl.mayPerform(content, redirectContentType, Right.DELETE) &&
                accessControl.mayPerform(content, redirectContentType, Right.PUBLISH);
    }

    private boolean isAdministrator() {
        User user = userRepository.getUser(getUserId());
        if (user == null) {
            throw new IllegalStateException("No user could be found");
        }

        return user.isAdministrative();
    }

    private String getUserId() {
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof CapUserDetails) {
            return ((CapUserDetails) user).getUserId();
        } else {
            throw new IllegalStateException("Could not get userId from authenticated user.");
        }
    }
}
