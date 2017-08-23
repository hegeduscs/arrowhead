package eu.arrowhead.core.serviceregistry;


import com.github.danieln.dnssdjava.*;
import org.apache.log4j.Logger;

import java.util.*;

public class PingProvidersTask extends TimerTask {

    private static Logger log = Logger.getLogger(ServiceRegistry.class.getName());

    @Override
    public void run() {
        log.debug("Cleaning up DNS at " + new Date().toString());
        pingAndRemoveServices();
    }

    public void pingAndRemoveServices() {
        DnsSDDomainEnumerator de;
        de = DnsSDFactory.getInstance().createDomainEnumerator(ServiceRegistryMain.computerDomain);
        DnsSDBrowser browser = DnsSDFactory.getInstance().createBrowser(de.getBrowsingDomains());

        Collection<ServiceType> types = browser.getServiceTypes();

        if (types != null) {
           //for every type,
           for (ServiceType type : types) {
             Collection<ServiceName> instances = browser.getServiceInstances(type);

             //per every instance we shall ping
             for (ServiceName instance : instances) {
               ServiceData serviceInstanceData = browser.getServiceData(instance);
               String hostName = serviceInstanceData.getHost();
               int port = serviceInstanceData.getPort();
               RegistryUtils.removeLastChar(hostName,'.');
               boolean toBeRemoved = false;
               if (hostName == "127.0.0.1" || hostName == "localhost")
                 toBeRemoved = true;
               else if (!RegistryUtils.pingHost(hostName,port,ServiceRegistryMain.pingTimeout))
                 toBeRemoved = true;

               if (toBeRemoved)
                 try {
                   DnsSDRegistrator registrator = RegistryUtils.createRegistrator();
                   registrator.unregisterService(instance);
                 } catch (DnsSDException e) {
                   log.error("DNS error occured in deleting an entry." + e.getMessage());
                 }
               }
             }
           }
       }
}