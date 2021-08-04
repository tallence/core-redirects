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

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.tallence.core.redirects.model.RedirectType;
import com.tallence.core.redirects.model.SourceUrlType;
import com.tallence.core.redirects.studio.model.Redirect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * A redirect reference used by the studio initialize the redirect remote beans. The redirect reference is a bean. The
 * studio sets the properties contained in the json directly. Thus the bean is already loaded. The target link of the
 * redirect must be loaded asynchronously if necessary. For the grid view the name of the target is also present in the
 * json.
 */
public class RedirectReference extends RedirectRepresentation {

  @JsonProperty("$Bean")
  private String reference;
  private String targetLinkName;
  private Boolean imported;


  RedirectReference(Redirect redirect) {
    super(redirect);
    this.reference = "redirect/" + redirect.getSiteId() + "/" + redirect.getId();
    if (redirect.getTargetLink() != null) {
      this.targetLinkName = redirect.getTargetLink().getName();
    } else if (redirect.getTargetUrl() != null) {
      this.targetLinkName = this.getTargetUrl();
    }
    this.imported = redirect.isImported();
  }

  public String getReference() {
    return reference;
  }

  public String getTargetLinkName() {
    return targetLinkName;
  }

  public Boolean getImported() {
    return imported;
  }

  private class TargetLink {

    @JsonProperty("$Ref")
    private String reference;

    TargetLink(Content target) {
      this.reference = "content/" + IdHelper.parseContentId(target.getId());
    }

    public String getReference() {
      return reference;
    }
  }

}
