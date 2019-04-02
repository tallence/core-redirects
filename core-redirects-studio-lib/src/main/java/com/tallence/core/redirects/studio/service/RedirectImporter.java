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

package com.tallence.core.redirects.studio.service;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.tallence.core.redirects.studio.model.Redirect;
import com.tallence.core.redirects.studio.model.RedirectUpdateProperties;
import com.tallence.core.redirects.studio.repository.RedirectRepository;
import com.tallence.core.redirects.studio.rest.RedirectImportResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A service to import redirects.
 */
public class RedirectImporter {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectImporter.class);

  private static final String INVALID_CSV_ENTRY = "length_invalid";
  private static final String INVALID_ACTIVE_VALUE = "active_invalid";
  private static final String INVALID_SOURCE_URL_TYPE_VALUE = "sourceUrlType_invalid";
  private static final String INVALID_SOURCE_VALUE = "source_invalid";
  private static final String SOURCE_ALREADY_EXISTS = "source_already_exists";
  private static final String INVALID_TAGET_LINK_VALUE = "targetLink_invalid";
  private static final String INVALID_REDIRECT_TYPE_VALUE = "redirectType_invalid";
  private static final String CREATION_FAILURE = "creation_failure";

  private final RedirectRepository redirectRepository;
  private final ContentRepository contentRepository;

  @Autowired
  public RedirectImporter(RedirectRepository redirectRepository, ContentRepository contentRepository) {
    this.redirectRepository = redirectRepository;
    this.contentRepository = contentRepository;
  }

  /**
   * Imports all redirects for the given site.
   *
   * @param siteId      the site id.
   * @param inputStream the input stream for the csv file.
   * @return {@link RedirectImportResponse}.
   */
  public RedirectImportResponse importRedirects(String siteId, InputStream inputStream) {
    RedirectImportResponse redirectImportResponse = new RedirectImportResponse();
    try {
      Iterable<CSVRecord> records = CSVFormat.EXCEL
          .withDelimiter(';')
          .withFirstRecordAsHeader()
          .parse(new InputStreamReader(inputStream));
      for (CSVRecord record : records) {
        if (record.size() < 6) {
          redirectImportResponse.addErrorMessage(getCsvEntry(record), INVALID_CSV_ENTRY);
        } else {
          createRedirect(siteId, record, redirectImportResponse);
        }
      }
    } catch (IOException e) {
      redirectImportResponse.addErrorMessage("", CREATION_FAILURE);
      LOG.error("Error while processing uploaded file", e);
    }

    return redirectImportResponse;
  }

  /**
   * Creates a redirect for the given {@link CSVRecord}. If the csv record is invalid,
   * corresponding error messages are added to the {@link RedirectImportResponse}.
   *
   * @param siteId   the site id.
   * @param record   the csv record.
   * @param response the redirect import response.
   */
  private void createRedirect(String siteId, CSVRecord record, RedirectImportResponse response) {
    boolean valid = true;
    String csvEntry = getCsvEntry(record);

    Map<String, Object> updateProperties = new HashMap<>();

    String active = record.get(0);
    if ((active != null) &&
        !(active.equalsIgnoreCase("true") || active.equalsIgnoreCase("false"))) {
      valid = false;
      response.addErrorMessage(csvEntry, INVALID_ACTIVE_VALUE);
    } else {
      updateProperties.put(RedirectUpdateProperties.ACTIVE, Boolean.valueOf(active));
    }

    String sourceUrlType = record.get(1);
    if (StringUtils.isEmpty(sourceUrlType)) {
      valid = false;
      response.addErrorMessage(csvEntry, INVALID_SOURCE_URL_TYPE_VALUE);
    } else {
      updateProperties.put(RedirectUpdateProperties.SOURCE_URL_TYPE, sourceUrlType);
    }

    String source = record.get(2);
    if (StringUtils.isEmpty(source) || !redirectRepository.sourceIsValid(source)) {
      valid = false;
      response.addErrorMessage(csvEntry, INVALID_SOURCE_VALUE);
    } else if (redirectRepository.sourceAlreadyExists(siteId, source)) {
      valid = false;
      response.addErrorMessage(csvEntry, SOURCE_ALREADY_EXISTS);
    } else {
      updateProperties.put(RedirectUpdateProperties.SOURCE, source);
    }

    Content targetLink = getTargetLink(record);
    if (targetLink == null) {
      valid = false;
      response.addErrorMessage(csvEntry, INVALID_TAGET_LINK_VALUE);
    } else if (active.equalsIgnoreCase("true") && redirectRepository.targetIsInvalid(targetLink)) {
      valid = false;
      response.addErrorMessage(csvEntry, INVALID_TAGET_LINK_VALUE);
    } else {
      updateProperties.put(RedirectUpdateProperties.TARGET_LINK, targetLink);
    }

    String redirectType = record.get(4);
    if (StringUtils.isEmpty(redirectType)) {
      valid = false;
      response.addErrorMessage(csvEntry, INVALID_REDIRECT_TYPE_VALUE);
    } else {
      updateProperties.put(RedirectUpdateProperties.REDIRECT_TYPE, redirectType);
    }

    updateProperties.put(RedirectUpdateProperties.DESCRIPTION, record.get(5));
    updateProperties.put(RedirectUpdateProperties.IMPORTED, true);
    if (valid) {
      try {
        Redirect redirect = redirectRepository.createRedirect(siteId, new RedirectUpdateProperties(updateProperties));
        response.addCreated(redirect);
      } catch (Exception e) {
        response.addErrorMessage(getCsvEntry(record), CREATION_FAILURE);
      }
    }
  }

  /**
   * Returns the linked content. If no content is found using an id or path, null is returned.
   *
   * @param csvRecord the csv record.
   * @return the target content.
   */
  private Content getTargetLink(CSVRecord csvRecord) {
    String targetLink = csvRecord.get(3);
    if (StringUtils.isEmpty(targetLink)) {
      return null;
    } else if (IdHelper.isContentId(targetLink)) {
      return contentRepository.getContent(targetLink);
    } else if (targetLink.matches("\\d+")) {
      String contentId = IdHelper.formatContentId(Integer.valueOf(targetLink));
      return contentRepository.getContent(contentId);
    }
    return contentRepository.getRoot().getChild(targetLink);
  }

  /**
   * Converts the {@link CSVRecord} to a string representing the csv line.
   *
   * @param record the csv record.
   * @return string.
   */
  private String getCsvEntry(CSVRecord record) {
    StringBuilder entry = new StringBuilder();
    for (String column : record) {
      if (entry.length() > 0) {
        entry.append(";");
      }
      entry.append(column);
    }
    return entry.toString();
  }
}
