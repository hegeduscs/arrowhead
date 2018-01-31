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

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.HashMap;
import java.util.Map;

/**
 * Message used to create a new monitor rule.
 *
 * @author Renato Ayres
 */
public class MonitorRule {

  private String protocol;
  private ArrowheadSystem provider;
  private ArrowheadSystem consumer;
  private Map<String, String> parameters = new HashMap<>();
  private boolean softRealTime;

  /**
   * Creates a new instance with no parameters initialized.
   */
  public MonitorRule() {
  }

  /**
   * Creates a new instance with the given monitor protocol, service provider, service consumer, monitor parameters and a soft real time clause.
   *
   * @param protocol the monitor protocol
   * @param provider the service provider
   * @param consumer the service consumer
   * @param parameters the monitor parameters. It works by getting the value of the parameter (key) e.g. key=bandwidth, value=100
   * @param softRealTime the soft real time clause
   */
  public MonitorRule(String protocol, ArrowheadSystem provider, ArrowheadSystem consumer, Map<String, String> parameters, boolean softRealTime) {
    this.protocol = protocol;
    this.provider = provider;
    this.consumer = consumer;
    this.parameters = parameters;
    this.softRealTime = softRealTime;
  }

  /**
   * Gets the monitor protocol
   *
   * @return the monitor protocol
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * Sets the monitor protocol
   *
   * @param protocol the monitor protocol
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Gets the service provider
   *
   * @return the service provider
   */
  public ArrowheadSystem getProvider() {
    return provider;
  }

  /**
   * Sets the service provider
   *
   * @param provider the service provider
   */
  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  /**
   * Gets the service consumer
   *
   * @return the service consumer
   */
  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  /**
   * Sets the service consumer
   *
   * @param consumer the service consumer
   */
  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  /**
   * Gets the monitor parameters
   *
   * @return the monitor parameters
   */
  public Map<String, String> getParameters() {
    return parameters;
  }

  /**
   * Sets the monitor of parameters
   *
   * @param parameters the monitor parameters
   */
  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public boolean isSoftRealTime() {
    return softRealTime;
  }

  public void setSoftRealTime(boolean softRealTime) {
    this.softRealTime = softRealTime;
  }

}
