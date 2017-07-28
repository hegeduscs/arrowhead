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
package eu.arrowhead.qos;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.qos.Network;
import eu.arrowhead.common.database.qos.Network_Device;
import eu.arrowhead.common.database.qos.QoS_Resource_Reservation;
import eu.arrowhead.common.exception.DriverNotFoundException;
import eu.arrowhead.common.exception.ReservationException;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.AddMonitorRule;
import eu.arrowhead.common.model.messages.QoSReservationCommand;
import eu.arrowhead.common.model.messages.QoSReservationResponse;
import eu.arrowhead.common.model.messages.QoSReserve;
import eu.arrowhead.common.model.messages.QoSVerificationResponse;
import eu.arrowhead.common.model.messages.QoSVerifierResponse;
import eu.arrowhead.common.model.messages.QoSVerify;
import eu.arrowhead.qos.algorithms.VerifierAlgorithmFactory;
import eu.arrowhead.qos.drivers.DriversFactory;
import eu.arrowhead.qos.factories.QoSFactory;
import eu.arrowhead.qos.factories.SCSFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.hibernate.cfg.NotYetImplementedException;

public class QoSManagerService {

  private static Logger log = Logger.getLogger(QoSManagerService.class.
      getName());
  private VerifierAlgorithmFactory algorithmFactory;

  private SCSFactory scsfactory;
  private QoSFactory qosfactory;

  private Client client;

  public QoSManagerService() {
    algorithmFactory = VerifierAlgorithmFactory.getInstance();
    qosfactory = QoSFactory.getInstance();
    scsfactory = SCSFactory.getInstance();
    client = ClientBuilder.newClient();
  }

  /**
   * Verifies if the requestedQoS is possible on the selected providers.
   *
   * @param message QoSVerify parameters.
   * @return Returns if is possible or not and why.
   */
  public QoSVerificationResponse qoSVerify(QoSVerify message) {
    QoSVerificationResponse qosVerificationResponse = new QoSVerificationResponse();
    log.info("QoS: Verifying QoS paramteres.");

    // Get The Consumer Device to get all of its capabilites
    Network_Device consumer_network_device = scsfactory.
        getNetworkDeviceFromSystem(message.getConsumer());
    // Get The Consumer QoSReservations
    List<QoS_Resource_Reservation> consumerDeviceQoSReservations = qosfactory.
        getQoSReservationsFromArrowheadSystem(message.getConsumer());

    for (ArrowheadSystem system : message.getProvider()) {
      // Get Provider Network - TO GET CAPABILITIES
      Network_Device provider_network_device = scsfactory.
          getNetworkDeviceFromSystem(system);
      if (provider_network_device == null) {
        continue;
      }

      // Get Provider Arrowhead System - TO GET QOSRESERVATIONS
      List<QoS_Resource_Reservation> providerDeviceQoSReservations = qosfactory.
          getQoSReservationsFromArrowheadSystem(system);

      // GET NETWORK TO GET ITS TYPE: ex:FTTSE
      // Network network =
      // scsfactory.getNetworkFromNetworkDevice(provider_network_device);
      Network network = provider_network_device.getNetwork();
      if (network == null) {
        continue;
      }

      // Run Algorithm
      QoSVerifierResponse response;
      try {
        response = algorithmFactory.verify(network.
                                               getNetworkType(),
                                           provider_network_device.
                                               getNetworkCapabilities(), consumer_network_device.
                getNetworkCapabilities(),
                                           providerDeviceQoSReservations,
                                           consumerDeviceQoSReservations, message.
                getRequestedQoS(),
                                           message.getCommands());

        updateQoSVerificationResponse(system, response, qosVerificationResponse);
      } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException ex) {
        Logger.getLogger(QoSManagerService.class.getName()).
            log(Level.SEVERE, null, ex);
      }
    }

