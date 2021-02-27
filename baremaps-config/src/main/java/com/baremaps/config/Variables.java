package com.baremaps.config;

import java.util.Map;
import java.util.Map.Entry;

/**
 * A utility class to interpolate variables in strings.
 */
public class Variables {

  public static String interpolate(Map<String, String> variables, String string) {
    for (Entry<String, String> entry : variables.entrySet()) {
      string = string.replace(String.format("${%s}", entry.getKey()), entry.getValue());
    }
    return string;
  }

}
