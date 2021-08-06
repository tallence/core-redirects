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

package com.tallence.core.redirects.studio.model;

import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.model.RedirectParameter;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectTargetParameter;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.repository.RedirectRepository;

import java.util.Date;
import java.util.List;

/**
 * Interface for redirects used by the {@link RedirectRepository} to
 * edit, create and import redirects and used by the {@link com.tallence.core.redirects.cae.service.RedirectService}
 * to handle redirects in the cae.
 */
public interface Redirect {

  /**
   * Returns the id of the rediret.
   */
  String getId();

  /**
   * Returns the id of the site for which redirection is active.
   */
  String getSiteId();

  /**
   * Returns true, if the redirect is active.
   */
  boolean isActive();

  /**
   * Returns the {@link SourceUrlType} of the redirect.
   */
  SourceUrlType getSourceUrlType();

  /**
   * Returns the source url of the redirect.
   */
  String getSource();

  /**
   * Returns the creation date of the redirect.
   */
  Date getCreationDate();

  /**
   * Returns a {@link Content} to which the redirect links.
   */
  Content getTargetLink();

  /**
   * Returns the target url of the redirect.
   */
  String getTargetUrl();

  /**
   * Returns the {@link RedirectType} of the redirect.
   */
  RedirectType getRedirectType();

  /**
   * Returns a description of the redirect.
   */
  String getDescription();

  /**
   * Returns true, if the redirect was imported.
   */
  boolean isImported();

  /**
   * Returns the list of source parameters or an empty list.
   */
  List<RedirectSourceParameter> getSourceParameters();

  /**
   * Returns the list of target parameters or an empty list.
   */
  List<RedirectTargetParameter> getTargetParameters();

}
