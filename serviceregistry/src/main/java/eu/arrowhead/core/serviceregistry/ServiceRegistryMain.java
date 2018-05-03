package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDRegistrator;
import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.misc.CoreSystem;
import eu.arrowhead.common.misc.TypeSafeProperties;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceRegistryMain extends ArrowheadMain {

  static int PING_TIMEOUT;
  //DNS-SD global settings
  static final String TSIG_NAME;
  static final String TSIG_KEY;
  static final String TSIG_ALGORITHM;
  static final String DNS_ADDRESS;
  static final String DNS_DOMAIN;
  static final int DNS_PORT;
  static String DNS_REGISTRATOR_DOMAIN;

  private static final TypeSafeProperties dnsProp = Utility.getProp("dns.properties");

  static {
    TSIG_NAME = dnsProp.getProperty("tsig_name", "key.arrowhead.tmit.bme.hu");
    TSIG_KEY = dnsProp.getProperty("tsig_key", "RM/jKKEPYB83peT0DQnYGg==");
    TSIG_ALGORITHM = dnsProp.getProperty("tsig_algorithm", DnsSDRegistrator.TSIG_ALGORITHM_HMAC_MD5);
    DNS_ADDRESS = dnsProp.getProperty("dns_address", "152.66.246.237");
    DNS_DOMAIN = dnsProp.getProperty("dns_domain", "arrowhead.tmit.bme.hu");
    DNS_PORT = dnsProp.getIntProperty("dns_port", 53);
    DNS_REGISTRATOR_DOMAIN = dnsProp.getProperty("dns_registrator_domain", "srv.arrowhead.tmit.bme.hu.");
  }

  {
    PING_TIMEOUT = props.getIntProperty("ping_timeout", 10000);
  }

  private ServiceRegistryMain(String[] args) {
    Set<Class<?>> classes = new HashSet<>(Collections.singleton(ServiceRegistryResource.class));
    String[] packages = {"eu.arrowhead.common", "eu.arrowhead.core.serviceregistry.filter"};
    init(CoreSystem.SERVICE_REGISTRY_DNS, args, classes, packages);

    System.setProperty("dns.server", DNS_ADDRESS);
    System.setProperty("dnssd.domain", DNS_DOMAIN);
    System.setProperty("dnssd.hostname", dnsProp.getProperty("dns_host", "localhost"));
    if (!DNS_REGISTRATOR_DOMAIN.endsWith(".")) {
      DNS_REGISTRATOR_DOMAIN = DNS_REGISTRATOR_DOMAIN.concat(".");
    }

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
      int interval = props.getIntProperty("ttl_interval", 10);
      ttlTimer.schedule(removeTask, 45L * 1000L, interval * 60L * 1000L);
    }

    listenForInput();
  }

  public static void main(String[] args) {
    new ServiceRegistryMain(args);
  }

}
