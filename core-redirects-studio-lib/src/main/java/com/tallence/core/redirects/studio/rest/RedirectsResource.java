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
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.rest.linking.LinkResolver;
import com.coremedia.rest.linking.LinkResolverUtil;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.model.Pageable;
import com.tallence.core.redirects.studio.model.Redirect;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import com.tallence.core.redirects.studio.repository.RedirectRepository;
import com.tallence.core.redirects.studio.service.RedirectImporter;
import com.tallence.core.redirects.studio.service.RedirectPermissionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.*;

/**
 * The resource handles requests to load, create, validate and upload redirects.
 */
@RestController
@RequestMapping(value = "redirects")
public class RedirectsResource {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectsResource.class);

  private final RedirectRepository redirectRepository;
  private final RedirectImporter redirectImporter;
  private final RedirectPermissionService redirectPermissionService;
  private final ContentRepository contentRepository;
  private final LinkResolver linkResolver;

  @Autowired
  public RedirectsResource(RedirectRepository redirectRepository,
                           LinkResolver linkResolver,
                           RedirectImporter redirectImporter,
                           RedirectPermissionService redirectPermissionService,
                           ContentRepository contentRepository) {
    this.redirectRepository = redirectRepository;
    this.redirectImporter = redirectImporter;
    this.redirectPermissionService = redirectPermissionService;
    this.contentRepository = contentRepository;
    this.linkResolver = linkResolver;
  }

  @GetMapping("{siteId}")
  public Map<String, Object> getRedirectsForSite(
          @PathVariable String siteId,
          @RequestParam int page,
          @RequestParam int pageSize,
          @RequestParam String sorter,
          @RequestParam String sortDirection,
          @RequestParam String search,
          @RequestParam boolean exactMatch) {
    Map<String, Object> response = new HashMap<>();
    Pageable redirects = redirectRepository.getRedirects(siteId, search, sorter, sortDirection, pageSize, page, exactMatch);
    response.put("items", redirects.getRedirects().stream().map(RedirectReference::new).collect(Collectors.toList()));
    response.put("total", redirects.getTotal());
    return response;
  }

  @GetMapping("{siteId}/permissions")
  public RedirectPermissionService.RedirectRights resolveRights(@PathVariable String siteId) {
    Content rootFolder = redirectRepository.getRedirectsRootFolder(siteId);
    return redirectPermissionService.resolveRights(rootFolder);

  }

  @PostMapping("{siteId}/create")
  public RedirectReference createRedirect(@PathVariable String siteId,
                                          @RequestBody Map<String, Object> rawJson) {
    RedirectUpdateProperties creationProperties = resolveCreationProperties(siteId, rawJson);

    Map<String, String> errors = creationProperties.validate();
    if (!errors.isEmpty()) {
      LOG.error("Validation failed for new redirect with properties [{}] and errors: [{}]. This should not happen, " +
              "frontend should already take care of invalid values.", rawJson, errors);
      throw new RedirectCreationException("Validation failed for new redirect");
    }

    Redirect redirect = redirectRepository.createRedirect(siteId, creationProperties);
    return new RedirectReference(redirect);
  }

  @PostMapping(value = "{siteId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public RedirectImportResponse uploadCsv(@PathVariable String siteId,
                                          @RequestParam("file") MultipartFile file) throws IOException {
    return redirectImporter.importRedirects(siteId, file.getInputStream());
  }

  @GetMapping("{siteId}/validate")
  public RedirectValidationResult validateRedirect(@PathVariable String siteId,
                                                   @RequestParam String source,
                                                   @RequestParam String redirectId,
                                                   @RequestParam String targetId,
                                                   @RequestParam(required = false) String targetUrl, //it might be not available
                                                   @RequestParam Boolean active,
                                                   @RequestParam List<RedirectSourceParameter> sourceParameters) {
    Map<String, Object> properties = new HashMap<>();

    properties.put(ACTIVE, active);
    properties.put(TARGET_LINK, StringUtils.isNotBlank(targetId) ? contentRepository.getContent(targetId) : null);
    properties.put(TARGET_URL, targetUrl);
    properties.put(SOURCE, source);
    properties.put(SOURCE_PARAMETERS, sourceParameters);
    // Let's assume default values for the types, so that the validation does not fail.
    // These are not sent by the validation request, as they cannot be empty.
    properties.put(REDIRECT_TYPE, RedirectType.AFTER_NOT_FOUND.toString());
    properties.put(SOURCE_URL_TYPE, SourceUrlType.PLAIN.toString());

    Map<String, String> errors = new RedirectUpdateProperties(properties, redirectRepository, siteId, redirectId).validate();

    RedirectValidationResult validationResult = new RedirectValidationResult();
    errors.forEach(validationResult::addErrorCode);

    return validationResult;
  }

  private RedirectUpdateProperties resolveCreationProperties(String siteId, Map<String, Object> rawJson) {
    Map<String, Object> resolvedJson = (Map) LinkResolverUtil.resolveJson(rawJson, this.linkResolver, true);
    return new RedirectUpdateProperties(resolvedJson, redirectRepository, siteId, null);
  }

  private class RedirectCreationException extends RuntimeException {

    public RedirectCreationException(String message) {
      super(message);
    }

  }

}
