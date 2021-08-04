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

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.publication.PublicationHelper;
import com.coremedia.cap.content.publication.PublicationService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.rest.cap.content.search.SearchServiceResult;
import com.coremedia.rest.cap.content.search.solr.SolrSearchService;
import com.tallence.core.redirects.helper.RedirectHelper;
import com.tallence.core.redirects.model.RedirectParameter;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectTargetParameter;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.model.Pageable;
import com.tallence.core.redirects.studio.model.Redirect;
import com.tallence.core.redirects.studio.model.RedirectImpl;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import com.tallence.core.redirects.studio.service.RedirectPermissionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.TARGET_URL;

/**
 * Default implementation of a {@link RedirectRepository}.
 * This implementation stores the redirects as content objects in a settings folder.
 */
public class RedirectRepositoryImpl implements RedirectRepository {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectRepositoryImpl.class);

  private static final String REDIRECT_PATH = "Options/Settings/Redirects";

  private static final String DESCRIPTION = "description";
  private static final String IMPORTED = "imported";
  private static final String SOURCE = "source";
  private static final String SOURCE_URL_TYPE = "sourceUrlType";
  private static final String REDIRECT_TYPE = "redirectType";
  private static final String TARGET_LINK = "targetLink";

  private final SolrSearchService solrSearchService;
  private final SitesService sitesService;
  private final ContentRepository contentRepository;
  private final PublicationHelper publicationHelper;
  private final PublicationService publicationService;
  private final RedirectPermissionService redirectPermissionService;

  private ContentType redirectContentType;

  @Autowired
  public RedirectRepositoryImpl(SolrSearchService solrSearchService, ContentRepository contentRepository,
                                SitesService sitesService, RedirectPermissionService redirectPermissionService) {
    this.solrSearchService = solrSearchService;
    this.contentRepository = contentRepository;
    this.sitesService = sitesService;
    redirectContentType = contentRepository.getContentType("Redirect");
    this.publicationService = contentRepository.getPublicationService();
    this.publicationHelper = new PublicationHelper(contentRepository);
    this.redirectPermissionService = redirectPermissionService;
  }

  @Override
  public Redirect createRedirect(String siteId, RedirectUpdateProperties updateProperties) {

    if (!redirectPermissionService.mayCreate(getRedirectsRootFolder(siteId), updateProperties)) {
      throw new IllegalStateException("User has no rights to create a redirect in site " + siteId);
    }

    String source = updateProperties.getSource();
    if (redirectAlreadyExists(siteId, source, updateProperties.getSourceParameters())) {
      throw new IllegalArgumentException("duplicated source url");
    }
    String uuid = UUID.randomUUID().toString();
    String documentName = "redirect-" + uuid;
    Content redirect = contentRepository.createChild(getFolderForRedirect(siteId, uuid), documentName, redirectContentType, new HashMap<>());
    try {
      updateRedirect(redirect, false, updateProperties);
    } catch (Exception e) {
      if (redirect.isCheckedOut()) {
        redirect.checkIn();
        redirect.delete();
      }
      throw e;
    } finally {
      if (!redirect.isDeleted() && redirect.isCheckedOut()) {
        redirect.checkIn();
      }
    }
    return convertToRedirect(redirect);
  }

  @Override
  public boolean sourceAlreadyExists(String siteId, String redirectId, String source, List<RedirectSourceParameter> sourceParameters) {
    return redirectAlreadyExists(siteId, redirectId, source, sourceParameters);
  }

  @Override
  public boolean sourceAlreadyExists(String siteId, String source, List<RedirectSourceParameter> sourceParameters) {
    return redirectAlreadyExists(siteId, source, sourceParameters);
  }

  @Override
  public boolean targetIsInvalid(Content target) {
    return !contentRepository.getPublicationService().isPublished(target);
  }

  @Override
  public Redirect getRedirect(String id) {
    return convertToRedirect(contentRepository.getContent(id));
  }

  @Override
  public void updateRedirect(String id, RedirectUpdateProperties updateProperties) {
    Content redirect = contentRepository.getContent(id);
    boolean wasPublished = publicationService.isPublished(redirect);
    if (redirect.isCheckedOut() && !redirect.isCheckedOutByCurrentSession()) {
      LOG.error("Content {} is checked out by other user, could not update redirect", redirect.getId());
      throw new IllegalArgumentException("Redirect is checked out by other user.");
    }

    if (!redirectPermissionService.mayWrite(redirect, updateProperties)) {
      throw new IllegalStateException("User has no rights to modify the redirect " + redirect.getId());
    }

    try {
      if (redirect.isCheckedIn()) {
        redirect.checkOut();
      }
      updateRedirect(redirect, wasPublished, updateProperties);
    } finally {
      if (redirect.isCheckedOut()) {
        redirect.checkIn();
      }
    }
  }

  @Override
  public void deleteRedirect(String id) {
    Content redirect = contentRepository.getContent(id);
    if (redirect.isCheckedOut() && !redirect.isCheckedOutByCurrentSession()) {
      LOG.error("Content {} is checked out by other user, could not delete redirect", redirect.getId());
      throw new IllegalArgumentException("Redirect is checked out by other user.");
    }

    if (!redirectPermissionService.mayDelete(redirect)) {
      throw new IllegalStateException("User has no rights to delete the redirect " + redirect.getId());
    }

    if (redirect.isCheckedOut()) {
      redirect.checkIn();
    }

    if (publicationService.isPublished(redirect)) {
      publicationHelper.withdraw(redirect);
    }

    redirect.delete();
  }

  @Override
  public Pageable getRedirects(String siteId, String search, String sorter, String sortDirection, int pageSize, int page, boolean exactMatch) {

    if (!redirectPermissionService.mayRead(getRedirectsRootFolder(siteId))) {
      return new Pageable(Collections.emptyList(), 0);
    }

    String query = StringUtils.isEmpty(StringUtils.trim(search)) ? "source:*" : getSourceQuery(search, exactMatch);

    List<String> sortCriteria;
    if (StringUtils.isNotEmpty(sorter) && StringUtils.isNotEmpty(sortDirection)) {
      sortCriteria = Collections.singletonList(sorter.toLowerCase() + " " + sortDirection.toLowerCase());
    } else {
      sortCriteria = Collections.emptyList();
    }

    SearchServiceResult result = search(siteId, Arrays.asList("isdeleted:false", query), sortCriteria, page * pageSize);
    List<Content> hits = result.getHits();
    List<Content> relevantContent = new ArrayList<>();
    //This complicated iteration is a workaround required by the lack of a "start" or "offset" parameter in
    // com.coremedia.rest.cap.content.search.SearchService.search
    for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, hits.size()); i++) {
      relevantContent.add(hits.get(i));
    }

    //Prefetch details for all contents with one call for performance reasons.
    contentRepository.prefetch(relevantContent);

    List<Redirect> redirects = relevantContent.stream()
        //check the state (isInProduction) in case someone deleted the result but the solr was not notified yet.
        .filter(Content::isInProduction)
        .map(this::convertToRedirect).collect(Collectors.toList());

    return new Pageable(redirects, Math.toIntExact(result.getTotal()));
  }

  @Override
  public Content getRedirectsRootFolder(String siteId) {
    Site site = sitesService.getSite(siteId);
    if (site != null) {
      if (site.getSiteRootFolder().hasChild(REDIRECT_PATH)) {
        return site.getSiteRootFolder().getChild(REDIRECT_PATH);
      }
      return site.getSiteRootFolder();
    }
    return contentRepository.getRoot().getChild(REDIRECT_PATH);
  }

  private boolean redirectAlreadyExists(String siteId, String redirectId, String source, List<RedirectSourceParameter> sourceParameters) {
    if (StringUtils.isEmpty(source)) {
      return false;
    }
    List<String> filterQueries = Arrays.asList("isdeleted:false", "source:" + ClientUtils.escapeQueryChars(source), "-numericid:" + redirectId);
    return redirectAlreadyExists(siteId, filterQueries, sourceParameters);
  }

  private boolean redirectAlreadyExists(String siteId, String source, List<RedirectSourceParameter> sourceParameters) {
    if (StringUtils.isEmpty(source)) {
      return false;
    }
    List<String> filterQueries = Arrays.asList("isdeleted:false", "source:" + ClientUtils.escapeQueryChars(source));
    return redirectAlreadyExists(siteId, filterQueries, sourceParameters);
  }

  private boolean redirectAlreadyExists(String siteId, List<String> filterQueries,List<RedirectSourceParameter> sourceParameters) {
    List<Content> hits = search(siteId, filterQueries, Collections.emptyList(), -1).getHits();
    // multiple redirects may exist for the same source as long as the source parameters are different
    return hits.stream()
            .map(RedirectHelper::getSourceParameters)
            .anyMatch(parameters -> parameters.size() == sourceParameters.size() && parameters.containsAll(sourceParameters));
  }

  /**
   * Uses the {@link SolrSearchService} to search for redirects.
   *
   * @param siteId        The site id.
   * @param filterQueries The filter queries.
   * @return the search result
   */
  private SearchServiceResult search(String siteId, List<String> filterQueries, List<String> sortCriteria, int limit) {
    return solrSearchService.search(
        "*",
        limit,
        sortCriteria,
        getRedirectsRootFolder(siteId),
        true,
        Collections.singletonList(redirectContentType),
        false,
        filterQueries,
        new ArrayList<>(),
        new ArrayList<>());
  }

  /**
   * Returns the folder for the new redirect. Redirects a stored in sub folders under the redirects root folder.
   *
   * @param siteId the site id
   * @param uuid   the uuid for the new redirect.
   * @return the folder
   */
  private Content getFolderForRedirect(String siteId, String uuid) {
    Site site = sitesService.getSite(siteId);
    String pathToRedirectFolder = REDIRECT_PATH + "/" + uuid.substring(0, 2);
    Content rootFolder = site != null ? site.getSiteRootFolder() : contentRepository.getRoot();

    if (!rootFolder.hasChild(pathToRedirectFolder)) {
      contentRepository.createSubfolders(rootFolder, pathToRedirectFolder);
    }
    return rootFolder.getChild(pathToRedirectFolder);
  }

  /**
   * Converts a {@link Content} object to a {@link Redirect}
   *
   * @param redirectEntry the redirect content object.
   * @return the redirect.
   */
  private Redirect convertToRedirect(Content redirectEntry) {
    Site site = sitesService.getContentSiteAspect(redirectEntry).getSite();
    return new RedirectImpl(
        Integer.toString(IdHelper.parseContentId(redirectEntry.getId())),
        site != null ? site.getId() : null,
        publicationService.isPublished(redirectEntry),
        SourceUrlType.asSourceUrlType(redirectEntry.getString(SOURCE_URL_TYPE)),
        redirectEntry.getString(SOURCE),
        redirectEntry.getCreationDate().getTime(),
        redirectEntry.getLink(TARGET_LINK),
        redirectEntry.getString(TARGET_URL),
        RedirectType.asRedirectType(redirectEntry.getString(REDIRECT_TYPE)),
        redirectEntry.getString(DESCRIPTION),
        redirectEntry.getBoolean(IMPORTED),
        RedirectHelper.getSourceParameters(redirectEntry),
        RedirectHelper.getTargetParameters(redirectEntry)
    );
  }

  /**
   * Updates a redirect content object.
   *
   * @param redirect         the redirect content object.
   * @param wasPublished     true, if the content was published.
   * @param updateProperties the redirect update properties.
   */
  private void updateRedirect(Content redirect, boolean wasPublished, RedirectUpdateProperties updateProperties) {
    updateProperty(updateProperties::getDescription, DESCRIPTION, redirect);
    updateBooleanProperty(updateProperties::getImported, IMPORTED, redirect);
    updateProperty(updateProperties::getSource, SOURCE, redirect);
    updateEnumProperty(updateProperties::getRedirectType, REDIRECT_TYPE, redirect);
    updateEnumProperty(updateProperties::getSourceUrlType, SOURCE_URL_TYPE, redirect);

    if (updateProperties.getTargetLink() != null) {
      updateLinkProperty(updateProperties::getTargetLink, TARGET_LINK, redirect);
      redirect.set(TARGET_URL, null);
    } else if (StringUtils.isNotEmpty(updateProperties.getTargetUrl())) {
      updateProperty(updateProperties::getTargetUrl, TARGET_URL, redirect);
      redirect.set(TARGET_LINK, null);
    }

    if (updateProperties.urlParametersChanged()) {
      StructBuilder structBuilder = Optional.ofNullable(redirect.getStruct(RedirectParameter.PROPERTY_URL_PARAMS))
              .map(Struct::builder)
              .orElseGet(this::createStructBuilder);

      if (updateProperties.sourceParametersChanged()) {
        structBuilder.remove(RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS);
        List<Struct> sourceParams = convertToStructs(updateProperties.getSourceParameters());
        structBuilder.declareStructs(RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS, sourceParams);
      }

      if (updateProperties.targetParametersChanged()) {
        structBuilder.remove(RedirectTargetParameter.STRUCT_PROPERTY_TARGET_PARAMS);
        List<Struct> targetParams = convertToStructs(updateProperties.getTargetParameters());
        structBuilder.declareStructs(RedirectTargetParameter.STRUCT_PROPERTY_TARGET_PARAMS, targetParams);
      }

      redirect.set(RedirectParameter.PROPERTY_URL_PARAMS, structBuilder.build());
    }

    Boolean active = updateProperties.getActive();
    if (active != null) {
      // if active is set to false, the redirect must be withdrawn
      publicationHelper.publish(redirect, active);
    } else if (wasPublished) {
      publicationHelper.publish(redirect);
    }
  }

  private StructBuilder createStructBuilder() {
    return contentRepository.getConnection().getStructService().createStructBuilder();
  }

  private List<Struct> convertToStructs(List<? extends RedirectParameter> parameters) {
    return parameters.stream().map(this::convertToStruct).collect(Collectors.toList());
  }

  private Struct convertToStruct(RedirectParameter parameter) {
    StructBuilder structBuilder = createStructBuilder();
    structBuilder.declareString(RedirectParameter.STRUCT_PROPERTY_PARAMS_NAME, Integer.MAX_VALUE, parameter.getName());
    structBuilder.declareString(RedirectParameter.STRUCT_PROPERTY_PARAMS_VALUE, Integer.MAX_VALUE, parameter.getValue());
    if (parameter instanceof RedirectSourceParameter) {
      structBuilder.declareString(RedirectSourceParameter.STRUCT_PROPERTY_SOURCE_PARAMS_OPERATOR, Integer.MAX_VALUE, ((RedirectSourceParameter) parameter).getOperator().name());
    }
    return structBuilder.build();
  }

  private void updateProperty(Supplier<Object> supplier, String property, Content redirect) {
    Object value = supplier.get();
    if (value != null) {
      redirect.set(property, value);
    }
  }

  private void updateEnumProperty(Supplier<Object> supplier, String propertyName, Content redirect) {
    Object value = supplier.get();
    if (value != null) {
      redirect.set(propertyName, value.toString());
    }
  }

  private void updateLinkProperty(Supplier<Content> supplier, String propertyName, Content redirect) {
    Content value = supplier.get();
    if (value != null) {
      redirect.set(propertyName, Collections.singletonList(value));
    }
  }

  private void updateBooleanProperty(Supplier<Boolean> supplier, String propertyName, Content redirect) {
    Boolean value = supplier.get();
    if (value != null) {
      redirect.set(propertyName, Boolean.TRUE.equals(value) ? 1 : 0);
    }
  }

  private String getSourceQuery(String search, boolean exactMatch) {
    String escaped = ClientUtils.escapeQueryChars(StringUtils.trim(search));
    return exactMatch ? "source:" + escaped : "source:*" + escaped + "*";
  }

}
