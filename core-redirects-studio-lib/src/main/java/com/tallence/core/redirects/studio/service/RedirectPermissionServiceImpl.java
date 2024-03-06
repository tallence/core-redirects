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
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.authorization.AccessControl;
import com.coremedia.cap.content.authorization.Right;
import com.coremedia.cap.springframework.security.impl.CapUserDetails;
import com.coremedia.cap.user.Group;
import com.coremedia.cap.user.User;
import com.coremedia.cap.user.UserRepository;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.PostConstruct;

import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.SOURCE_URL_TYPE;

/**
 * Default implementation of a {@link RedirectPermissionService}.
 * With this implementation, the system checks whether the read and write authorizations are present on the folder when
 * creating, editing, deleting, and reading forwards. Redirects with a regex can only be edited, deleted or created by
 * administrators.
 */
public class RedirectPermissionServiceImpl implements RedirectPermissionService {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectPermissionServiceImpl.class);

  private final ContentRepository contentRepository;
  private final UserRepository userRepository;
  private final String regexGroupName;
  private final String targetUrlGroupName;
  private Group regexGroup;
  private Group targetUrlGroup;
  private ContentType redirectContentType;

  @Autowired
  public RedirectPermissionServiceImpl(ContentRepository contentRepository, UserRepository userRepository,
                                       @Value("${core.redirects.permissions.targetUrlGroup:}") String targetUrlGroupName,
                                       @Value("${core.redirects.permissions.regexGroup:}") String regexGroupName) {
    this.contentRepository = contentRepository;
    this.userRepository = userRepository;
    this.redirectContentType = contentRepository.getContentType("Redirect");
    this.regexGroupName = regexGroupName;
    this.targetUrlGroupName = targetUrlGroupName;
  }

  @Override
  public boolean mayRead(Content rootFolder) {
    return contentRepository.getAccessControl().mayPerform(rootFolder, this.redirectContentType, Right.READ);
  }

  @Override
  public boolean mayCreate(Content rootFolder, RedirectUpdateProperties updateProperties) {
    Boolean toBePublished = updateProperties.getActive();

    if (toBePublished == null) {
      //It should not be null
      LOG.warn("The active flag should not be null!");
      return false;
    }

    return mayPerformWrite(rootFolder) &&
            (!toBePublished || mayPerformPublish(rootFolder)) &&
            isAllowedForTargetUrl(updateProperties.getTargetUrl()) &&
            isAllowedForRegex(isUserAllowedForRegex(), updateProperties.getSourceUrlType());
  }

  @Override
  public boolean mayDelete(Content redirect) {
    //Only admins may delete regex redirects
    boolean administrator = isUserAllowedForRegex();

    boolean published = contentRepository.getPublicationService().isPublished(redirect);
    return mayPerformDelete(redirect) &&
            (!published || mayPerformPublish(redirect)) &&
            isAllowedForRegex(administrator, SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE)));
  }

  @Override
  public boolean mayWrite(Content redirect, RedirectUpdateProperties updateProperties) {
    //Only admins may edit regex redirects
    boolean administrator = isUserAllowedForRegex();

    //publication rights are required if the document is already published or if it is meant to be,
    // according to the given properties.
    boolean alreadyPublished = contentRepository.getPublicationService().isPublished(redirect);
    Boolean publishDocument = updateProperties.getActive();
    boolean requirePublicationRights = Boolean.TRUE.equals(publishDocument) || alreadyPublished;

    return mayPerformWrite(redirect) &&
            (!requirePublicationRights || mayPerformPublish(redirect)) &&
            isAllowedForTargetUrl(updateProperties.getTargetUrl()) &&
            isAllowedForRegex(administrator, updateProperties.getSourceUrlType()) &&
            isAllowedForRegex(administrator, SourceUrlType.asSourceUrlType(redirect.getString(SOURCE_URL_TYPE)));
  }

  @Override
  public RedirectRights resolveRights(Content rootFolder) {
    return new RedirectRights(mayPerformWrite(rootFolder), mayPerformPublish(rootFolder), isUserAllowedForRegex(), isUserAllowedForTargetUrlUsage());
  }

  private boolean isAllowedForRegex(boolean mayUseRegex, SourceUrlType sourceType) {
    return mayUseRegex || !SourceUrlType.REGEX.equals(sourceType);
  }

  private boolean isAllowedForTargetUrl(String targetUrl) {
    return isUserAllowedForTargetUrlUsage() || StringUtils.isEmpty(targetUrl);
  }

  private boolean mayPerformWrite(Content content) {
    AccessControl accessControl = contentRepository.getAccessControl();
    return accessControl.mayPerform(content, redirectContentType, Right.WRITE);
  }

  private boolean mayPerformPublish(Content content) {
    AccessControl accessControl = contentRepository.getAccessControl();
    return accessControl.mayPerform(content, redirectContentType, Right.PUBLISH);
  }

  private boolean mayPerformDelete(Content content) {
    AccessControl accessControl = contentRepository.getAccessControl();
    return accessControl.mayPerform(content, redirectContentType, Right.DELETE);
  }

  private boolean isUserAllowedForRegex() {
    User user = userRepository.getUser(getUserId());
    if (user == null) {
      throw new IllegalStateException("No user could be found");
    }

    if (regexGroup != null) {
      return user.isMemberOf(regexGroup);
    } else {
      return user.isAdministrative();
    }
  }

  private boolean isUserAllowedForTargetUrlUsage() {
    User user = userRepository.getUser(getUserId());
    if (user == null) {
      throw new IllegalStateException("No user could be found");
    }

    if (targetUrlGroup != null) {
      return user.isMemberOf(targetUrlGroup) || user.isAdministrative();
    } else {
      return "*".equalsIgnoreCase(targetUrlGroupName);
    }
  }

  private String getUserId() {
    Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (user instanceof CapUserDetails) {
      return ((CapUserDetails) user).getUserId();
    } else {
      throw new IllegalStateException("Could not get userId from authenticated user.");
    }
  }

  @PostConstruct
  public void postConstruct() {
    if (StringUtils.isNotBlank(regexGroupName)) {
      regexGroup = userRepository.getGroupByName(regexGroupName);
      if (regexGroup == null) {
        LOG.error("Configured regexGroup [{}] not found in CMS!", regexGroupName);
      }
    }

    if (StringUtils.isNotBlank(targetUrlGroupName)) {
      targetUrlGroup = userRepository.getGroupByName(targetUrlGroupName);
      if (targetUrlGroup == null) {
        LOG.error("Configured targetUrlGroup [{}] not found in CMS!", targetUrlGroupName);
      }
    }
  }
}
