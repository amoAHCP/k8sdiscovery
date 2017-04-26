package org.jacpfx.util;

import java.lang.reflect.Field;

/**
 * Created by amo on 13.04.17.
 */
public class FieldUtil {
  public static void setFieldValue(Object bean, Field serviceNameField, Object value) {
    serviceNameField.setAccessible(true);
    try {
      serviceNameField.set(bean, value);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
