package com.tallence.core.redirects.cae.filter;

/**
 * Thrown on errors in the configuration of the redirects.
 */
public class RedirectConfigurationException extends RuntimeException {

  public RedirectConfigurationException(String message) {
    super(message);
  }

}
