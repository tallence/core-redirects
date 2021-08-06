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

import com.coremedia.blueprint.common.contentbeans.CMLinkable;
import com.coremedia.objectserver.beans.ContentBeanFactory;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.tallence.core.redirects.cae.filter.RedirectMatchingService.Result;
import com.tallence.core.redirects.cae.model.Redirect;
import com.tallence.core.redirects.model.RedirectTargetParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.utils.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.web.util.UriUtils.encodeQueryParam;

/**
 * Filter for the handling of redirects.
 */
@Service
public class RedirectFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(RedirectFilter.class);
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private final ContentBeanFactory contentBeanFactory;
  private final LinkFormatter linkFormatter;
  private final RedirectMatchingService redirectMatchingService;
  private final boolean keepSourceUrlParams;

  @Autowired
  public RedirectFilter(ContentBeanFactory contentBeanFactory,
                        @Value("${core.redirects.filter.keepParams:false}")
                        boolean keepSourceUrlParams,
                        RedirectMatchingService redirectMatchingService,
                        LinkFormatter linkFormatter) {
    this.contentBeanFactory = contentBeanFactory;
    this.linkFormatter = linkFormatter;
    this.redirectMatchingService = redirectMatchingService;
    this.keepSourceUrlParams = keepSourceUrlParams;
  }


  /**
   * Match the request for possible redirects and handle accordingly.
   */
  @Override
  public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException, ServletException {
    // Cast
    HttpServletRequest request = (HttpServletRequest) srequest;
    HttpServletResponse response = (HttpServletResponse) sresponse;
    RedirectHttpServletResponseWrapper wrapper = null;

    // Pre-handle
    final Result result = redirectMatchingService.getMatchingRedirect(request);
    if (result.getAction() == Result.Action.SEND) {
      sendPermanentRedirect(request, response, result.getRedirect());
      return;
    } else if (result.getAction() == Result.Action.WRAP) {
      // Because we might have to modify the response, we need to wrap it in order to prevent tomcat from starting
      // to write to the wire before we have inspected it.
      wrapper = new RedirectHttpServletResponseWrapper(response, result.getRedirect());
    }

    // Let the actual controller do its thing (with the wrapper, if one is set)
    chain.doFilter(request, wrapper == null ? response : wrapper);

    // Posthandle only on 404
    if (wrapper != null) {
      if (wrapper.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
        // Ignore response and send redirect
        Redirect redirect = wrapper.getRedirect();
        sendPermanentRedirect(request, response, redirect);
      } else {
        // Write cached status code to response
        wrapper.writeOnSuper();
      }
    }
  }


  // FILTER DEFAULT METHODS

  @Override
  public void init(FilterConfig filterConfig) {
    // Nothing to do here
  }

  @Override
  public void destroy() {
    // Nothing to do here
  }


  // HELPER METHODS

  /**
   * Executes the actual redirect.
   * TODO Currently, this code always does a 301 with instant expiry. This should be made configurable.
   */
  private void sendPermanentRedirect(HttpServletRequest request, HttpServletResponse response, Redirect target) {
    if (target.hasNoTarget()) {
      LOG.error("Unable to redirect to empty string for redirect {}", target);
      return;
    }

    LOG.debug("Redirecting to {}", target);

    // Reset content and headers
    response.reset();

    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
    response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    response.setDateHeader(HttpHeaders.EXPIRES, 0);

    String targetLink = Optional.ofNullable(target.getTarget())
            .map(t -> contentBeanFactory.createBeanFor(t, CMLinkable.class))
            .map(t -> linkFormatter.formatLink(t, null, request, response, true))
            .orElse(target.getTargetUrl());

    try {
      targetLink = handleParameters(request, targetLink, target.getTargetParameters());
    } catch (RuntimeException e) {
      LOG.warn("Error during handling query params [{}] of source url [{}]: [{}]. The query params will be ignored.",
              Arrays.toString(request.getParameterMap().entrySet().toArray()), request.getPathInfo(), e.getMessage());
    }

    response.setHeader(HttpHeaders.LOCATION, targetLink);
  }

  private String handleParameters(HttpServletRequest request, String targetLink, List<RedirectTargetParameter> targetParameters) {
    Map<String, String[]> parameterMap = Optional.ofNullable(request.getParameterMap()).orElse(Collections.emptyMap());
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(targetLink);

    if (keepSourceUrlParams && !parameterMap.isEmpty()) {
      //Keep the request params if they are not already used by the targetParams of the redirect
      parameterMap.entrySet().stream()
              .filter(e -> targetParameters.stream().noneMatch(t -> t.getName().equalsIgnoreCase(e.getKey())))
              .forEach(e -> mapUrlParam(uriBuilder, e));
    }
    targetParameters.forEach(t -> uriBuilder.queryParam(t.getName(), encodeQueryParam(t.getValue(), UTF8)));

    return uriBuilder.build(true).toString();
  }

  private void mapUrlParam(UriComponentsBuilder uriBuilder, Map.Entry<String, String[]> entry) {
    Arrays.stream(entry.getValue())
            .map(v -> encodeQueryParam(v, UTF8))
            .forEach(v -> uriBuilder.queryParam(entry.getKey(), v));
  }
}
