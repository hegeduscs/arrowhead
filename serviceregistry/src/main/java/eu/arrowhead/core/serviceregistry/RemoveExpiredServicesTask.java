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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.TimerTask;
import org.apache.log4j.Logger;

class RemoveExpiredServicesTask extends TimerTask {

  private static final Logger log = Logger.getLogger(RemoveExpiredServicesTask.class.getName());

  private ServiceRegistryEntry entry;

  RemoveExpiredServicesTask() {
  }

  private RemoveExpiredServicesTask(ServiceRegistryEntry entry) {
    this.entry = entry;
  }

  @Override
  public void run() {
    DnsSDDomainEnumerator de = DnsSDFactory.getInstance().createDomainEnumerator(ServiceRegistryMain.DNS_DOMAIN);
    DnsSDBrowser browser = DnsSDFactory.getInstance().createBrowser(de.getBrowsingDomains());

    Collection<ServiceType> types = browser.getServiceTypes();

    if (types != null) {
      //for every type,
      for (ServiceType type : types) {
        Collection<ServiceName> instances = browser.getServiceInstances(type);

        //per every instance we shall ping
        for (ServiceName instance : instances) {
          ServiceData serviceInstanceData = browser.getServiceData(instance);
          Map<String, String> properties = serviceInstanceData.getProperties();
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
            if (LocalDateTime.now().isAfter(entry.getEndOfValidity())) {
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
  }

}
