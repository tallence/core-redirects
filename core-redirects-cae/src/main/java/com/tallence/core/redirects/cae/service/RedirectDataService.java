package com.tallence.core.redirects.cae.service;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;

/**
 * Service for resolving and building redirects.
 *
 */
public interface RedirectDataService {

  enum JobType {
    Create, Delete, Modify;

    boolean requiredDeletion() {
      return this.equals(Delete) || this.equals(Modify);
    }

    boolean requiredCreation() {
      return this.equals(Create) || this.equals(Modify);
    }
  }

  SiteRedirects getForSite(Site site);

  void addJob(Site site, JobType type, Content content);

}
