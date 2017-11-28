package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDBrowser;
import com.github.danieln.dnssdjava.DnsSDDomainEnumerator;
import com.github.danieln.dnssdjava.DnsSDException;
import com.github.danieln.dnssdjava.DnsSDFactory;
import com.github.danieln.dnssdjava.DnsSDRegistrator;
import com.github.danieln.dnssdjava.ServiceData;
import com.github.danieln.dnssdjava.ServiceName;
import com.github.danieln.dnssdjava.ServiceType;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.DnsException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class ServiceRegistry {

  private static final Logger log = Logger.getLogger(ServiceRegistry.class.getName());

  static boolean register(@NotNull ServiceRegistryEntry entry) throws DnsSDException {

    log.info("Entered SR register method.");
    //creating service name and type based on Arrowhead data
    String serviceGroup = entry.getProvidedService().getServiceGroup();
    String serviceDefinition = entry.getProvidedService().getServiceDefinition();

    //ArrowheadSystem is the instance name with underscores
    String providerInstance = entry.getProvider().getSystemName() + "_" + entry.getProvider().getSystemGroup();
    String address = entry.getProvider().getAddress() + ".";
    int port = entry.getProvider().getPort();

    boolean allRegistered = true;
    //One System may offer out the same service on multiple interface implementations/IDD-s
    for (String interf : entry.getProvidedService().getInterfaces()) {

      //ArrowheadService is encoded in the service type field, interface (IDD) as the protocol
      String serviceType = "_ahf-" + serviceDefinition + "_" + serviceGroup + "_" + interf;
      if (entry.isUDP()) {
        serviceType += ("._udp");
      } else {
        serviceType += ("._tcp");
      }

      try {
        DnsSDRegistrator registrator = RegistryUtils.createRegistrator();
        ServiceName name = registrator.makeServiceName(providerInstance, ServiceType.valueOf(serviceType));

        //create  service data object
        ServiceData data = new ServiceData(name, address, port);
        RegistryUtils.setServiceDataProperties(entry, data);

        if (registrator.registerService(data)) {
          log.info("Service registered in DNS-SD: " + providerInstance + "." + serviceType);
        } else {
          //need to delete the record first
          registrator.unregisterService(name);
          //second try
          registrator.registerService(data);
          log.info("Service record updated in DNS-SD: " + providerInstance + "." + serviceType);
          //allRegistered = false;
        }
      } catch (DnsSDException ex) {
        log.error(ex);
        ex.printStackTrace();
        throw new DnsException(ex.getMessage());
      }
    }
    return allRegistered;
  }

  static boolean unRegister(@NotNull ServiceRegistryEntry entry) {

    log.info("Entered SR unregister method.");

    //creating service name and type based on Arrowhead data
    String serviceGroup = entry.getProvidedService().getServiceGroup();
    String serviceDefinition = entry.getProvidedService().getServiceDefinition();

    //ArrowheadSystem is the instance name with underscores
    String providerInstance = entry.getProvider().getSystemName() + "_" + entry.getProvider().getSystemGroup();

    boolean allRemoved = true;
    for (String interf : entry.getProvidedService().getInterfaces()) {
      String serviceType = "_ahf-" + serviceDefinition + "_" + serviceGroup + "_" + interf;

      if (entry.isUDP()) {
        serviceType += ("._udp");
      } else {
        serviceType += ("._tcp");
      }

      try {
        DnsSDRegistrator registrator = RegistryUtils.createRegistrator();
        ServiceName name = registrator.makeServiceName(providerInstance, ServiceType.valueOf(serviceType));

        if (registrator.unregisterService(name)) {
          log.info("Service unregistered: " + entry.getProvidedService().toString() + "," + interf + entry.getProvider().toString());
        } else {
          log.info("No service to remove: " + entry.getProvidedService().toString() + "," + interf + entry.getProvider().toString());
          //allRemoved = false;
        }
      } catch (DnsSDException ex) {
        log.error(ex);
        ex.printStackTrace();
        throw new DnsException(ex.getMessage());
      }
    }
    return allRemoved;
  }

  @NotNull
  static ServiceQueryResult provideServices(@NotNull ServiceQueryForm queryForm) {

    //creating DNS browser
    DnsSDDomainEnumerator de = DnsSDFactory.getInstance().createDomainEnumerator(ServiceRegistryMain.computerDomain);
    DnsSDBrowser browser = DnsSDFactory.getInstance().createBrowser(de.getBrowsingDomains());

    //this list will contain all instances corresponding to the given interfaces
    List<ServiceRegistryEntry> fetchedList = new ArrayList<>();
    fetchedList.clear();

    //building look-up service types for query
    for (String interf : queryForm.getService().getInterfaces()) {

      String serviceType = "_ahf-" + queryForm.getService().getServiceDefinition() + "_" + queryForm.getService().getServiceGroup() + "_" + interf;

      //getting the instances for each interface on each transport layer
      Collection<ServiceName> instances = new ArrayList<>();
      instances.addAll(browser.getServiceInstances(ServiceType.valueOf(serviceType + "._tcp")));
      instances.addAll(browser.getServiceInstances(ServiceType.valueOf(serviceType + "._udp")));

      for (ServiceName instance : instances) {
        ServiceData serviceInstance = browser.getServiceData(instance);
        try {
          ServiceRegistryEntry provService = RegistryUtils.buildRegistryEntry(serviceInstance);
          fetchedList.add(provService);
        } catch (IllegalArgumentException e) {
          log.info("There is a non-Arrowhead compliant DNS record in the Registry: " + instance.getName() + "." + instance.getType().toString());
        }
      }
    }

    //filtering on service version
    if (queryForm.getVersion() != 1) {
      RegistryUtils.filteronVersion(fetchedList, queryForm.getVersion());
    }

    //filtering on metadata
    if (queryForm.isMetadataSearch()) {
      RegistryUtils.filterOnMeta(fetchedList, queryForm.getService().getServiceMetadata());
    }

    //filtering if pingProviders
    if (queryForm.isPingProviders()) {
      RegistryUtils.filterOnPing(fetchedList);
    }

    //compiling result
    ServiceQueryResult sqr = new ServiceQueryResult();
    sqr.setServiceQueryData(fetchedList);
    return sqr;
  }

  @NotNull
  static ServiceQueryResult provideAllServices() throws DnsSDException {
    //Preparing DNS-SD
    DnsSDDomainEnumerator de = DnsSDFactory.getInstance().createDomainEnumerator(ServiceRegistryMain.computerDomain);
    DnsSDBrowser browser = DnsSDFactory.getInstance().createBrowser(de.getBrowsingDomains());
    Collection<ServiceType> types = browser.getServiceTypes();

    List<ServiceRegistryEntry> list = new ArrayList<>();
    list.clear();

    if (types != null) {

      for (ServiceType type : types) {

        Collection<ServiceName> instances = browser.getServiceInstances(type);

        for (ServiceName instance : instances) {

          ServiceData serviceInstanceData = browser.getServiceData(instance);
          try {
            list.add(RegistryUtils.buildRegistryEntry(serviceInstanceData));
          } catch (IllegalArgumentException e) {
            log.info("There is a non-Arrowhead compliant DNS record: " + instance.getName() + "." + instance.getType().toString());
          }
        }
      }

      ServiceQueryResult result = new ServiceQueryResult();
      result.setServiceQueryData(list);
      log.info("All Services are provided!");
      return result;
    }
    return new ServiceQueryResult();
  }

  static boolean removeAllServices() {
    DnsSDDomainEnumerator de = DnsSDFactory.getInstance().createDomainEnumerator(ServiceRegistryMain.computerDomain);
    DnsSDBrowser browser = DnsSDFactory.getInstance().createBrowser(de.getBrowsingDomains());
    Collection<ServiceType> types = browser.getServiceTypes();

    try {
      DnsSDRegistrator registrator = RegistryUtils.createRegistrator();

      for (ServiceType type : types) {
        Collection<ServiceName> instances = browser.getServiceInstances(type);
        for (ServiceName instance : instances) {
          registrator.unregisterService(instance);
        }
      }
    } catch (DnsSDException e) {
      log.error("There was a DNS-SD error in removing all services." + e.getMessage());
      e.printStackTrace();
      return false;
    }

    log.info("Deleted all services from DNS-SD!");
    return true;
  }
}
