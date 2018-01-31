/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gateway.model;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.Date;

public class ActiveSession {

  private ArrowheadSystem consumer;
  private ArrowheadCloud consumerCloud;
  private ArrowheadSystem provider;
  private ArrowheadCloud providerCloud;
  private ArrowheadService service;
  private String brokerName;
  private Integer brokerPort;
  private Integer serverSocketPort;
  private String queueName;
  private String controlQueueName;
  private Boolean isSecure;
  private Date startSession;

  public ActiveSession() {
  }

  public ActiveSession(ArrowheadSystem consumer, ArrowheadCloud consumerCloud, ArrowheadSystem provider, ArrowheadCloud providerCloud,
                       ArrowheadService service, String brokerName, Integer brokerPort, Integer serverSocketPort, String queueName, String
                           controlQueueName, Boolean isSecure, Date startSession) {
    this.consumer = consumer;
    this.consumerCloud = consumerCloud;
    this.provider = provider;
    this.providerCloud = providerCloud;
    this.service = service;
    this.brokerName = brokerName;
    this.brokerPort = brokerPort;
    this.serverSocketPort = serverSocketPort;
    this.queueName = queueName;
    this.controlQueueName = controlQueueName;
    this.isSecure = isSecure;
    this.startSession = startSession;
  }

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public ArrowheadCloud getConsumerCloud() {
    return consumerCloud;
  }

  public void setConsumerCloud(ArrowheadCloud consumerCloud) {
    this.consumerCloud = consumerCloud;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public ArrowheadCloud getProviderCloud() {
    return providerCloud;
  }

  public void setProviderCloud(ArrowheadCloud providerCloud) {
    this.providerCloud = providerCloud;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public String getBrokerName() {
    return brokerName;
  }

  public void setBrokerName(String brokerName) {
    this.brokerName = brokerName;
  }

  public Integer getBrokerPort() {
    return brokerPort;
  }

  public void setBrokerPort(Integer brokerPort) {
    this.brokerPort = brokerPort;
  }

  public Integer getServerSocketPort() {
    return serverSocketPort;
  }

  public void setServerSocketPort(Integer serverSocketPort) {
    this.serverSocketPort = serverSocketPort;
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

  public Boolean getIsSecure() {
    return isSecure;
  }

  public void setIsSecure(Boolean isSecure) {
    this.isSecure = isSecure;
  }

  public Date getStartSession() {
    return startSession;
  }

  public void setStartSession(Date startSession) {
    this.startSession = startSession;
  }

}
