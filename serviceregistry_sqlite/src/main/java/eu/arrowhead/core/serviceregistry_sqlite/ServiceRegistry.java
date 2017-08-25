package eu.arrowhead.core.serviceregistry_sqlite;

import eu.arrowhead.common.exception.DnsException;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

class ServiceRegistry {

  private static Logger log = Logger.getLogger(ServiceRegistry.class.getName());

  static boolean register(ServiceRegistryEntry entry) {

      log.info("Entered SR register method.");

      boolean allRegistered = true;
      //One System may offer out the same service on multiple interface implementations/IDD-s
      for (String interf : entry.getProvidedService().getInterfaces()) {


      }
      return allRegistered;
  }

  static boolean unRegister(ServiceRegistryEntry entry) {

      log.info ("Entered SR unregister method.");

      //creating service name and type based on Arrowhead data
      String serviceGroup = entry.getProvidedService().getServiceGroup();
      String serviceDefinition = entry.getProvidedService().getServiceDefinition();

      //ArrowheadSystem is the instance name with underscores
      String providerInstance = entry.getProvider().getSystemName() + "_" + entry.getProvider().getSystemGroup();

      boolean allRemoved = true;
      for (String interf : entry.getProvidedService().getInterfaces()) {
          String serviceType = "_ahf-" + serviceDefinition + "_" + serviceGroup + "_" + interf;

          if (entry.isUDP())
            serviceType +=("._udp");
          else
            serviceType +=("._tcp");


      }
    return allRemoved;
  }

   static ServiceQueryResult provideServices(ServiceQueryForm queryForm) {


        //compiling result
        ServiceQueryResult sqr = new ServiceQueryResult();

        return sqr;
  }

   static ServiceQueryResult provideAllServices(){

      log.info("All Services are provided!");

    return null;
  }

   static boolean removeAllServices () {
     log.info("Deleted all services from DNS-SD!");
      return true;
  }
}
