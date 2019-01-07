package com.tallence.core.redirects.cae;

import com.tallence.core.redirects.cae.filter.RedirectFilter;
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
  public ExecutorService getRedirectCacheKeyRecomputeThreadPool(
          @Value("${core.redirects.cache.parallel.recompute.threads}") int threads) {
    return Executors.newFixedThreadPool(threads);
  }
}
