/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.HashMap;
import java.util.Map;

public class QoSReservationCommand {

  private ArrowheadService service;
  private ArrowheadSystem producer;
  private ArrowheadSystem consumer;
  private Map<String, String> commands = new HashMap<>();
  private Map<String, String> requestedQoS = new HashMap<>();

  public QoSReservationCommand() {
  }

  public QoSReservationCommand(ArrowheadService service, ArrowheadSystem producer, ArrowheadSystem consumer, Map<String, String> commands, Map
      <String, String> requestedQoS) {
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
