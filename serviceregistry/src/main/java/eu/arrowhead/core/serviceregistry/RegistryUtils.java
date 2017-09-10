package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDException;
import com.github.danieln.dnssdjava.DnsSDFactory;
import com.github.danieln.dnssdjava.DnsSDRegistrator;
import com.github.danieln.dnssdjava.ServiceData;
import com.github.danieln.dnssdjava.ServiceName;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ServiceMetadata;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;
import java.util.Iterator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistryUtils {
    private static Logger log = Logger.getLogger(RegistryUtils.class.getName());

  static DnsSDRegistrator createRegistrator() throws DnsSDException {
    // Get the DNS specific settings from global static variables (from prop files)
    // and then set up Registrator
    InetSocketAddress dnsServerAddress = new InetSocketAddress(ServiceRegistryMain.dnsIpAddress, ServiceRegistryMain.dnsPort);
    DnsSDRegistrator registrator = DnsSDFactory
        .getInstance().createRegistrator(ServiceRegistryMain.dnsDomain, dnsServerAddress);
    if (!ServiceRegistryMain.tsigKeyName.isEmpty())
        registrator.setTSIGKey(ServiceRegistryMain.tsigKeyName,ServiceRegistryMain.tsigAlgorithm,ServiceRegistryMain.tsigKeyValue);
    return registrator;
  }

    public static boolean removeLastChar(String host, char charachter) {
        if (host != null && host.length() > 0 && host.charAt(host.length() - 1) == charachter) {
            host = host.substring(0, host.length() - 1);
            return true;
        }
        return false;
    }

    public static void setServiceDataProperties(ServiceRegistryEntry registryEntry, ServiceData data) {
        //setting up everything for the TXT record
        Map<String, String> properties = data.getProperties();

        if (registryEntry.getProvider() == null)
          return;

        //Arrowhead core-related metadata
        if (registryEntry.getProvider().getSystemGroup()!=null)
          properties.put("ahsysgrp", registryEntry.getProvider().getSystemGroup());
        if (registryEntry.getProvider().getSystemName() != null)
          properties.put("ahsysname", registryEntry.getProvider().getSystemName());
        if (registryEntry.getProvider().getAuthenticationInfo() != null)
          properties.put("ahsysauth", registryEntry.getProvider().getAuthenticationInfo());
        if (registryEntry.getServiceURI() != null)
          properties.put("path", registryEntry.getServiceURI());

        //additional user metadata
        if (registryEntry.getServiceMetadata() != null)
          for (ServiceMetadata entry : registryEntry.getServiceMetadata()) {
              properties.put("ahsrvmetad_" + entry.getKey(), entry.getValue());
          }

        //As per the DNS-SD standard on service versioning
        properties.put("txtvers",Integer.toString(registryEntry.getVersion()));
    }

    public static ServiceRegistryEntry buildRegistryEntry(ServiceData service) throws IllegalArgumentException {

        //extracting fields from DNS record
        String providerName = service.getName().getName();
        String serviceName = service.getName().getType().toString();
        String address = service.getHost();
        int port = service.getPort();
        Map<String, String> properties = service.getProperties();

        if (providerName == null || serviceName == null || address == null)
            throw new IllegalArgumentException("DNS entry is empty somehow!");

        //building ArrowheadService object from fields
        ArrowheadService arrowheadService =new ArrowheadService();

        //need to differentiate between TCP and UDP
        int serviceNameLength;
        boolean isUDP = false;
        if (serviceName.contains("_tcp"))
            serviceNameLength = serviceName.indexOf("._tcp");
        else {
            if (serviceName.contains("_udp")) {
                serviceNameLength = serviceName.indexOf("._udp");
                isUDP = true;
            } else
                throw new IllegalArgumentException("Entry has unknown transport layer set.");
        }

        //parsing ArrowheadService
        if (serviceNameLength > 3) { //arrowhead service data should not be empty
            serviceName = serviceName.substring(0, serviceNameLength);
            String[] array = serviceName.split("_");
            if (serviceName.startsWith("_") && array.length == 4) {
                List<String> intf = new ArrayList<>();
                intf.add(array[3]);
                arrowheadService.setServiceGroup(array[2]);
                if (array[1].contains("ahf-"))
                  arrowheadService.setServiceDefinition(array[1].substring(4));
                else
                  arrowheadService.setServiceDefinition(array[1]);
              arrowheadService.setInterfaces(intf);
            } else
                throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadService");
        } else
            throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadService");

        for (String key: properties.keySet()) {
            if (key.contains("ahsrvmetad_")) {
                String metaKey = key.substring(key.indexOf("_")+1,key.length());
                arrowheadService.getServiceMetadata().add(new ServiceMetadata(metaKey,properties.get(key)));
            }
        }

        //building ArrowheadSystem
        ArrowheadSystem arrowheadSystem = new ArrowheadSystem();

        removeLastChar(providerName,'.');
        removeLastChar(address,'.');
        arrowheadSystem.setAddress(address);
        arrowheadSystem.setPort(port);

        if (properties.containsKey("ahsysauth"))
            arrowheadSystem.setAuthenticationInfo(properties.get("ahsysauth"));
        else if ((properties.containsKey("ahsysauthinfo")))
            arrowheadSystem.setAuthenticationInfo(properties.get("ahsysauthinfo"));

          //if sysName and sysGrp is present in TXT record
        if (properties.containsKey("ahsysname") || properties.containsKey("ahsysgrp")) {

            arrowheadSystem.setSystemName(properties.get("ahsysname"));
            arrowheadSystem.setSystemGroup(properties.get("ahsysgrp"));

            //sanity checking if instance equals TXT fields
            if (providerName.contains("_")) {
              String[] array = providerName.split("_");
              if (array.length == 2) {
                if (!properties.get("ahsysname").equals(array[0]) ||
                    !properties.get("ahsysgrp").equals(array[1]))
                  log.info(
                      "Malformed instance name in DNS record:" + providerName + "." + serviceName);
              } else
                log.info(
                    "Malformed instance name in DNS record:" + providerName + "." + serviceName);
            }

        } else {
            //SysGrp and SysName is not present in TXT record
            if (providerName.contains("_")) {
              String[] array = providerName.split("_");
              if (array.length == 2) {
                arrowheadSystem.setSystemName(array[0]);
                arrowheadSystem.setSystemGroup(array[1]);
              } else
                throw new IllegalArgumentException("Entry cannot be parsed into ArrowheadSystem.");
            } else
              throw new IllegalArgumentException("Entry cannot be parsed into ArrowheadSystem.");
        }

        //setting up response
        ServiceRegistryEntry providerService = new ServiceRegistryEntry();
        providerService.setProvidedService(arrowheadService);
        providerService.setProvider(arrowheadSystem);

        if (isUDP)
            providerService.setUDP(true);

        if (properties.containsKey("path"))
            providerService.setServiceURI(properties.get("path"));

        if (properties.containsKey("txtvers"))
            providerService.setVersion(new Integer(properties.get("txtvers")));
        else
            providerService.setVersion(1);

        return providerService;
    }

    public static boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

  public static void filterOnPing(List<ServiceRegistryEntry> fetchedList) {

    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();

    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      if (current.getProvider().getAddress().equals("localhost") ||
          current.getProvider().getAddress().equals("127.0.0.1"))
        iterator.remove();

      else if (!pingHost(current.getProvider().getAddress(),
                        current.getProvider().getPort(),
                        ServiceRegistryMain.pingTimeout))
        iterator.remove();
    }
  }

  /*
      TODO This method filters on Service metadata.
  */
  public static void filterOnMeta(List<ServiceRegistryEntry> fetchedList, List<ServiceMetadata> metadata) {
    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();
    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      boolean allMatch = true;
      for (ServiceMetadata currentMeta : current.getProvidedService().getServiceMetadata()) {
        if (!metadata.contains(currentMeta)) allMatch = false;
      }
      if (!allMatch) iterator.remove();
    }
  }

  /*
     TODO This method filters on Service version.
 */
  public static void filteronVersion(List<ServiceRegistryEntry> fetchedList, int targetVersion) {
    Iterator<ServiceRegistryEntry> iterator = fetchedList.iterator();
    while (iterator.hasNext()) {
      ServiceRegistryEntry current = iterator.next();
      if (current.getVersion() != targetVersion) {
        iterator.remove();
      }
    }
  }
}
