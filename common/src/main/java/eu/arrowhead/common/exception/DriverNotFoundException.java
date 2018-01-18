package eu.arrowhead.common.exception;

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

/**
 * Related to the drivers of each communication protocol. Whenever the DriverFactory doesn't recognize the type, this exception will be launched.
 *
 * @author CISTER/INESC-TEC
 */
public class DriverNotFoundException extends RuntimeException {

  public DriverNotFoundException(String message) {
    super(message);
  }

}
