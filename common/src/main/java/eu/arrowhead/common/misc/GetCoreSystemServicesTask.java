/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.misc;

import eu.arrowhead.common.Utility;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimerTask;

public class GetCoreSystemServicesTask extends TimerTask {

  private Object context;
  private List<String> serviceDefs = new ArrayList<>();
  private Map<String, String[]> uriMap = new HashMap<>();

  public GetCoreSystemServicesTask(Object context, List<String> serviceDefs) {
    if (context instanceof NeedsCoreSystemService) {
      this.context = context;
    } else {
      throw new AssertionError("This task can only be called with a class that implements the NeedsCoreSystemService interface!");
    }
    this.serviceDefs = serviceDefs;
  }

  @Override
  public void run() {
    for (String serviceDef : serviceDefs) {
      Optional<String[]> optionalUri = Utility.getServiceInfo(serviceDef);
      optionalUri.ifPresent(uri -> uriMap.put(serviceDef, uri));
    }

    try {
      Method method = context.getClass().getMethod("getCoreSystemServiceUris", Map.class);
      method.invoke(context, uriMap);
    } catch (Exception e) {
      throw new AssertionError("Exception occurred with Java reflection!", e);
    }
  }

}
