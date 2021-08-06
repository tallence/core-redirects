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

package com.tallence.core.redirects.studio.model;

import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.model.RedirectSourceParameter;
import com.tallence.core.redirects.model.RedirectTargetParameter;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;

import java.util.Date;
import java.util.List;

/**
 * Default implementation of a {@link Redirect}
 */
public class RedirectImpl implements Redirect {

  private String id;
  private String siteId;
  private boolean active;
  private SourceUrlType sourceUrlType;
  private String source;
  private Date creationDate;
  private Content targetLink;
  private String targetUrl;
  private RedirectType redirectType;
  private String description;
  private boolean isImported;
  private List<RedirectSourceParameter> sourceParameters;
  private List<RedirectTargetParameter> targetParameters;

  public RedirectImpl(String id) {
    this.id = id;
  }

  /**
   * Creates a redirect with all properties that are displayed in the overview in the studio and can be edited by the
   * editor in the edit panel. This model class contains more properties than the redirect in CAE, because in Studio
   * additional properties like "isImported" are displayed, which are not needed in CAE.
   */
  public RedirectImpl(String id,
                      String siteId,
                      boolean active,
                      SourceUrlType sourceUrlType,
                      String source,
                      Date creationDate,
                      Content targetLink,
                      String targetUrl,
                      RedirectType redirectType,
                      String description,
                      boolean isImported,
                      List<RedirectSourceParameter> sourceParameters,
                      List<RedirectTargetParameter> targetParameters) {
    this.id = id;
    this.siteId = siteId;
    this.active = active;
    this.sourceUrlType = sourceUrlType;
    this.source = source;
    this.creationDate = creationDate;
    this.targetLink = targetLink;
    this.targetUrl = targetUrl;
    this.redirectType = redirectType;
    this.description = description;
    this.isImported = isImported;
    this.sourceParameters = sourceParameters;
    this.targetParameters = targetParameters;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getSiteId() {
    return siteId;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public SourceUrlType getSourceUrlType() {
    return sourceUrlType;
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public Content getTargetLink() {
    return targetLink;
  }

  @Override
  public String getTargetUrl() {
    return targetUrl;
  }

  @Override
  public RedirectType getRedirectType() {
    return redirectType;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isImported() {
    return isImported;
  }

  @Override
  public List<RedirectSourceParameter> getSourceParameters() {
    return sourceParameters;
  }

  @Override
  public List<RedirectTargetParameter> getTargetParameters() {
    return targetParameters;
  }

}
