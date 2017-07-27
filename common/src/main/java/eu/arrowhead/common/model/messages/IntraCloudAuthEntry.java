package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author umlaufz
 */
@XmlRootElement
public class IntraCloudAuthEntry {

  private ArrowheadSystem consumer;
  private ArrayList<ArrowheadSystem> providerList = new ArrayList<ArrowheadSystem>();
  private ArrayList<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();

  public IntraCloudAuthEntry() {
  }

  public IntraCloudAuthEntry(ArrowheadSystem consumer, ArrayList<ArrowheadSystem> providerList,
      ArrayList<ArrowheadService> serviceList) {
    this.consumer = consumer;
    this.providerList = providerList;
    this.serviceList = serviceList;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public ArrayList<ArrowheadSystem> getProviderList() {
    return providerList;
  }

  public void setProviderList(ArrayList<ArrowheadSystem> providerList) {
    this.providerList = providerList;
  }

  public ArrayList<ArrowheadService> getServiceList() {
    return serviceList;
  }

  public void setServiceList(ArrayList<ArrowheadService> serviceList) {
    this.serviceList = serviceList;
  }

  public boolean isPayloadUsable() {
    if (consumer == null || serviceList.isEmpty() || providerList.isEmpty() || !consumer
        .isValidForDatabase()) {
      return false;
    }
    for (ArrowheadSystem provider : providerList) {
      if (!provider.isValidForDatabase()) {
        return false;
      }
    }
    for (ArrowheadService service : serviceList) {
      if (!service.isValidSoft()) {
        return false;
      }
    }
    return true;
  }

}
