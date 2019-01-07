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

import com.coremedia.rest.linking.AbstractLinkingResource;
import com.coremedia.rest.linking.LinkResolver;
import com.tallence.core.redirects.studio.model.Redirect;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import com.tallence.core.redirects.studio.repository.RedirectRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * A redirect {@link Redirect} object as a RESTful resource.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("redirect/{siteId:[^/]+}/{id:[^/]+}")
public class RedirectResource extends AbstractLinkingResource {

  private final RedirectRepository redirectRepository;

  private String id;

  @Autowired
  public RedirectResource(RedirectRepository redirectRepository, LinkResolver linkResolver) {
    this.redirectRepository = redirectRepository;
    this.setLinkResolver(linkResolver);
  }

  @GET
  public RedirectRepresentation getRedirect() {
    return new RedirectRepresentation(redirectRepository.getRedirect(getId()));
  }

  @PUT
  @Consumes({"application/json"})
  public Response setProperties(Map<String, Object> rawJson) {
    redirectRepository.updateRedirect(getId(), resolveUpdateProperties(rawJson));
    return Response.ok().build();
  }

  @DELETE
  public Response deleteRedirect() {
    redirectRepository.deleteRedirect(getId());
    return Response.ok().build();
  }

  @PathParam("id")
  public void setId(String id) {
    this.id = id;
  }

  private String getId() {
    return id;
  }

  private RedirectUpdateProperties resolveUpdateProperties(Map<String, Object> rawJson) {
    return new RedirectUpdateProperties((Map<String, Object>) resolveJson(rawJson));
  }

}