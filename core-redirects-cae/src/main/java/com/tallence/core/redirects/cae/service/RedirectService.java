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
