/*
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

import eu.arrowhead.common.database.qos.ResourceReservation;
import eu.arrowhead.common.messages.QoSVerifierResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

ackage eu.arrowhead.qos.algorithms;

public class VerifierAlgorithmFactory {

  private static VerifierAlgorithmFactory instance;

  private Class[] paramVerificationInfo = new Class[1];

  private VerifierAlgorithmFactory() {
    //VerificationInfo parameter
    paramVerificationInfo[0] = VerificationInfo.class;
  }

  public static VerifierAlgorithmFactory getInstance() {
    if (instance == null) {
      instance = new VerifierAlgorithmFactory();
    }
    return instance;
  }

  // Cause I think using interfaces and enums would be more clean and faster at runtime
  // Or if we keep this reflection pattern, than catch and handle the exceptions here in this method!
  //note: communicationProtocol == network.getNetworkType :/

  public QoSVerifierResponse verify(String communicationProtocol, Map<String, String> providerDeviceCapabilities, Map<String, String> consumerDeviceCapabilities, List<ResourceReservation> providerDeviceQoSReservations,
                                    List<ResourceReservation> consumerDeviceQoSReservations, Map<String, String> requestedQoS, Map<String, String> commands)
      throws InstantiationException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
    Class cls;

    // Class Invoking
    cls = Class.forName("eu.arrowhead.qos.algorithms.implementations." + communicationProtocol.toUpperCase());
    Object obj = cls.newInstance();
    // Method Invoking
    Method method = cls.getDeclaredMethod("verifyQoS", paramVerificationInfo);
    return (QoSVerifierResponse) method.
                                           invoke(obj, new VerificationInfo(providerDeviceCapabilities, consumerDeviceCapabilities, providerDeviceQoSReservations,
                                                                            consumerDeviceQoSReservations, requestedQoS, commands));

  }

}
