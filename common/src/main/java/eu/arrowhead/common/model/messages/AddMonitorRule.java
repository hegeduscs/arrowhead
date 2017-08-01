package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.Map;

/**
 * Message used to create a new monitor rule.
 *
 * @author Renato Ayres
 */
public class AddMonitorRule {

  private String protocol;
  private ArrowheadSystem provider;
  private ArrowheadSystem consumer;
  private Map<String, String> parameters;
  private boolean softRealTime;

  /**
   * Creates a new instance with no parameters initialized.
   */
  public AddMonitorRule() {
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
  public AddMonitorRule(String protocol, ArrowheadSystem provider, ArrowheadSystem consumer, Map<String, String> parameters, boolean softRealTime) {
    this.protocol = protocol;
    this.provider = provider;
    this.consumer = consumer;
    this.parameters = parameters;
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

  /**
   * Gets the soft real time clause.
   *
   * @return the soft real time clause
   */
  public boolean isSoftRealTime() {
    return softRealTime;
  }

  /**
   * Sets the soft real time clause.
   *
   * @param softRealTime the soft real time clause
   */
  public void setSoftRealTime(boolean softRealTime) {
    this.softRealTime = softRealTime;
  }
}
