/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDException;
import com.github.danieln.dnssdjava.DnsSDFactory;
import com.github.danieln.dnssdjava.DnsSDRegistrator;
import com.github.danieln.dnssdjava.ServiceData;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.DnsException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class RegistryUtils {

  private static final Logger log = Logger.getLogger(RegistryUtils.class.getName());

  static DnsSDRegistrator createRegistrator() throws DnsSDException {
    // Get the DNS specific settings from global static variables (from prop files) and then set up Registrator
    InetSocketAddress dnsServerAddress = new InetSocketAddress(ServiceRegistryMain.DNS_ADDRESS, ServiceRegistryMain.DNS_PORT);
    DnsSDRegistrator registrator = DnsSDFactory.getInstance().createRegistrator(ServiceRegistryMain.DNS_REGISTRATOR_DOMAIN, dnsServerAddress);
    if (!ServiceRegistryMain.TSIG_NAME.isEmpty()) {
      registrator.setTSIGKey(ServiceRegistryMain.TSIG_NAME, ServiceRegistryMain.TSIG_ALGORITHM, ServiceRegistryMain.TSIG_KEY);
    }
    return registrator;
  }

  public static String removeLastChar(String host, char charachter) {
    if (host != null && host.length() > 0 && host.charAt(host.length() - 1) == charachter) {
      return host.substring(0, host.length() - 1);
    }
    return host;
  }

  public static void setServiceDataProperties(ServiceRegistryEntry registryEntry, ServiceData data) {
    //setting up everything for the TXT record
    Map<String, String> properties = data.getProperties();

    if (registryEntry.getProvider() == null) {
      return;
    }

    //Arrowhead core-related metadata
    if (registryEntry.getProvider().getSystemName() != null) {
      properties.put("ahsysname", registryEntry.getProvider().getSystemName());
    }
    if (registryEntry.getProvider().getAuthenticationInfo() != null) {
      properties.put("ahsysauth", registryEntry.getProvider().getAuthenticationInfo());
    }
    if (registryEntry.getServiceURI() != null) {
      properties.put("path", registryEntry.getServiceURI());
    }

    LocalDateTime date = registryEntry.getEndOfValidity();
    String encodedDate = String.valueOf(date.getYear()).concat(".").concat(String.valueOf(date.getMonthValue())).concat(".").concat(String.valueOf(date.getDayOfMonth())).concat(".").concat(String.valueOf(date.getHour())).concat(".")
                               .concat(String.valueOf(date.getMinute())).concat(".").concat(String.valueOf(date.getSecond()));
    properties.put("eovdate", encodedDate);

    //additional user metadata
    if (!registryEntry.getProvidedService().getServiceMetadata().isEmpty()) {
      for (Map.Entry<String, String> entry : registryEntry.getProvidedService().getServiceMetadata().entrySet()) {
        properties.put("ahsrvmetad_" + entry.getKey(), entry.getValue());
      }
    }

    //As per the DNS-SD standard on service versioning
    properties.put("txtvers", Integer.toString(registryEntry.getVersion()));
  }


  public static ServiceRegistryEntry buildRegistryEntry(ServiceData service) throws IllegalArgumentException {

    //extracting fields from DNS record
    String providerName = service.getName().getName();
    String serviceName = service.getName().getType().toString();
    String address = removeLastChar(service.getHost(), '.');
    int port = service.getPort();
    Map<String, String> properties = service.getProperties();

    if (providerName == null || serviceName == null || address == null) {
      throw new IllegalArgumentException("DNS entry is empty somehow!");
    }

    //building ArrowheadService object from fields
    ArrowheadService arrowheadService = new ArrowheadService();

    //need to differentiate between TCP and UDP
    int serviceNameLength;
    boolean isUDP = false;
    if (serviceName.contains("_tcp")) {
      serviceNameLength = serviceName.indexOf("._tcp");
    } else {
      if (serviceName.contains("_udp")) {
        serviceNameLength = serviceName.indexOf("._udp");
        isUDP = true;
      } else {
        throw new IllegalArgumentException("Entry has unknown transport layer set.");
      }
    }

    //parsing ArrowheadService
    if (serviceNameLength > 3) { //arrowhead service data should not be empty
      serviceName = serviceName.substring(0, serviceNameLength);
      String[] array = serviceName.split("_");
      if (serviceName.startsWith("_") && array.length == 3) {
        List<String> intf = new ArrayList<>();
        intf.add(array[2]);
        if (array[1].contains("ahf-")) {
          arrowheadService.setServiceDefinition(array[1].substring(4));
        } else {
          arrowheadService.setServiceDefinition(array[1]);
        }
        arrowheadService.setInterfaces(intf);
      } else {
        throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadService");
      }
    } else {
      throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadService");
    }

    for (String key : properties.keySet()) {
      if (key.contains("ahsrvmetad_")) {
        String metaKey = key.substring(key.indexOf("_") + 1, key.length());
        arrowheadService.getServiceMetadata().put(metaKey, properties.get(key));
      }
    }

    //building ArrowheadSystem
    ArrowheadSystem arrowheadSystem = new ArrowheadSystem();

    removeLastChar(providerName, '.');
    removeLastChar(address, '.');
    arrowheadSystem.setAddress(address);
    arrowheadSystem.setPort(port);

    if (properties.containsKey("ahsysauth")) {
      arrowheadSystem.setAuthenticationInfo(properties.get("ahsysauth"));
    } else if ((properties.containsKey("ahsysauthinfo"))) {
      arrowheadSystem.setAuthenticationInfo(properties.get("ahsysauthinfo"));
    }

    //if sysName is present in TXT record
    if (properties.containsKey("ahsysname")) {
      arrowheadSystem.setSystemName(properties.get("ahsysname"));

      //sanity checking if instance equals TXT fields
      if (!properties.get("ahsysname").equals(providerName)) {
        log.info("Malformed instance name in DNS record:" + providerName + "." + serviceName);
      }
    } else {
      //SysName is not present in TXT record
      arrowheadSystem.setSystemName(providerName);
    }

    //setting up response
    ServiceRegistryEntry providerService = new ServiceRegistryEntry();
    providerService.setProvidedService(arrowheadService);
    providerService.setProvider(arrowheadSystem);

    if (isUDP) {
      providerService.setUdp(true);
    }

    if (properties.containsKey("path")) {
      providerService.setServiceURI(properties.get("path"));
    }

    if (properties.containsKey("txtvers")) {
      providerService.setVersion(new Integer(properties.get("txtvers")));
    } else {
      providerService.setVersion(1);
    }

    if (properties.containsKey("eovdate")) {
      String encodedDate = properties.get("eovdate");
      String[] dateFields = encodedDate.split("\\.");
      if (dateFields.length != 6) {
        log.error("End of validity date string decoding error");
        throw new DnsException("End of validity date string decoding error");
      }
      LocalDateTime endOfValidity = LocalDateTime
          .of(Integer.valueOf(dateFields[0]), Integer.valueOf(dateFields[1]), Integer.valueOf(dateFields[2]), Integer.valueOf(dateFields[3]),
              Integer.valueOf(dateFields[4]), Integer.valueOf(dateFields[5]));
      providerService.setEndOfValidity(endOfValidity);
    }

    return providerService;
  }

  public static boolean pingHost(String host, int port, int timeout) {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), timeout);
      return true;
    } catch (Exception e) {
      return false; // Either timeout or unreachable or failed DNS lookup.
    }
  }

  public static void filterOnPing(List<ServiceRegistryEntry> fetchedList) {

    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();

    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      if (current.getProvider().getAddress().equals("0.0.0.0")) {
        iterator.remove();
      } else if (!pingHost(current.getProvider().getAddress(), current.getProvider().getPort(), ServiceRegistryMain.PING_TIMEOUT)) {
        iterator.remove();
      }
    }
  }

  public static void filterOnMeta(List<ServiceRegistryEntry> fetchedList, Map<String, String> metadata) {
    fetchedList.removeIf(current -> !metadata.equals(current.getProvidedService().getServiceMetadata()));
  }

  public static void filteronVersion(List<ServiceRegistryEntry> fetchedList, int targetVersion) {
    fetchedList.removeIf(current -> current.getVersion() != targetVersion);
  }

}
   