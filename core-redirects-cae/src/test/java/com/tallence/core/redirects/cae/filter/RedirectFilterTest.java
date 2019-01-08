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
package com.tallence.core.redirects.cae.filter;

import com.tallence.core.redirects.cae.service.cache.RedirectFolderCacheKeyFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RedirectFilterTest extends AbstractRedirectsTest {

  @Autowired
  private RedirectFilter testling;

  @Autowired
  private RedirectFolderCacheKeyFactory folderCacheKeyFactory;

  @Before
  public void init() {
    folderCacheKeyFactory.setTestmode(true);
  }

  @Test
  public void testRedirect() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = requestTestHelper.createRequest("/channela/redirect-test").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain();

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    assertThat(response.getHeader(HttpHeaders.LOCATION), anyOf(is("/channela"), is("/context/servlet/channela")));
    assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", response.getHeader(HttpHeaders.EXPIRES));
  }

}
