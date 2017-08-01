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

import eu.arrowhead.common.database.qos.Network;
import eu.arrowhead.common.database.qos.NetworkDevice;
import eu.arrowhead.common.database.qos.ResourceReservation;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.cfg.NotYetImplementedException;
//import org.hibernate.cfg.NotYetImplementedException;

final class QoSManagerService {

  private static Logger log = Logger.getLogger(QoSManagerService.class.getName());
  private static VerifierAlgorithmFactory algorithmFactory = VerifierAlgorithmFactory.getInstance();
  private static SCSFactory scsFactory = SCSFactory.getInstance();
  private static QoSFactory qosFactory = QoSFactory.getInstance();

  private QoSManagerService() throws AssertionError {
    throw new AssertionError("QoSManagerService is a non-instantiable class");
  }

  /**
   * Verifies if the requestedQoS is possible on the selected providers.
   *
   * @param message QoSVerify parameters.
   * @return Returns if is possible or not and why.
   */
  static QoSVerificationResponse qosVerify(QoSVerify message) {
    log.info("QoS: Verifying QoS paramteres.");

    // Get The Consumer Device to get all of its capabilites
    NetworkDevice consumerNetworkDevice = scsFactory.getNetworkDeviceFromSystem(message.getConsumer());
    // Get The Consumer QoSReservations
    List<ResourceReservation> consumerReservations = qosFactory.getReservationsFromSystem(message.getConsumer());

    QoSVerificationResponse qosVerificationResponse = new QoSVerificationResponse();
    for (ArrowheadSystem provider : message.getProviders()) {
      // Get Provider Network - TO GET CAPABILITIES
      NetworkDevice providerNetworkDevice = scsFactory.getNetworkDeviceFromSystem(provider);
      if (providerNetworkDevice == null) {
        continue;
      }
      // Get Provider Arrowhead System - TO GET QOSRESERVATIONS
      List<ResourceReservation> providerReservations = qosFactory.getReservationsFromSystem(provider);

      // GET NETWORK TO GET ITS TYPE: ex:FTTSE
      Network network = providerNetworkDevice.getNetwork();
      if (network == null) {
        continue;
      }

      // Run Algorithm
      try {
        QoSVerifierResponse response = algorithmFactory
            .verify(network.getNetworkType(), providerNetworkDevice.getNetworkCapabilities(), consumerNetworkDevice.getNetworkCapabilities(),
                    providerReservations, consumerReservations, message.getRequestedQoS(), message.getCommands());

        //TODO this hashmap solution is not elegant at all, rethinking of the verification response is needed at a later time
        qosVerificationResponse.addResponse(provider, response.getResponse());
        if (!response.getResponse()) {
          qosVerificationResponse.addRejectMotivation(provider, response.getRejectMotivation());
        }
      } catch (Exception ex) {
        log.error(ex.getClass() + " in QosManagerService:qosVerify");
        ex.printStackTrace();
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
  static QoSReservationResponse qosReserve(QoSReserve message) throws ReservationException, DriverNotFoundException, IOException {
    QoSReservationResponse qosreservationResponse;

    // Go To System Configuration Store get NetworkDevice
    ArrowheadSystem consumer = message.getConsumer();
    ArrowheadSystem provider = message.getProvider();

    // Get The Consumer Device to get all of its capabilites
    NetworkDevice consumer_network_device = scsFactory.
        getNetworkDeviceFromSystem(consumer);
    // Get The Producer Device to get all of its capabilites
    NetworkDevice provider_network_device = scsFactory.
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
          generateCommands(network.getNetworkType(), network.getNetworkConfigurations(), provider, consumer, message.
              getService(), message.getCommands(), message.getRequestedQoS());
      /**
       * **** IF SUCCESS: Saving Quality of Service Reservation on
       * DataBase *
       */
      /* Create Message Stream */
      wasSucessful = qosFactory.
          saveMessageStream(provider, consumer, message.getService(), message.getRequestedQoS(), responseS, network.getNetworkType());

      scsFactory.updateNetwork(network);

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
        qosreservationResponse = new QoSReservationResponse(response, new QoSReservationCommand(message.getService(), message.getConsumer(),
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
   * Contacts the QoSMonitoring
   *
   * @param rule Message to send to the QoSMonitor.
   * @return Returns true if it was successful.
   */
  private static boolean contactMonitoring(AddMonitorRule rule) throws IOException {
    //TODO app properties használata többi példájára
    /*Properties props = new Properties();
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

    return response.getStatus() > 199 && response.getStatus() < 300;*/

    return true;
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
