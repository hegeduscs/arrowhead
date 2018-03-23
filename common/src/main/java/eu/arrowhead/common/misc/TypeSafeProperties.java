package eu.arrowhead.common.misc;

import java.util.Properties;

public class TypeSafeProperties extends Properties {

  public int getIntProperty(String key, int defaultValue) {
    String val = getProperty(key);
    try {
      return (val == null) ? defaultValue : Integer.valueOf(val);
    } catch (NumberFormatException e) {
      System.out
          .println(val + " is not a valid number! Please fix the \"" + key + "\" property! Using default value (" + defaultValue + ") instead!");
      return defaultValue;
    }
  }

  //NOTE add more data types later if needed

}
