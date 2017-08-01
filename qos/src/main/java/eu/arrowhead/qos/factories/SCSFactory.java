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
package eu.arrowhead.qos.factories;

import eu.arrowhead.common.database.qos.ISCSRepository;
import eu.arrowhead.common.database.qos.Network;
import eu.arrowhead.common.database.qos.NetworkDevice;
import eu.arrowhead.common.database.qos.SCSRepositoryImpl;
import eu.arrowhead.common.model.ArrowheadSystem;

public class SCSFactory {

  private static SCSFactory instance;
  private ISCSRepository repo;

  private SCSFactory() {
    super();
    repo = new SCSRepositoryImpl();
  }

  /**
   * Returns a instance from this singleton class.
   */
  public static SCSFactory getInstance() {
    if (instance == null) {
      instance = new SCSFactory();
    }
    return instance;
  }

  /**
   * Get NetworkDevice from System.
   *
   * @param system System which is deployed on the network device.
   * @return Returns the network device where the system is deployed.
   */
  public NetworkDevice getNetworkDeviceFromSystem(ArrowheadSystem system) {
    return repo.getNetworkDeviceFromSystem(converFromDTO(system));
  }

  /**
   * Update a already saved network.
   *
   * @param network Network to be updated.
   * @return Returns the updated network.
   */
  public Network updateNetwork(Network network) {
    return repo.updateNetwork(network);
  }

}
