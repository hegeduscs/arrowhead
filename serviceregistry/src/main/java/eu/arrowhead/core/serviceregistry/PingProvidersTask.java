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
           for (ServiceType type : types) {
             Collection<ServiceName> instances = browser.getServiceInstances(type);
             for (ServiceName instance : instances) {
               //TODO ping services and remove if necessary
             }
           }
       }
    }
}