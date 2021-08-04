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
import com.tallence.core.redirects.model.RedirectParameter;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.model.Redirect;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A redirect representation used by the studio to display redirects.
 */
public class RedirectRepresentation {

  private final boolean active;
  private final SourceUrlType sourceUrlType;
  private final String source;
  private final Date creationDate;
  private final Content targetLink;
  private final String targetUrl;
  private final RedirectType redirectType;
  private final String siteId;
  private final String description;
  private final List<RedirectParameterRepresentation> sourceParameters;
  private final List<RedirectParameterRepresentation> targetParameters;

  RedirectRepresentation(Redirect redirect) {
    this.active = redirect.isActive();
    this.sourceUrlType = redirect.getSourceUrlType();
    this.source = redirect.getSource();
    this.creationDate = redirect.getCreationDate();
    this.targetLink = redirect.getTargetLink();
    this.targetUrl = redirect.getTargetUrl();
    this.redirectType = redirect.getRedirectType();
    this.siteId = redirect.getSiteId();
    this.description = redirect.getDescription();
    this.sourceParameters = convertParamers(redirect.getSourceParameters());
    this.targetParameters = convertParamers(redirect.getTargetParameters());
  }

  public boolean isActive() {
    return active;
  }

  public SourceUrlType getSourceUrlType() {
    return sourceUrlType;
  }

  public String getSource() {
    return source;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public Content getTargetLink() {
    return targetLink;
  }

  public String getTargetUrl() {
    return targetUrl;
  }

  public RedirectType getRedirectType() {
    return redirectType;
  }

  public String getSiteId() {
    return siteId;
  }

  public String getDescription() {
    return description;
  }

  public List<RedirectParameterRepresentation> getSourceParameters() {
    return sourceParameters;
  }

  public List<RedirectParameterRepresentation> getTargetParameters() {
    return targetParameters;
  }

  private List<RedirectParameterRepresentation> convertParamers(List<? extends RedirectParameter> parameters) {
    return Optional.ofNullable(parameters)
            .orElse(List.of())
            .stream()
            .map(RedirectParameterRepresentation::new)
            .collect(Collectors.toList());
  }
}
