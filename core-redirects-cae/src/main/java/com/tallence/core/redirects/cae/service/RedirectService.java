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
package com.tallence.core.redirects.cae.service;

import com.coremedia.cap.multisite.Site;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A service used by the CAE to manage redirects.
 */
public interface RedirectService {

  /**
   * Returns all redirects for the given site.
   *
   * @param site the site marker for which the redirects should be fetched. May be {@code null}, in which case an empty result will be returned.
   * @return a holder for redirect results.
   */
  @NonNull
  SiteRedirects getRedirectsForSite(@Nullable Site site);

}
