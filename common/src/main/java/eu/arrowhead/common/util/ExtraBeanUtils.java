/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.util;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

//NOTE might not be needed
public class ExtraBeanUtils {

  public static void copyNonNullProperties(Object source, Object destination, String... ignoreProperties) {
    String[] nullArray = getNullAndEmptyProperties(source);

    //This combines the ignoreProperties and nullArray arrays, without leaving duplicate elements
    Set<String> list = new LinkedHashSet<String>();
    list.addAll(Arrays.asList(nullArray));
    list.addAll(Arrays.asList(ignoreProperties));
    String[] mergedArray = list.toArray(new String[0]);

    BeanUtils.copyProperties(source, destination, mergedArray);
  }

  private static String[] getNullAndEmptyProperties(Object source) {
    final BeanWrapper src = new BeanWrapperImpl(source);
    PropertyDescriptor[] pds = src.getPropertyDescriptors();

    Set<String> emptyNames = new HashSet<String>();
    for (PropertyDescriptor pd : pds) {
      //check if value of this property is null then add it to the collection
      Object srcValue = src.getPropertyValue(pd.getName());
      if (srcValue == null) {
        emptyNames.add(pd.getName());
      }

      //If the property is a type that implements the Collection interface, check if the value is empty
      if (srcValue instanceof Collection) {
        if (((Collection) srcValue).isEmpty()) {
          emptyNames.add(pd.getName());
        }
      }
    }
    String[] result = new String[emptyNames.size()];
    return emptyNames.toArray(result);
  }
}
