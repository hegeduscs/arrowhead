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
package eu.arrowhead.qos.algorithms;

import eu.arrowhead.qos.database.model.QoS_Resource_Reservation;
import eu.arrowhead.common.model.messages.QoSVerifierResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class VerifierAlgorithmFactory {

	private static VerifierAlgorithmFactory instance;
	Class[] paramVerificationInfo = new Class[1];

	protected VerifierAlgorithmFactory() {
		super();
		//VerificationInfo parameter
		paramVerificationInfo[0] = VerificationInfo.class;
	}

	public static VerifierAlgorithmFactory getInstance() {
		if (instance == null) {
			instance = new VerifierAlgorithmFactory();
		}
		return instance;
	}

	public QoSVerifierResponse verify(String communicationProtocol,
									  Map<String, String> provierDeviceCapabilities,
									  Map<String, String> consumerDeviceCapabilities,
									  List<QoS_Resource_Reservation> providerDeviceQoSReservations,
									  List<QoS_Resource_Reservation> consumerDeviceQoSReservations,
									  Map<String, String> requestedQoS,
									  Map<String, String> commands) throws InstantiationException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		Class cls;

		// Class Invoking
		cls = Class.forName("qosmanager.algorithms." + communicationProtocol.
			toUpperCase());
		Object obj = cls.newInstance();
		// Method Invoking
		Method method = cls.
			getDeclaredMethod("verifyQoS", paramVerificationInfo);
		return (QoSVerifierResponse) method.
			invoke(obj, new VerificationInfo(provierDeviceCapabilities, consumerDeviceCapabilities, providerDeviceQoSReservations, consumerDeviceQoSReservations, requestedQoS, commands));

	}

}
