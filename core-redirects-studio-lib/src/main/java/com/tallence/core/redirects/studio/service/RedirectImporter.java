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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallence.core.redirects.helper.RedirectHelper;
import com.tallence.core.redirects.model.RedirectSourceParameter;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A service to import redirects.
 */
public class RedirectImporter {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectImporter.class);

  private static final String INVALID_CSV_ENTRY = "length_invalid";
  private static final String DUPLICATE_SOURCE = "duplicate_source";
  private static final String CREATION_FAILURE = "creation_failure";
  private static final String PARSING_FAILURE = "parsing_failure";

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

      Map<String, RedirectUpdateProperties> imports = new HashMap<>();
      for (CSVRecord record : records) {
        String csvEntry = getCsvEntry(record);
        if (record.size() < 7) {
          redirectImportResponse.addErrorMessage(csvEntry, INVALID_CSV_ENTRY);
        } else {
          addIfNoDuplicate(siteId, redirectImportResponse, imports, record, csvEntry);

        }
      }

      imports.forEach((csvEntry, properties) -> createRedirect(siteId, csvEntry, properties, redirectImportResponse));
    } catch (IOException | IllegalArgumentException e) {
      redirectImportResponse.addErrorMessage("", CREATION_FAILURE);
      LOG.error("Error while processing uploaded file", e);
    }

    return redirectImportResponse;
  }

  private void addIfNoDuplicate(String siteId, RedirectImportResponse redirectImportResponse, Map<String, RedirectUpdateProperties> imports, CSVRecord record, String csvEntry) {
    //Add to map, if no duplicate sourceUrl
    try {
      RedirectUpdateProperties properties = mapToProperties(siteId, record);
      if (imports.values().stream().noneMatch(p -> sourcesMatch(properties, p))) {
        imports.put(csvEntry, properties);
      } else {
        redirectImportResponse.addErrorMessage(csvEntry, DUPLICATE_SOURCE);
      }
    } catch (JsonProcessingException e) {
      // could not parse source or target paramters
      redirectImportResponse.addErrorMessage(csvEntry, PARSING_FAILURE);
    }



  }

  private boolean sourcesMatch(RedirectUpdateProperties value, RedirectUpdateProperties p) {

    String source1 = Optional.ofNullable(p.getSource()).map(String::trim).orElse(null);
    String source2 = Optional.ofNullable(value.getSource()).map(String::trim).orElse(null);


    if (source1 != null && source1.equalsIgnoreCase(source2)) {
      List<RedirectSourceParameter> sourceParameters1 = value.getSourceParameters();
      List<RedirectSourceParameter> sourceParameters2 = p.getSourceParameters();
      return sourceParameters1.size() == sourceParameters2.size() && sourceParameters2.containsAll(sourceParameters1);
    }
    return false;
  }

  private RedirectUpdateProperties mapToProperties(String siteId, CSVRecord record) throws JsonProcessingException {

    Map<String, Object> properties = new HashMap<>();

    String active = record.get(0);
    properties.put(RedirectUpdateProperties.ACTIVE, Boolean.valueOf(active));

    String sourceUrlType = record.get(1);
    properties.put(RedirectUpdateProperties.SOURCE_URL_TYPE, sourceUrlType);

    String source = record.get(2);
    properties.put(RedirectUpdateProperties.SOURCE, source);

    Content targetLink = getTargetLink(record);
    properties.put(RedirectUpdateProperties.TARGET_LINK, targetLink);

    String targetUrl = record.get(4);
    properties.put(RedirectUpdateProperties.TARGET_URL, targetUrl);

    String redirectType = record.get(5);
    properties.put(RedirectUpdateProperties.REDIRECT_TYPE, redirectType);

    properties.put(RedirectUpdateProperties.DESCRIPTION, record.get(6));

    Optional<String> sourceParams = Optional.ofNullable(record.get(7));
    if (sourceParams.isPresent()) {
      properties.put(RedirectUpdateProperties.SOURCE_PARAMETERS, RedirectHelper.parseRedirectSourceParameters(sourceParams.get()));
    }

    Optional<String> targetParams = Optional.ofNullable(record.get(8));
    if (targetParams.isPresent()) {
      properties.put(RedirectUpdateProperties.TARGET_PARAMETERS, RedirectHelper.parseRedirectTargetParameters(targetParams.get()));
    }

    properties.put(RedirectUpdateProperties.IMPORTED, true);

    return new RedirectUpdateProperties(properties, redirectRepository, siteId, null);
  }

  /**
   * Creates a redirect for the given {@link RedirectUpdateProperties}. If the csv record is invalid,
   * corresponding error messages are added to the {@link RedirectImportResponse}.
   *
   * @param siteId     the site id.
   * @param csvEntry   the csv record.
   * @param properties the already created properties.
   * @param response   the redirect import response.
   */
  private void createRedirect(String siteId, String csvEntry, RedirectUpdateProperties properties, RedirectImportResponse response) {

    Map<String, String> errors = properties.validate();
    if (errors.isEmpty()) {
      try {
        Redirect redirect = redirectRepository.createRedirect(siteId, properties);
        response.addCreated(redirect);
      } catch (Exception e) {
        response.addErrorMessage(csvEntry, CREATION_FAILURE);
      }
    } else {
      errors.values().forEach(e -> response.addErrorMessage(csvEntry, e));
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