    log.info("QoS: QoS paramteres verified.");
    return qosVerificationResponse;
  }

  /**
   * Reserves a QoS on the consumer and provider stream.
   *
   * @param message QoSReservation parameters.
   * @return Returns if the reservation was possible.
   * @throws ReservationException The reservation on the devices was not possible.
   * @throws DriverNotFoundException The network type doesnt have a driver assigned.
   */
  public QoSReservationResponse qoSReserve(QoSReserve message)
      throws ReservationException, DriverNotFoundException, IOException {
    QoSReservationResponse qosreservationResponse;

    // Go To System Configuration Store get NetworkDevice
    ArrowheadSystem consumer = message.getConsumer();
    ArrowheadSystem provider = message.getProvider();

    // Get The Consumer Device to get all of its capabilites
    Network_Device consumer_network_device = scsfactory.
        getNetworkDeviceFromSystem(consumer);
    // Get The Producer Device to get all of its capabilites
    Network_Device provider_network_device = scsfactory.
        getNetworkDeviceFromSystem(provider);
    if (provider_network_device == null) {
      throw new ReservationException("");
    }

    // TODO: GET NETWORK
    Network network = provider_network_device.getNetwork();
    if (network == null) {
      return new QoSReservationResponse(false);
    }
    /**
     * *******************************************************
     ******************* Generate Commands *******************
     * *******************************************************
     */

		/* FOR BOTH */
    Map<String, String> responseS;
    Boolean wasSucessful = false;
    try {
      responseS = DriversFactory.getInstance().
          generateCommands(network.getNetworkType(),
                           network.getNetworkConfigurations(), provider, consumer, message.
                  getService(), message.getCommands(),
                           message.getRequestedQoS());
      /**
       * **** IF SUCCESS: Saving Quality of Service Reservation on
       * DataBase *
       */
      /* Create Message Stream */
      wasSucessful = qosfactory.
          saveMessageStream(provider, consumer, message.getService(),
                            message.getRequestedQoS(),
                            responseS,
                            network.getNetworkType());

      scsfactory.updateNetwork(network);

      if (wasSucessful) {
        /**
         * IF SUCCESS: Contact Monitoring Core System *
         */
        // Send to Monitor
        AddMonitorRule rule = new AddMonitorRule();
        rule.setProvider(provider);
        rule.setConsumer(consumer);
        rule.setSoftRealTime(false);
        rule.setProtocol(network.getNetworkType().toUpperCase());
        Map<String, String> temp = new HashMap<>();
        temp.putAll(message.getRequestedQoS());
        temp.putAll(network.getNetworkConfigurations());

        rule.setParameters(temp);

        boolean response = contactMonitoring(rule);
        qosreservationResponse = new QoSReservationResponse(response,
                                                            new QoSReservationCommand(
                                                                message.getService(),
                                                                message.getConsumer(),
                                                                message.getProvider(), responseS,
                                                                message.getRequestedQoS()));
        return qosreservationResponse;

      } else {
        // voltar a tras
      }
    } catch (Exception ex) {
      throw new ReservationException(ex.getMessage());
    }

    return null;
  }

  /**
   * Fills the parameters of a qosVerify message.
   *
   * @param system ArrowheadSystem
   * @param v Was possible.
   * @param qosVerificationResponse Reject Motivation Reason.
   */
  protected void updateQoSVerificationResponse(ArrowheadSystem system,
                                               QoSVerifierResponse v,
                                               QoSVerificationResponse qosVerificationResponse) {
    boolean isPossible = v.getResponse();
    qosVerificationResponse.addResponse(system, v.getResponse());
    if (!isPossible) {
      qosVerificationResponse.addRejectMotivation(system, v.
          getRejectMotivation());
    }
  }

  /**
   * Contacts the QoSMonitoring
   *
   * @param rule Message to send to the QoSMonitor.
   * @return Returns true if it was successful.
   */
  protected boolean contactMonitoring(AddMonitorRule rule) throws IOException {
    // READ FROM PROPERTIES FILE
    Properties props = new Properties();
    InputStream inputStream = getClass().getClassLoader().
        getResourceAsStream("monitor.properties");
    if (inputStream != null) {
      props.load(inputStream);
      inputStream.close();
    } else {
      String exMsg = "Properties file 'monitor.properties' not found in the classpath";
      log.log(Level.SEVERE, exMsg);
      throw new FileNotFoundException(exMsg);
    }
    final String URL = props.getProperty("monitor.url");
    Response response = Utility.sendRequest(URL, "POST", rule);

    return response.getStatus() > 199 && response.getStatus() < 300;
  }

  /**
   * Deletes a stream configuration between provider and consumer.
   *
   * @param qosReservation QoSReservation to be deleted.
   * @return True if was successfull.
   */
  public boolean deleteQoSReserve(QoSReserve qosReservation) {
    throw new NotYetImplementedException();
  }

}
