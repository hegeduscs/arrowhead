package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QoSReservationCommand {

  public ArrowheadService service;
  public ArrowheadSystem producer;
  public ArrowheadSystem consumer;
  public Map<String, String> commands;
  public Map<String, String> requestedQoS;

  protected QoSReservationCommand() {

  }

  public QoSReservationCommand(ArrowheadService service, ArrowheadSystem producer,
      ArrowheadSystem consumer,
      Map<String, String> commands, Map<String, String> requestedQoS) {
    super();
    this.service = service;
    this.producer = producer;
    this.consumer = consumer;
    this.commands = commands;
    this.requestedQoS = requestedQoS;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public ArrowheadSystem getProducer() {
    return producer;
  }

  public void setProducer(ArrowheadSystem producer) {
    this.producer = producer;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public Map<String, String> getCommands() {
    return commands;
  }

  public void setCommands(Map<String, String> commands) {
    this.commands = commands;
  }

  public Map<String, String> getRequestedQoS() {
    return requestedQoS;
  }

  public void setRequestedQoS(Map<String, String> requestedQoS) {
    this.requestedQoS = requestedQoS;
  }

}
