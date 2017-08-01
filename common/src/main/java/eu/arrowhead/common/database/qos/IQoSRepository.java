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
  List<ResourceReservation> getQoSReservationsFromArrowheadSystem(ArrowheadSystem_qos system);

  /**
   * get all qos reservations.
   *
   * @return returns list of qos reservations.
   */
  List<ResourceReservation> getAllQoS_Resource_Reservations();

  /**
   * get qos reservations from filter
   *
   * @param filter map with filter
   * @return returns list of messages streams
   */
  List<Message_Stream> getQoS_Resource_ReservationsFromFilter(Map<String, String> filter);

  /**
   * get all messages streams
   *
   * @return returns list of messages streams
   */
  List<Message_Stream> getAllMessage_Streams();

  /**
   * get all arrowhead systems
   *
   * @return return list of arrowhead system
   */
  List<ArrowheadSystem_qos> getAllArrowheadSystems();

  /**
   * get all arrowhead services
   *
   * @return return list of arrowhead services
   */
  List<ArrowheadService_qos> getAllArrowheadServices();

  /**
   * get message stream
   *
   * @param messageStream message stream
   * @return returns message stream
   */
  Message_Stream getMessage_Stream(Message_Stream messageStream);

  /**
   * get arrowhead system
   *
   * @param system arrowhead system
   * @return returns arrowhead system
   */
  ArrowheadSystem_qos getArrowheadSystem(ArrowheadSystem_qos system);

  /**
   * get arrowhead service
   *
   * @param service arrowhead service
   * @return returns arrowhead service
   */
  ArrowheadService_qos getArrowheadService(ArrowheadService_qos service);

  /**
   * save message stream
   *
   * @param messageStream message stream
   * @return returns message stream
   */
  Message_Stream saveMessageStream(Message_Stream messageStream);

  /**
   * delete message stream
   *
   * @param messageStream message stream
   * @return returns true if message stream was deleted
   */
  boolean deleteMessageStream(Message_Stream messageStream);

  /**
   * delete arrowhead system
   *
   * @param system arrowhead system
   * @return returns true if arrowhead system was deleted
   */
  boolean deleteArrowheadSystem(ArrowheadSystem_qos system);

  /**
   * delete arrowhead service
   *
   * @param service arrowhead service
   * @return returns true if arrowhead service was deleted
   */
  boolean deleteArrowheadService(ArrowheadService_qos service);

}
