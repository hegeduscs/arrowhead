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

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.DriverNotFoundException;
import eu.arrowhead.common.exception.ReservationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

//TODO make this a static final class, no need for factory pattern
public class DriversFactory {

  private static DriversFactory instance;
  private Class[] paramVerificationInfo = new Class[1];

  private DriversFactory() {
    //
    super();
    //VerificationInfo parameter
    paramVerificationInfo[0] = ReservationInfo.class;
  }

  /**
   * Returns a instance from this singleton class.
   */
  public static DriversFactory getInstance() {
    if (instance == null) {
      instance = new DriversFactory();
    }
    return instance;
  }

  //TODO same notes as in VerifierAlgorithmFactory

  /**
   * @param networkConfiguration Network configuration parameters on a map.
   * @param provider ArrowheadSystem.
   * @param consumer ArrowheadSystem.
   * @param service ArrowheadService.
   * @param commands Map of the selected commands from the user.
   * @param requestedQoS Map of the desired requestedQoS.
   *
   * @return Returns the generatedCommands from the QoSDriver.
   *
   * @throws ReservationException The StreamConfiguration found an error.
   * @throws DriverNotFoundException The selected type doesnt have an assigned driver.
   */
  public Map<String, String> generateCommands(String communicationProtocol, Map<String, String> networkConfiguration, ArrowheadSystem provider,
                                              ArrowheadSystem consumer, ArrowheadService service, Map<String, String> commands,
                                              Map<String, String> requestedQoS)
      throws ReservationException, DriverNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException,
      NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

    // Class Invoking
    Class cls = findClass(communicationProtocol);
    Object obj = cls.newInstance();
    // Method Invoking
    Method method = findMethod(cls);

    Map<String, String> streamConfiguration = (Map<String, String>) method.
        invoke(obj, new ReservationInfo(networkConfiguration, provider, consumer, service, commands, requestedQoS));

    if (streamConfiguration == null) {
      throw new ReservationException();
    }

    return streamConfiguration;

  }

  public Class findClass(String communicationProtocol) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    // Class Invoking
    Class cls;
    cls = Class.forName("eu.arrowhead.qos.communication.drivers." + communicationProtocol.toUpperCase());
    return cls;
  }

  public Method findMethod(Class cls) throws NoSuchMethodException {
    return cls.getDeclaredMethod("reserveQoS", paramVerificationInfo);
  }

}
