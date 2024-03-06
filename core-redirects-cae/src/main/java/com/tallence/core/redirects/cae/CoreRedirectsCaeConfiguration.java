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
package com.tallence.core.redirects.cae;

import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.tallence.core.redirects.cae.filter.RedirectFilter;
import com.tallence.core.redirects.cae.service.SiteRedirects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Filter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spring configuration for the redirects.
 */
@Configuration
public class CoreRedirectsCaeConfiguration {

  private static final String FILTER_NAME = "core-redirects";

  /**
   * Modify the default settings of the {@link RedirectFilter}.
   * <p>
   * The filter is registered by Spring Boot with default settings, which we need
   * to override.
   */
  @Bean
  public FilterRegistrationBean redirectFilterRegistration(RedirectFilter redirectFilter) {
    FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(redirectFilter);
    registration.setName(FILTER_NAME);
    // We want to redirect filter to run early in the chain in order to make it act faster.
    registration.setOrder(100);
    return registration;
  }

  /**
   * The central cache of redirects.
   */
  @Bean
  public ConcurrentMap<Site, SiteRedirects> redirectsCache(@Autowired SitesService sitesService) {
    // Resizing ConcurrentHashMaps is rather expensive, so we start at least with the correct size.
    return new ConcurrentHashMap<>(sitesService.getSites().size());
  }

}
