/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

ackage eu.arrowhead.qos.algorithms;

public interface IVerifierAlgorithm {

  /**
   * Verify if the desired qos is possible
   *
   * @param info Contains provider, consumer systems, the requested service, requested QoS and the all the capabilities.
   *
   * @return Returns true or false with a rejetction motivation.
   */
  VerificationResponse verifyQoS(VerificationInfo info);

}
