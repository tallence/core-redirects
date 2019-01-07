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
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;

import java.util.Date;

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
  private RedirectType redirectType;
  private String description;
  private boolean isImported;

  public RedirectImpl(String id, String siteId, boolean active, SourceUrlType sourceUrlType, String source, Date creationDate, Content targetLink, RedirectType redirectType, String description, boolean isImported) {
    this.id = id;
    this.siteId = siteId;
    this.active = active;
    this.sourceUrlType = sourceUrlType;
    this.source = source;
    this.creationDate = creationDate;
    this.targetLink = targetLink;
    this.redirectType = redirectType;
    this.description = description;
    this.isImported = isImported;
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
}