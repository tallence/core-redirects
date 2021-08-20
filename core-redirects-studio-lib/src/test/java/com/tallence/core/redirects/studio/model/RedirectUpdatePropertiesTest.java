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
import com.tallence.core.redirects.studio.repository.RedirectRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.ACTIVE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.INVALID_ACTIVE_VALUE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.INVALID_REDIRECT_TYPE_VALUE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.INVALID_SOURCE_URL_TYPE_VALUE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.INVALID_SOURCE_VALUE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.INVALID_SOURCE_WHITESPACE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.INVALID_TARGET_LINK;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.MISSING_TARGET_LINK;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.REDIRECT_TYPE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.SOURCE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.SOURCE_ALREADY_EXISTS;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.SOURCE_URL_TYPE;
import static com.tallence.core.redirects.studio.model.RedirectUpdateProperties.TARGET_LINK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link RedirectUpdateProperties}
 */
public class RedirectUpdatePropertiesTest {

  private RedirectRepository repository;

  @Before
  public void setUp() {
    repository = mock(RedirectRepository.class);
  }

  /**
   * Tests an update request, which contains invalid values for the enum properties. It should result in a
   * validation error.
   */
  @Test
  public void testUpdateValidation() {

    Map<String, Object> properties = new HashMap<>();
    properties.put(SOURCE_URL_TYPE, "123");
    properties.put(REDIRECT_TYPE, "123");

    RedirectUpdateProperties updateProperties = new RedirectUpdateProperties(properties, repository, null, "123");

    Map<String, String> errors = updateProperties.validate();

    assertThat(errors.get(SOURCE_URL_TYPE), equalTo(INVALID_SOURCE_URL_TYPE_VALUE));
    assertThat(errors.get(REDIRECT_TYPE), equalTo(INVALID_REDIRECT_TYPE_VALUE));

  }

  @Test
  public void testCreateValidationInvalidEnumsAndSource() {

    Content targetLink = mock(Content.class);
    when(repository.targetIsInvalid(eq(targetLink))).thenReturn(false);

    Map<String, Object> properties = new HashMap<>();
    properties.put(ACTIVE, true);
    properties.put(SOURCE, "invalid_url");
    properties.put(SOURCE_URL_TYPE, "123");
    properties.put(REDIRECT_TYPE, "123");
    properties.put(TARGET_LINK, targetLink);

    RedirectUpdateProperties creationProperties = new RedirectUpdateProperties(properties, repository, null, null);

    Map<String, String> errors = creationProperties.validate();

    assertThat(errors.size(), equalTo(3));
    assertThat(errors.get(SOURCE_URL_TYPE), equalTo(INVALID_SOURCE_URL_TYPE_VALUE));
    assertThat(errors.get(REDIRECT_TYPE), equalTo(INVALID_REDIRECT_TYPE_VALUE));
    assertThat(errors.get(SOURCE), equalTo(INVALID_SOURCE_VALUE));

  }

  @Test
  public void testUpdateValidationWhitespacesSource() {
    Map<String, Object> properties = new HashMap<>();
    // leading and ending whitespaces will be ignored (String#trim())
    properties.put(SOURCE, "/url-with- whitespaces  ");

    RedirectUpdateProperties updateProperties = new RedirectUpdateProperties(properties, repository, null, "123");

    Map<String, String> errors = updateProperties.validate();
    assertThat(errors.get(SOURCE), equalTo(INVALID_SOURCE_WHITESPACE));

  }


  @Test
  public void testCreateValidationInvalidTargetLink() {

    Content targetLink = mock(Content.class);
    when(repository.targetIsInvalid(eq(targetLink))).thenReturn(true);

    Map<String, Object> properties = new HashMap<>();
    properties.put(ACTIVE, true);
    properties.put(SOURCE, "/valid-url");
    properties.put(SOURCE_URL_TYPE, "PLAIN");
    properties.put(REDIRECT_TYPE, "ALWAYS");
    properties.put(TARGET_LINK, targetLink);

    RedirectUpdateProperties creationProperties = new RedirectUpdateProperties(properties, repository, null, null);

    Map<String, String> errors = creationProperties.validate();

    assertThat(errors.size(), equalTo(1));
    assertThat(errors.get(TARGET_LINK), equalTo(INVALID_TARGET_LINK));
  }

  @Test
  public void testCreateValidationNoFields() {

    Map<String, Object> properties = new HashMap<>();

    RedirectUpdateProperties creationProperties = new RedirectUpdateProperties(properties, repository, null, null);

    Map<String, String> errors = creationProperties.validate();

    assertThat(errors.size(), equalTo(5));
    assertThat(errors.get(ACTIVE), equalTo(INVALID_ACTIVE_VALUE));
    assertThat(errors.get(TARGET_LINK), equalTo(MISSING_TARGET_LINK));
    assertThat(errors.get(SOURCE_URL_TYPE), equalTo(INVALID_SOURCE_URL_TYPE_VALUE));
    assertThat(errors.get(REDIRECT_TYPE), equalTo(INVALID_REDIRECT_TYPE_VALUE));
    assertThat(errors.get(SOURCE), equalTo(INVALID_SOURCE_VALUE));
  }

  @Test
  public void testCreateValidationExistingSource() {

    when(repository.sourceAlreadyExists(any(), ArgumentMatchers.eq("/existing_source"), eq(List.of()))).thenReturn(true);

    Map<String, Object> properties = new HashMap<>();
    properties.put(SOURCE, "/existing_source");

    RedirectUpdateProperties creationProperties = new RedirectUpdateProperties(properties, repository, null, null);

    Map<String, String> errors = creationProperties.validate();

    assertThat(errors.get(SOURCE), equalTo(SOURCE_ALREADY_EXISTS));

  }
}
