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
package eu.arrowhead.qos.database.model;

import eu.arrowhead.qos.database.model.ArrowheadService;
import eu.arrowhead.qos.database.model.ArrowheadSystem;
import eu.arrowhead.qos.database.model.Message_Stream;
import eu.arrowhead.qos.database.model.QoS_Resource_Reservation;
import java.util.List;
import java.util.Map;

public interface IQoSRepository {

	/**
	 * get all qos reservations from the system
	 *
	 * @param system ArrowheadSyste
	 * @return returns list of qos reservations.
	 */
	public List<QoS_Resource_Reservation> getQoSReservationsFromArrowheadSystem(
		ArrowheadSystem system);

	/**
	 * get all qos reservations.
	 *
	 * @return returns list of qos reservations.
	 */
	public List<QoS_Resource_Reservation> getAllQoS_Resource_Reservations();

	/**
	 * get qos reservations from filter
	 *
	 * @param filter map with filter
	 * @return returns list of messages streams
	 */
	public List<Message_Stream> getQoS_Resource_ReservationsFromFilter(
		Map<String, String> filter);

	/**
	 * get all messages streams
	 *
	 * @return returns list of messages streams
	 */
	public List<Message_Stream> getAllMessage_Streams();

	/**
	 * get all arrowhead systems
	 *
	 * @return return list of arrowhead system
	 */
	public List<ArrowheadSystem> getAllArrowheadSystems();

	/**
	 * get all arrowhead services
	 *
	 * @return return list of arrowhead services
	 */
	public List<ArrowheadService> getAllArrowheadServices();

	/**
	 * get message stream
	 *
	 * @param messageStream message stream
	 * @return returns message stream
	 */
	public Message_Stream getMessage_Stream(Message_Stream messageStream);

	/**
	 * get arrowhead system
	 *
	 * @param system arrowhead system
	 * @return returns arrowhead system
	 */
	public ArrowheadSystem getArrowheadSystem(ArrowheadSystem system);

	/**
	 * get arrowhead service
	 *
	 * @param service arrowhead service
	 * @return returns arrowhead service
	 */
	public ArrowheadService getArrowheadService(ArrowheadService service);

	/**
	 * save message stream
	 *
	 * @param messageStream message stream
	 * @return returns message stream
	 */
	public Message_Stream saveMessageStream(Message_Stream messageStream);

	/**
	 * delete message stream
	 *
	 * @param messageStream message stream
	 * @return returns true if message stream was deleted
	 */
	public boolean deleteMessageStream(Message_Stream messageStream);

	/**
	 * delete arrowhead system
	 *
	 * @param system arrowhead system
	 * @return returns true if arrowhead system was deleted
	 */
	public boolean deleteArrowheadSystem(ArrowheadSystem system);

	/**
	 * delete arrowhead service
	 *
	 * @param service arrowhead service
	 * @return returns true if arrowhead service was deleted
	 */
	public boolean deleteArrowheadService(ArrowheadService service);

}
