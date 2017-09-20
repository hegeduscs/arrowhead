/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/. 
*
* This work was supported by National Funds through FCT (Portuguese
* Foundation for Science and Technology) and by the EU ECSEL JU
* funding, within Arrowhead project, ref. ARTEMIS/0001/2012,
* JU grant nr. 332987.
* ISEP, Polytechnic Institute of Porto.
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
