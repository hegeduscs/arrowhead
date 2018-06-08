/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.qos.algorithms.implementations;

import eu.arrowhead.qos.algorithms.IVerifierAlgorithm;
import eu.arrowhead.qos.algorithms.VerificationInfo;
import eu.arrowhead.qos.algorithms.VerificationResponse;

public class FTTSE implements IVerifierAlgorithm {

  private final String BANDWIDTH = "bandwidth";
  private final String DELAY = "delay";

  public FTTSE() {
  }


  @Override
  public VerificationResponse verifyQoS(VerificationInfo info) {
    return new VerificationResponse(true, null);
  }

}
