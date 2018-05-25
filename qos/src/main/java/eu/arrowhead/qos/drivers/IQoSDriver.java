/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.qos.drivers;

public interface IQoSDriver {

  /**
   * Configures a stream between a provider and a consumer.
   *
   * @param info Necessary information to the driver.
   *
   * @return Returns the stream configuration parameters.
   */
  ReservationResponse reserveQoS(ReservationInfo info);

}
