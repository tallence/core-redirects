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

import com.tallence.core.redirects.cae.filter.RedirectFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  public FilterRegistrationBean getRedirectFilterRegistration(RedirectFilter redirectFilter) {
    FilterRegistrationBean registration = new FilterRegistrationBean(redirectFilter);
    registration.setName(FILTER_NAME);
    // We want to redirect filter to run early in the chain in order to make it faster.
    registration.setOrder(100);
    return registration;
  }

  /**
   * Executor for the recomputation of new redirects.
   */
  @Bean
  @Qualifier("redirectRecomputeThreadPool")
  public ExecutorService getRedirectRecomputeThreadPool(
          @Value("${core.redirects.cache.parallel.recompute.threads}") int threads) {
    return Executors.newFixedThreadPool(threads);
  }
}
