package com.tallence.core.redirects.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum represents the type of a redirect.
 * Redirects can be applied after a 404 error or always.
 */
public enum RedirectType {

  ALWAYS, AFTER_NOT_FOUND;

  private static final Map<String, RedirectType> LOOKUP = new HashMap<>(2);

  static {
    Arrays.stream(RedirectType.values()).forEach(redirectType -> LOOKUP.put(redirectType.name(), redirectType));
  }

  public static RedirectType asRedirectType(String type) {
    return LOOKUP.get(type);
  }

}
