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
