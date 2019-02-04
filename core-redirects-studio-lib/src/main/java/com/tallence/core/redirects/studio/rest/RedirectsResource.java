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

package com.tallence.core.redirects.studio.rest;

import com.coremedia.cap.content.Content;
import com.coremedia.rest.linking.AbstractLinkingResource;
import com.coremedia.rest.linking.LinkResolver;
import com.sun.jersey.multipart.FormDataParam;
import com.tallence.core.redirects.studio.model.Pageable;
import com.tallence.core.redirects.studio.model.Redirect;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import com.tallence.core.redirects.studio.repository.RedirectRepository;
import com.tallence.core.redirects.studio.service.RedirectImporter;
import com.tallence.core.redirects.studio.service.RedirectPermissionService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

/**
 * The resource handles requests to load, create, validate and upload redirects.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("redirects/{siteId:[^/]+}")
public class RedirectsResource extends AbstractLinkingResource {

  private static final String SOURCE_ALREADY_EXISTS = "source_already_exists";
  private static final String INVALID_SOURCE = "source_invalid";

  private String siteId;
  private final RedirectRepository redirectRepository;
  private final RedirectImporter redirectImporter;
  private final RedirectPermissionService redirectPermissionService;

  @Autowired
  public RedirectsResource(RedirectRepository redirectRepository, LinkResolver linkResolver, RedirectImporter redirectImporter, RedirectPermissionService redirectPermissionService) {
    this.redirectRepository = redirectRepository;
    this.redirectImporter = redirectImporter;
    this.redirectPermissionService = redirectPermissionService;
    setLinkResolver(linkResolver);
  }

  @GET
  public Map<String, Object> getRedirectsForSite(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("sorter") String sorter,
      @QueryParam("sortDirection") String sortDirection,
      @QueryParam("search") String search) {
    Map<String, Object> response = new HashMap<>();
    Pageable redirects = redirectRepository.getRedirects(getSiteId(), search, sorter, sortDirection, pageSize, page);
    response.put("items", redirects.getRedirects().stream().map(RedirectReference::new).collect(Collectors.toList()));
    response.put("total", redirects.getTotal());
    return response;
  }

  @GET
  @Path("permissions")
  public RedirectPermissionService.RedirectRights resolveRights() {
    Content rootFolder = redirectRepository.getRedirectsRootFolder(siteId);
    return redirectPermissionService.resolveRights(rootFolder);

  }

  @POST
  @Path("create")
  @Consumes({"application/json"})
  public Response createRedirect(Map<String, Object> rawJson) {
    Redirect redirect = redirectRepository.createRedirect(getSiteId(), resolveUpdateProperties(rawJson));
    return Response.ok(new RedirectReference(redirect)).build();
  }

  @POST
  @Path("upload")
  @Consumes(MULTIPART_FORM_DATA)
  public RedirectImportResponse uploadCsv(@FormDataParam("file") InputStream inputStream) {
    return redirectImporter.importRedirects(getSiteId(), inputStream);
  }

  @GET
  @Path("validate")
  public RedirectValidationResult validateRedirect(@QueryParam("source") String source,
                                                   @QueryParam("redirectId") String redirectId) {
    RedirectValidationResult validationResult = new RedirectValidationResult();
    if (redirectId != null && redirectRepository.sourceAlreadyExists(getSiteId(), redirectId, source) ||
        redirectId == null && redirectRepository.sourceAlreadyExists(getSiteId(), source)) {
      validationResult.addErrorCode(SOURCE_ALREADY_EXISTS);
    }
    if (!redirectRepository.sourceIsValid(source)) {
      validationResult.addErrorCode(INVALID_SOURCE);
    }

    return validationResult;
  }

  private RedirectUpdateProperties resolveUpdateProperties(Map<String, Object> rawJson) {
    return new RedirectUpdateProperties((Map<String, Object>) resolveJson(rawJson));
  }

  private String getSiteId() {
    return siteId;
  }

  @PathParam("siteId")
  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

}