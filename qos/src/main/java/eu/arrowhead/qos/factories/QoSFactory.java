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

import eu.arrowhead.common.database.qos.IQoSRepository;
import eu.arrowhead.common.database.qos.Message_Stream;
import eu.arrowhead.common.database.qos.QoSRepositoryImpl;
import eu.arrowhead.common.database.qos.QoS_Resource_Reservation;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.QoSReservationForm;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QoSFactory {

  private static QoSFactory instance;
  private IQoSRepository repo;

  private QoSFactory() {
    super();
    repo = new QoSRepositoryImpl();
  }

  /**
   * Returns a instance from this singleton class.
   */
  public static QoSFactory getInstance() {
    if (instance == null) {
      instance = new QoSFactory();
    }
    return instance;
  }

  private static eu.arrowhead.common.database.qos.ArrowheadSystem_qos convertFromDTO(eu.arrowhead.common.model.ArrowheadSystem in) {

    eu.arrowhead.common.database.qos.ArrowheadSystem_qos out = new eu.arrowhead.common.database.qos.ArrowheadSystem_qos();

    out.setAuthenticationInfo(in.getAuthenticationInfo());
    out.setAddress(in.getAddress());
    out.setPort(in.getPort());
    out.setSystemGroup(in.getSystemGroup());
    out.setSystemName(in.getSystemName());

    return out;
  }

  private static eu.arrowhead.common.model.ArrowheadSystem convertToDTO(eu.arrowhead.common.database.qos.ArrowheadSystem_qos in) {

    eu.arrowhead.common.model.ArrowheadSystem out = new eu.arrowhead.common.model.ArrowheadSystem();

    out.setAuthenticationInfo(in.getAuthenticationInfo());
    out.setAddress(in.getAddress());
    out.setPort(in.getPort());
    out.setSystemGroup(in.getSystemGroup());
    out.setSystemName(in.getSystemName());

    return out;
  }

  private static List<ArrowheadSystem> convertToDTO_List(List<eu.arrowhead.common.database.qos.ArrowheadSystem_qos> in) {
    if (in == null) {
      return null;
    }
    List<ArrowheadSystem> out = new ArrayList<>();
    for (eu.arrowhead.common.database.qos.ArrowheadSystem_qos system : in) {
      out.add(convertToDTO(system));
    }

    return out;
  }

  protected static List<eu.arrowhead.common.database.qos.ArrowheadSystem_qos> convertFromDTO_List(List<ArrowheadSystem> in) {
    if (in == null) {
      return null;
    }
    List<eu.arrowhead.common.database.qos.ArrowheadSystem_qos> out = new ArrayList<>();
    for (ArrowheadSystem system : in) {
      out.add(convertFromDTO(system));
    }

    return out;
  }

  private static eu.arrowhead.common.database.qos.ArrowheadService_qos convertFromDTO(eu.arrowhead.common.model.ArrowheadService in) {

    eu.arrowhead.common.database.qos.ArrowheadService_qos out = new eu.arrowhead.common.database.qos.ArrowheadService_qos();

    out.setInterfaces(in.getInterfaces());
    out.setServiceDefinition(in.getServiceDefinition());
    out.setServiceGroup(in.getServiceGroup());

    return out;
  }

  private static eu.arrowhead.common.model.ArrowheadService convertToDTO(eu.arrowhead.common.database.qos.ArrowheadService_qos in) {

    eu.arrowhead.common.model.ArrowheadService out = new eu.arrowhead.common.model.ArrowheadService();

    out.setInterfaces(in.getInterfaces());
    out.setServiceDefinition(in.getServiceDefinition());
    out.setServiceGroup(in.getServiceGroup());

    return out;
  }

  private static List<ArrowheadService> convertToDTO_ArrowheadServices(List<eu.arrowhead.common.database.qos.ArrowheadService_qos> in) {

    if (in == null) {
      return null;
    }
    List<ArrowheadService> out = new ArrayList<>();
    for (eu.arrowhead.common.database.qos.ArrowheadService_qos system : in) {
      out.add(convertToDTO(system));
    }

    return out;
  }

  public IQoSRepository getRepo() {
    return repo;
  }

  public void setRepo(IQoSRepository repo) {
    this.repo = repo;
  }

  /**
   * Return all the QoS reservations of the selected system.
   *
   * @param provider ArrowheadSystem_qos.
   * @return Returns all the QoSReservations.
   */
  public List<QoS_Resource_Reservation> getQoSReservationsFromArrowheadSystem(ArrowheadSystem provider) {
    return repo.
        getQoSReservationsFromArrowheadSystem(convertFromDTO(provider));
  }

  /**
   * Saves a messagae stream.
   *
   * @param provider ArrowheadSystem_qos that provides the service.
   * @param consumer ArrowheadSystem_qos that consumes the service.
   * @param service ArrowheadService_qos is the service that will be consumed and provided.
   * @param qualityOfService Requestet QoS.
   * @param messageConfigurationParameters Stream configuration parameters between consumer and provider.
   * @param type Network Type.
   * @return Return true when successful.
   */
  public boolean saveMessageStream(ArrowheadSystem provider, ArrowheadSystem consumer, ArrowheadService service, Map<String, String> qualityOfService,
                                   Map<String, String> messageConfigurationParameters, String type) {

    Message_Stream m = new Message_Stream(convertFromDTO(service), convertFromDTO(consumer), convertFromDTO(provider), qualityOfService,
                                          messageConfigurationParameters, type);
    return repo.saveMessageStream(m) != null;
  }

  /**
   * Get QoSReserations from a filter that contains a qos specification.
   *
   * @param filter QoS specification (ex. bandwidth, delay).
   * @return Returns QoSReservation.
   */
  public List<QoSReservationForm> getQoSReservationFromFilter(Map<String, String> filter) {
    List<QoSReservationForm> output = new ArrayList<>();
    for (Message_Stream m : repo.
        getQoS_Resource_ReservationsFromFilter(filter)) {
      output.add(new QoSReservationForm(convertToDTO(m.getService()), convertToDTO(m.
          getProvider()), convertToDTO(m.getConsumer()), m.
          getQualityOfService().
          getQosParameters()));
    }
    return output;
  }

  /**
   * Get all qos reservations.
   *
   * @return Returns list of qos reservations.
   */
  public List<QoS_Resource_Reservation> getAllQoS_Resource_Reservations() {
    return repo.getAllQoS_Resource_Reservations();
  }

  /**
   * Get all messages streams.
   *
   * @return Returns list of MessageStream.
   */
  public List<Message_Stream> getAllMessage_Streams() {
    return repo.getAllMessage_Streams();
  }

  /**
   * Get all arrowhead systems.
   *
   * @return Returns list of arrowhead systems.
   */
  public List<ArrowheadSystem> getAllArrowheadSystems() {
    return convertToDTO_List(repo.getAllArrowheadSystems());
  }

  /**
   * Get all services.
   *
   * @return Returns list of services.
   */
  public List<ArrowheadService> getAllArrowheadServices() {
    return convertToDTO_ArrowheadServices(repo.getAllArrowheadServices());
  }

  /**
   * Get MessageStream from ID.
   *
   * @param messageStream Message Stream to be searched on db.
   * @return Returns the message stream found on the db.
   */
  public Message_Stream getMessage_Stream(Message_Stream messageStream) {
    return repo.getMessage_Stream(messageStream);
  }

  /**
   * Get arrowhead system
   *
   * @param system ArrowheadSystem_qos to be searched on db.
   * @return Returns the system found on the db.
   */
  public ArrowheadSystem getArrowheadSystem(ArrowheadSystem system) {
    return convertToDTO(repo.getArrowheadSystem(convertFromDTO(system)));
  }

  /**
   * Get service.
   *
   * @param service ArrowheadService_qos to be searched on db.
   * @return Returns a arrowheadService.
   */
  public ArrowheadService getArrowheadService(ArrowheadService service) {
    return convertToDTO(repo.getArrowheadService(convertFromDTO(service)));
  }

  /**
   * Save a stream.
   *
   * @param messageStream Message Stream to be saved.
   * @return Returns the messageStream with the ID.
   */
  public Message_Stream saveMessageStream(Message_Stream messageStream) {
    return repo.saveMessageStream(messageStream);
  }

  /**
   * Delete a stream.
   *
   * @param messageStream Message stream to be deleted.
   * @return Returns true when the stream was successfully deleted.
   */
  public boolean deleteMessageStream(Message_Stream messageStream) {
    return repo.deleteMessageStream(messageStream);
  }

  /**
   * Delete a system.
   *
   * @param system System to be deleted.
   * @return Returns true when the system was successfully deleted.
   */
  public boolean deleteArrowheadSystem(ArrowheadSystem system) {
    return repo.deleteArrowheadSystem(convertFromDTO(system));
  }

  /**
   *
   * @param service
   * @return
   */
  public boolean deleteArrowheadService(ArrowheadService service) {
    return repo.deleteArrowheadService(convertFromDTO(service));
  }

}
