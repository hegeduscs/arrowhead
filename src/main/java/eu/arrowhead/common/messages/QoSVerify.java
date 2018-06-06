/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QoSVerify {

  private ArrowheadSystem consumer;
  private ArrowheadService requestedService;
  private List<ArrowheadSystem> providers = new ArrayList<>();
  private Map<String, String> requestedQoS = new HashMap<>();
  private Map<String, String> commands = new HashMap<>();

  public QoSVerify() {
  }

  public QoSVerify(ArrowheadSystem consumer, ArrowheadService requestedService, List<ArrowheadSystem> providers, Map<String, String> specifications,
                   Map<String, String> commands) {
    this.consumer = consumer;
    this.requestedService = requestedService;
    this.providers = providers;
    this.requestedQoS = specifications;
    this.commands = commands;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public List<ArrowheadSystem> getProviders() {
    return providers;
  }

  public void setProviders(List<ArrowheadSystem> providers) {
    this.providers = providers;
  }

  public Map<String, String> getRequestedQoS() {
    return requestedQoS;
  }

  public void setRequestedQoS(Map<String, String> requestedQoS) {
    this.requestedQoS = requestedQoS;
  }

  public Map<String, String> getCommands() {
    return commands;
  }

  public void setCommands(Map<String, String> commands) {
    this.commands = commands;
  }

}
