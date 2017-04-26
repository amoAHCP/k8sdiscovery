package org.jacpfx.util;

/**
 * Created by amo on 06.04.17.
 */
public class StringUtil {

  /**
   * Determine if a string is {@code null} or {@link String#isEmpty()} returns {@code true}.
   */
  public static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
