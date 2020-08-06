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

import com.coremedia.rest.linking.LinkResolver;
import com.coremedia.rest.linking.LinkResolverUtil;
import com.tallence.core.redirects.studio.model.Redirect;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import com.tallence.core.redirects.studio.repository.RedirectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A redirect {@link Redirect} object as a RESTful resource.
 */
@RestController
@RequestMapping(value = "redirect")
public class RedirectResource {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectResource.class);

  private final RedirectRepository redirectRepository;
  private final LinkResolver linkResolver;

  @Autowired
  public RedirectResource(RedirectRepository redirectRepository, LinkResolver linkResolver) {
    this.redirectRepository = redirectRepository;
    this.linkResolver = linkResolver;
  }

  @GetMapping("{siteId}/{id}")
  public RedirectRepresentation getRedirect(@PathVariable String id) {
    return new RedirectRepresentation(redirectRepository.getRedirect(id));
  }

  @PutMapping("{siteId}/{id}")
  public HttpStatus setProperties(@PathVariable String siteId,
                                  @PathVariable String id,
                                  @RequestBody Map<String, Object> rawJson) {
    RedirectUpdateProperties updateProperties = resolveUpdateProperties(siteId, id, rawJson);

    Map<String, String> errors = updateProperties.validate(true);
    if (!errors.isEmpty()) {
      LOG.error("Validation failed for redirectId [{}] with properties [{}] and errors: [{}]. This should not happen, " +
              "frontend should already take care of invalid values.", id, rawJson, errors);
      return HttpStatus.BAD_REQUEST;
    }

    redirectRepository.updateRedirect(id, updateProperties);
    return HttpStatus.OK;
  }

  @DeleteMapping("{siteId}/{id}")
  public HttpStatus deleteRedirect(@PathVariable String id) {
    redirectRepository.deleteRedirect(id);
    return HttpStatus.OK;
  }

  private RedirectUpdateProperties resolveUpdateProperties(String siteId, String id, Map<String, Object> rawJson) {
    Map<String, Object> resolvedJson = (Map) LinkResolverUtil.resolveJson(rawJson, this.linkResolver, true);
    return new RedirectUpdateProperties(resolvedJson, redirectRepository, siteId, id);
  }

}
