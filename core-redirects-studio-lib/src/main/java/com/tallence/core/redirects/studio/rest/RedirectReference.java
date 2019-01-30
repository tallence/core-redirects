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
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

/**
 * A redirect reference used by the studio initialize the redirect remote beans. The redirect reference is a bean. The
 * studio sets the properties contained in the json directly. Thus the bean is already loaded. The target link of the
 * redirect must be loaded asynchronously if necessary. For the grid view the name of the target is also present in the
 * json.
 */
public class RedirectReference {

    @JsonProperty("$Bean")
    private String reference;
    private Boolean active;
    private Date creationDate;
    private RedirectType redirectType;
    private String source;
    private SourceUrlType sourceUrlType;
    private String targetLinkName;
    private TargetLink targetLink;
    private String description;
    private Boolean imported;
    private String siteId;


    RedirectReference(Redirect redirect) {
        this.reference = "redirect/" + redirect.getSiteId() + "/" + redirect.getId();
        this.active = redirect.isActive();
        this.creationDate = redirect.getCreationDate();
        this.redirectType = redirect.getRedirectType();
        this.source = redirect.getSource();
        this.sourceUrlType = redirect.getSourceUrlType();
        this.targetLinkName = redirect.getTargetLink().getName();
        if (redirect.getTargetLink() != null) {
            this.targetLink = new TargetLink(redirect.getTargetLink());
        }
        this.description = redirect.getDescription();
        this.imported = redirect.isImported();
        this.siteId = redirect.getSiteId();
    }

    public String getReference() {
        return reference;
    }

    public Boolean getActive() {
        return active;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public RedirectType getRedirectType() {
        return redirectType;
    }

    public String getSource() {
        return source;
    }

    public SourceUrlType getSourceUrlType() {
        return sourceUrlType;
    }

    public String getTargetLinkName() {
        return targetLinkName;
    }

    public TargetLink getTargetLink() {
        return targetLink;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getImported() {
        return imported;
    }

    public String getSiteId() {
        return siteId;
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