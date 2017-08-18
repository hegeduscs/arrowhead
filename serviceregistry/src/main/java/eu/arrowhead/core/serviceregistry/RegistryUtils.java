package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.ServiceData;
import com.github.danieln.dnssdjava.ServiceName;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ServiceMetadata;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistryUtils {
    private static Logger log = Logger.getLogger(RegistryUtils.class.getName());


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

        //Arrowhead core-related metadata
        properties.put("ahsysgrp", registryEntry.getProvider().getSystemGroup());
        properties.put("ahsysname", registryEntry.getProvider().getSystemName());
        properties.put("ahsysauthinfo", registryEntry.getProvider().getAuthenticationInfo());
        properties.put("path", registryEntry.getServiceURI());

        //additional user metadata
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

        if (serviceNameLength > 3) { //arrowhead service data should not be empty
            serviceName = serviceName.substring(0, serviceNameLength);
            String[] array = serviceName.split("_");
            if (array.length == 4) {
                List<String> intf = new ArrayList<>();
                intf.add(array[3]);
                arrowheadService.setServiceGroup(array[2]);
                arrowheadService.setServiceDefinition(array[1]);
                arrowheadService.setInterfaces(intf);
            } else
                throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadService");
        } else
            throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadService");

        for (String key: properties.keySet()) {
            if (key.contains("ahsrvmetad_")) {
                String metaKey = key.substring(key.indexOf("_")+1,key.length()-1);
                arrowheadService.getServiceMetadata().add(new ServiceMetadata(metaKey,properties.get(key)));
            }
        }

        //building ArrowheadSystem
        ArrowheadSystem arrowheadSystem = new ArrowheadSystem();

        if (removeLastChar(providerName,'.')) {
            String[] array = serviceName.split("_");
            if (array.length == 3) {
               arrowheadSystem.setSystemGroup(array[2]);
               arrowheadSystem.setSystemName(array[1]);
               arrowheadSystem.setAddress(address);
               arrowheadSystem.setPort(port);
               arrowheadSystem.setAuthenticationInfo(properties.get("ahsysauthinfo"));
            } else {
                if (array.length == 1 ) {

                }
                throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadSystem");
            }
        } else
            throw new IllegalArgumentException("Cannot parse DNS entry into ArrowheadSystem");

        //setting up response
        ServiceRegistryEntry providerService = new ServiceRegistryEntry();
        providerService.setProvidedService(arrowheadService);
        providerService.setProvider(arrowheadSystem);

        if (isUDP) {
            providerService.setUDP(true);
        }

        if (properties.containsKey("path"))
            providerService.setServiceURI(properties.get("path"));
        else
            throw new IllegalArgumentException("ServiceURI was empty in DNS record.");

        if (properties.containsKey("txtvers"))
            providerService.setVersion(new Integer(properties.get("txtvers")));
        else
            providerService.setVersion(1);

        return providerService;
    }

    public static boolean pingService(ServiceName instance, ServiceData service) {

        boolean replied = true;
        int port = service.getPort();
        RegistryUtils.removeLastChar(service.getHost(), '.');
        if (RegistryUtils.pingHost(service.getHost(), port, ServiceRegistryMain.pingTimeout)==false) {
            log.info("Can't access host at:"+service.getHost()+":"+port);
            replied = false;
        }
        return replied;
    }

    private static boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            //log.error(e);
            //e.printStackTrace();
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

   /*
        TODO This method filters on Service metadata.
    */
    public static void filterOnMeta(List<ServiceRegistryEntry> queryForm, List<ServiceMetadata> metadata) {
    }

    /*
        This method filters the ProvidedService list based on Service Provider availability
     */
    public static void filterOnPing(List<ServiceRegistryEntry> fetchedList) {
    }
}
