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

package com.tallence.core.redirects.studio.repository;

import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.studio.model.Pageable;
import com.tallence.core.redirects.studio.model.Redirect;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;

import java.util.List;

/**
 * Repository used by the studio to manage redirects.
 */
public interface RedirectRepository {

  /**
   * Creates a new redirect for the given site.
   *
   * @param siteId           the site id.
   * @param updateProperties the redirect properties.
   * @return a new redirect.
   */
  Redirect createRedirect(String siteId, RedirectUpdateProperties updateProperties);

  /**
   * Checks if the given source already exists.
   *
   * @param siteId           the site id.
   * @param source           the source.
   * @param redirectId       the redirect id.
   * @param sourceParameters the list of source url parameters.
   * @return boolean.
   */
  boolean sourceAlreadyExists(String siteId, String redirectId, String source, List<RedirectSourceParameter> sourceParameters);

  /**
   * Checks if the given source already exists.
   *
   * @param siteId           the site id.
   * @param source           the source.
   * @param sourceParameters the list of source url parameters.
   * @return boolean.
   */
  boolean sourceAlreadyExists(String siteId, String source, List<RedirectSourceParameter> sourceParameters);

  /**
   * Checks if the target of the redirect is invalid.
   *
   * @param target the target of the redirect
   * @return true, if the redirect has issues.
   */
  boolean targetIsInvalid(Content target);

  /**
   * Loads the redirect for the given id.
   *
   * @param id the id of the redirect.
   * @return the redirect.
   */
  Redirect getRedirect(String id);

  /**
   * Updates the properties of the redirect for the given id.
   *
   * @param id               the id of the redirect.
   * @param updateProperties the redirect properties.
   */
  void updateRedirect(String id, RedirectUpdateProperties updateProperties);

  /**
   * Deletes the redirect for the given id.
   *
   * @param id the id of the redirect.
   */
  void deleteRedirect(String id);

  /**
   * Loads redirects for the redirects grid in the studio.
   *
   * @param siteId        The site id.
   * @param search        A search text for filtering the redirects.
   * @param sorter        The sort field.
   * @param sortDirection The sort direction.
   * @param pageSize      The page size of the grid
   * @param page          The selected page
   * @param exactMatch    true if the path of the redirect for the search should match exactly
   * @return A pageable element with the redirects and a total size.
   */
  Pageable getRedirects(String siteId, String search, String sorter, String sortDirection, int pageSize, int page, boolean exactMatch);

  /**
   * Returns redirects root folder for the given site. If no site is found, the root folder is used as fallback folder.
   *
   * @param siteId the site id
   * @return the folder containing all redirects for the given site
   */
  Content getRedirectsRootFolder(String siteId);
}
