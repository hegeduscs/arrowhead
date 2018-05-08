/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.core.serviceregistry_sql.support.OldServiceRegResource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceRegistryMain extends ArrowheadMain {

  static int PING_TIMEOUT;
  static int TTL_INTERVAL;

  {
    PING_TIMEOUT = props.getIntProperty("ping_timeout", 7500);
    TTL_INTERVAL = props.getIntProperty("ttl_interval", 10);
  }

  private ServiceRegistryMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Arrays.asList(ServiceRegistryResource.class, ServiceRegistryApi.class, OldServiceRegResource.class));
    String[] packages = {"eu.arrowhead.common", "eu.arrowhead.core.serviceregistry_sql.filter"};
    init(CoreSystem.SERVICE_REGISTRY_SQL, args, classes, packages);

    //if provider ping is scheduled, start the TimerTask that provides it
    if (props.getBooleanProperty("ping_scheduled", false)) {
      TimerTask pingTask = new PingProvidersTask();
      Timer pingTimer = new Timer();
      int interval = props.getIntProperty("ping_interval", 60);
      pingTimer.schedule(pingTask, 60L * 1000L, (interval * 60L * 1000L));
    }
    //if TTL based service removing is scheduled, start the TimerTask that provides it
    if (props.getBooleanProperty("ttl_scheduled", false)) {
      TimerTask removeTask = new RemoveExpiredServicesTask();
      Timer ttlTimer = new Timer();
      ttlTimer.schedule(removeTask, 45L * 1000L, TTL_INTERVAL * 60L * 1000L);
    }

    listenForInput();
  }

  public static void main(String[] args) {
    new ServiceRegistryMain(args);
  }

}
