package com.tallence.core.redirects.cae.service;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.events.ContentEvent;
import com.coremedia.cap.content.events.ContentRepositoryEventConstants;
import com.coremedia.cap.content.events.ContentRepositoryListenerBase;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Listener for ContentRepo Events.
 *
 */
@Service
public class RedirectListener extends ContentRepositoryListenerBase {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectListener.class);

  private final ContentRepository contentRepository;
  private final RedirectDataService dataService;
  private final SitesService sitesService;

  private ContentType redirectType;

  public RedirectListener(ContentRepository contentRepository, RedirectDataService dataService, SitesService sitesService) {
    this.contentRepository = contentRepository;
    this.dataService = dataService;
    this.sitesService = sitesService;
  }

  @Override
  protected void handleContentEvent(ContentEvent event) {

    if (!event.getContent().getType().isSubtypeOf(redirectType)) {
      return;
    }

    RedirectDataService.JobType type;

    switch (event.getType()) {
      case ContentRepositoryEventConstants.CONTENT_CREATED:
        type = RedirectDataService.JobType.Create;
        break;
      case ContentRepositoryEventConstants.PROPERTIES_CHANGED:
        type = RedirectDataService.JobType.Modify;
        break;
      case ContentRepositoryEventConstants.CONTENT_DELETED:
        type = RedirectDataService.JobType.Delete;
        break;
      default:
        LOG.debug("Received an event for content [{}] but the event is not relevant: [{}]", event.getContent().getId(), event.getType());
        return;
    }

    Site site = sitesService.getContentSiteAspect(event.getContent()).getSite();
    if (site != null) {
      LOG.debug("Receiving event of type [{}] for content [{}], delegate it to the dataService", type, event.getContent().getId());
      dataService.addJob(site, type, event.getContent());
    } else {
      LOG.debug("Receiving event of type [{}] for content [{}] but no site found, cannot delegate it to the dataService", type, event.getContent().getId());
    }
  }

  @PostConstruct
  public void postConstruct() {
    redirectType = contentRepository.getContentType("Redirect");
    contentRepository.addContentRepositoryListener(this);
  }
}
