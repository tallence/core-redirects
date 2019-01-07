package com.tallence.core.redirects.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum represents the type of the source url.
 * An url can either be absolute or a regular expression.
 */
public enum SourceUrlType {
  REGEX, ABSOLUTE;

  private static final Map<String, SourceUrlType> LOOKUP = new HashMap<>(2);

  static {
    Arrays.stream(SourceUrlType.values()).forEach(sourceUrlType -> LOOKUP.put(sourceUrlType.name(), sourceUrlType));
  }

  public static SourceUrlType asSourceUrlType(String type) {
    return LOOKUP.get(type);
  }
}
