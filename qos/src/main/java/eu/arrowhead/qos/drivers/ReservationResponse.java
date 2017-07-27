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
package eu.arrowhead.qos.drivers;

import java.util.Map;

/**
 * @author Paulo
 */
public class ReservationResponse {

  public Boolean sucess;
  public String reason;
  public Map<String, String> networkConfiguration;

  public ReservationResponse() {
  }

  public ReservationResponse(Boolean sucess, String reason,
      Map<String, String> networkConfiguration) {
    this.sucess = sucess;
    this.reason = reason;
    this.networkConfiguration = networkConfiguration;
  }

  public Boolean getSucess() {
    return sucess;
  }

  public void setSucess(Boolean sucess) {
    this.sucess = sucess;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Map<String, String> getNetworkConfiguration() {
    return networkConfiguration;
  }

  public void setNetworkConfiguration(
      Map<String, String> networkConfiguration) {
    this.networkConfiguration = networkConfiguration;
  }

}
