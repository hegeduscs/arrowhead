package eu.arrowhead.common.database.qos;


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
		ArrowheadSystem_qos system);

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
	public List<ArrowheadSystem_qos> getAllArrowheadSystems();

	/**
	 * get all arrowhead services
	 *
	 * @return return list of arrowhead services
	 */
	public List<ArrowheadService_qos> getAllArrowheadServices();

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
	public ArrowheadSystem_qos getArrowheadSystem(ArrowheadSystem_qos system);

	/**
	 * get arrowhead service
	 *
	 * @param service arrowhead service
	 * @return returns arrowhead service
	 */
	public ArrowheadService_qos getArrowheadService(ArrowheadService_qos service);

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
	public boolean deleteArrowheadSystem(ArrowheadSystem_qos system);

	/**
	 * delete arrowhead service
	 *
	 * @param service arrowhead service
	 * @return returns true if arrowhead service was deleted
	 */
	public boolean deleteArrowheadService(ArrowheadService_qos service);

}
