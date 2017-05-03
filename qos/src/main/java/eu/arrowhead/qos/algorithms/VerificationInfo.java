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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paulo
 */
public class VerificationInfo {

	Map<String, String> provierDeviceCapabilities;
	Map<String, String> consumerDeviceCapabilities;
	List<QoS_Resource_Reservation> providerDeviceQoSReservations;
	List<QoS_Resource_Reservation> consumerDeviceQoSReservations;
	Map<String, String> requestedQoS;
	Map<String, String> commands;

	public VerificationInfo() {
	}

	public VerificationInfo(Map<String, String> provierDeviceCapabilities,
							Map<String, String> consumerDeviceCapabilities,
							List<QoS_Resource_Reservation> providerDeviceQoSReservations,
							List<QoS_Resource_Reservation> consumerDeviceQoSReservations,
							Map<String, String> requestedQoS,
							Map<String, String> commands) {
		this.provierDeviceCapabilities = provierDeviceCapabilities;
		this.consumerDeviceCapabilities = consumerDeviceCapabilities;
		this.providerDeviceQoSReservations = providerDeviceQoSReservations;
		this.consumerDeviceQoSReservations = consumerDeviceQoSReservations;
		this.requestedQoS = requestedQoS;
		this.commands = commands;
	}

	public Map<String, String> getProvierDeviceCapabilities() {
		return provierDeviceCapabilities;
	}

	public void setProvierDeviceCapabilities(
		Map<String, String> provierDeviceCapabilities) {
		this.provierDeviceCapabilities = provierDeviceCapabilities;
	}

	public Map<String, String> getConsumerDeviceCapabilities() {
		return consumerDeviceCapabilities;
	}

	public void setConsumerDeviceCapabilities(
		Map<String, String> consumerDeviceCapabilities) {
		this.consumerDeviceCapabilities = consumerDeviceCapabilities;
	}

	public List<QoS_Resource_Reservation> getProviderDeviceQoSReservations() {
		return providerDeviceQoSReservations;
	}

	public void setProviderDeviceQoSReservations(
		List<QoS_Resource_Reservation> providerDeviceQoSReservations) {
		this.providerDeviceQoSReservations = providerDeviceQoSReservations;
	}

	public List<QoS_Resource_Reservation> getConsumerDeviceQoSReservations() {
		return consumerDeviceQoSReservations;
	}

	public void setConsumerDeviceQoSReservations(
		List<QoS_Resource_Reservation> consumerDeviceQoSReservations) {
		this.consumerDeviceQoSReservations = consumerDeviceQoSReservations;
	}

	public Map<String, String> getRequestedQoS() {
		return requestedQoS;
	}

	public void setRequestedQoS(Map<String, String> requestedQoS) {
		this.requestedQoS = requestedQoS;
	}

	public Map<String, String> getCommands() {
		return commands;
	}

	public void setCommands(Map<String, String> commands) {
		this.commands = commands;
	}

}
