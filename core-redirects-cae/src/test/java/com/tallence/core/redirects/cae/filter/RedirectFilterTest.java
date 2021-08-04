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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RedirectFilterTest extends AbstractRedirectsTest {

  @Autowired
  private RedirectFilter testling;

  @Test
  public void testRedirect() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = createRequest("/channela/redirect-test").buildRequest(servletContext);
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
    HttpServletRequest request = createRequest("/channela/redirect-test2/abc").buildRequest(servletContext);
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
    HttpServletRequest request = createRequest("/channela/redirect-test2/abc").buildRequest(servletContext);
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
    HttpServletRequest request = createRequest("/channela/redirect-test2/abc").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getServerErrorServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    assertThat(response.getHeader(HttpHeaders.LOCATION), is(nullValue()));
    assertThat(response.getHeader(HttpHeaders.EXPIRES), is(nullValue()));
  }

  @Test
  public void testKeepParams() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = createRequest("/channela/redirect-test").param("param1", "testValue1").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getOkServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    String expectedUrl = "/channela?param1=testValue1";
    assertThat(response.getHeader(HttpHeaders.LOCATION), anyOf(is("/context/servlet" + expectedUrl), is(expectedUrl)));
  }

  @Test
  public void testKeepParamsWithSpecialChar() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = createRequest("/channela/redirect-speciél-char").param("param1", "testVálüe1").buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getOkServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    //The special characters are url-encoded, the decode result looks like: /channela/chánnelง?param1=testVálüe1
    String expectedUrl = "/channela/ch%C3%A1nnel%E0%B8%87?param1=testV%C3%A1l%C3%BCe1";
    assertThat(response.getHeader(HttpHeaders.LOCATION), anyOf(is("/context/servlet" + expectedUrl), is(expectedUrl)));
  }

  @Test
  public void testSourceParamsSingle() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = createRequest("/channela/redirect-with-param")
            .param("importantParam", "testValue1")
            .param("irrelevantParam", "testValue2")
            .buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getOkServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    String expectedUrl = "/channela?irrelevantParam=testValue2&importantParam=rewrittenValue&targetUrlParam=targetValue";
    assertThat(response.getHeader(HttpHeaders.LOCATION), anyOf(is("/context/servlet" + expectedUrl), is(expectedUrl)));
  }

  @Test
  public void testSourceParamsMultiple() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = createRequest("/channela/redirect-with-param-multiple")
            .param("importantParam", "testValue1")
            .param("importantParam2", "testValue2")
            .param("irrelevantParam", "testValue2")
            .buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getOkServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    // This assertion is a bit strange, because it depends of the context of the extension: If the testcases in this
    // extension are running stand-alone, the blueprint link rewriter that removes the /context/servlet part is missing.
    String expectedUrl = "/channela?importantParam=testValue1&importantParam2=testValue2&irrelevantParam=testValue2";
    assertThat(response.getHeader(HttpHeaders.LOCATION), anyOf(is("/context/servlet" + expectedUrl), is(expectedUrl)));
  }

  @Test
  public void testTargetUrl() throws Exception {
    MockServletContext servletContext = new MockServletContext();
    HttpServletRequest request = createRequest("/channela/redirect-without-document-target")
            .buildRequest(servletContext);
    HttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = new MockFilterChain(getOkServlet());

    testling.doFilter(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    assertThat(response.getHeader(HttpHeaders.LOCATION), is("https://github.com/tallence/core-redirects"));
  }

  private MockHttpServletRequestBuilder createRequest(String shortUrl) throws URISyntaxException {
    return MockMvcRequestBuilders
            .get(new URI("/context/servlet" + shortUrl))
            .contextPath("/context")
            .servletPath("/servlet")
            .characterEncoding("UTF-8");
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

  private static class ResponseOnlyServlet implements Servlet {

    private final int status;

    ResponseOnlyServlet(int status) {
      this.status = status;
    }

    @Override
    public void init(ServletConfig servletConfig) {
    }

    @Override
    public ServletConfig getServletConfig() {
      return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) {
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
