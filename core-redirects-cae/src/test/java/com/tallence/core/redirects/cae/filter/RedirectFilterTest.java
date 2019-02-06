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

import com.tallence.core.redirects.cae.AbstractRedirectsTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class RedirectFilterTest extends AbstractRedirectsTest {

  @Autowired
  private RedirectFilter testling;

  @Test
  public void testRedirect() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = requestTestHelper.createRequest("/channela/redirect-test").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getOkServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    assertThat(response.getHeader(HttpHeaders.LOCATION), anyOf(is("/channela"), is("/context/servlet/channela")));
    assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", response.getHeader(HttpHeaders.EXPIRES));
  }

  @Test
  public void testRegexRedirect() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = requestTestHelper.createRequest("/channela/redirect-test2/abc").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    // We fake a 404 error from the controller
    FilterChain filterChain = new MockFilterChain(getNotFoundServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    assertThat(response.getHeader(HttpHeaders.LOCATION), anyOf(is("/channela"), is("/context/servlet/channela")));
    assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", response.getHeader(HttpHeaders.EXPIRES));
  }

  @Test
  public void testRegexNotRedirected() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = requestTestHelper.createRequest("/channela/redirect-test2/abc").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getOkServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    assertThat(response.getHeader(HttpHeaders.LOCATION), is(nullValue()));
    assertThat(response.getHeader(HttpHeaders.EXPIRES), is(nullValue()));
  }

  @Test
  public void testServerErrorNotRedirected() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = requestTestHelper.createRequest("/channela/redirect-test2/abc").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getServerErrorServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    assertThat(response.getHeader(HttpHeaders.LOCATION), is(nullValue()));
    assertThat(response.getHeader(HttpHeaders.EXPIRES), is(nullValue()));
  }


  private Servlet getOkServlet() {
    return new ResponseOnlyServlet(HttpServletResponse.SC_OK);
  }

  private Servlet getNotFoundServlet() {
    return new ResponseOnlyServlet(HttpServletResponse.SC_NOT_FOUND);
  }

  private Servlet getServerErrorServlet() {
    return new ResponseOnlyServlet(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  private class ResponseOnlyServlet implements Servlet {

    private int status;

    ResponseOnlyServlet(int status) {
      this.status = status;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
    }

    @Override
    public ServletConfig getServletConfig() {
      return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
      ((HttpServletResponse) servletResponse).setStatus(status);
    }

    @Override
    public String getServletInfo() {
      return null;
    }

    @Override
    public void destroy() {

    }
  }
}
