/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

public class ConnectToProviderResponse {

  private String queueName;
  private String controlQueueName;

  public ConnectToProviderResponse() {
  }

  public ConnectToProviderResponse(String queueName, String controlQueueName) {
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public String getControlQueueName() {
    return controlQueueName;
  }

  public void setControlQueueName(String controlQueueName) {
    this.controlQueueName = controlQueueName;
  }

}
