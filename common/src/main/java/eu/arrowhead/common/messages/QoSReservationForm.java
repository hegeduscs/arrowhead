package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.HashMap;
import java.util.Map;

public class QoSReservationForm {

  private ArrowheadService service;
  private ArrowheadSystem provider;
  private ArrowheadSystem consumer;
  private Map<String, String> requestedQoS = new HashMap<>();

  public QoSReservationForm() {
  }

  public QoSReservationForm(ArrowheadService service, ArrowheadSystem provider, ArrowheadSystem consumer, Map<String, String> requestedQoS) {
    this.service = service;
    this.provider = provider;
    this.consumer = consumer;
    this.requestedQoS = requestedQoS;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public Map<String, String> getRequestedQoS() {
    return requestedQoS;
  }

  public void setRequestedQoS(Map<String, String> requestedQoS) {
    this.requestedQoS = requestedQoS;
  }

}
