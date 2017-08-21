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

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.qos.DeployedSystem;
import eu.arrowhead.common.database.qos.MessageStream;
import eu.arrowhead.common.database.qos.Network;
import eu.arrowhead.common.database.qos.NetworkDevice;
import eu.arrowhead.common.database.qos.ResourceReservation;
import eu.arrowhead.common.exception.DriverNotFoundException;
import eu.arrowhead.common.exception.ReservationException;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.MonitorRule;
import eu.arrowhead.common.model.messages.QoSReservationCommand;
import eu.arrowhead.common.model.messages.QoSReservationResponse;
import eu.arrowhead.common.model.messages.QoSReserve;
import eu.arrowhead.common.model.messages.QoSVerificationResponse;
import eu.arrowhead.common.model.messages.QoSVerifierResponse;
import eu.arrowhead.common.model.messages.QoSVerify;
import eu.arrowhead.qos.algorithms.VerifierAlgorithmFactory;
import eu.arrowhead.qos.drivers.DriversFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.apache.log4j.Logger;

//TODO the cleaning I did was mostly syntax based, generally the logic of the methods (and the fields of the POJOs) need to be revised too
final class QoSManagerService {

  private static Logger log = Logger.getLogger(QoSManagerService.class.getName());
  private static VerifierAlgorithmFactory algorithmFactory = VerifierAlgorithmFactory.getInstance();
  private static DriversFactory driverFactory = DriversFactory.getInstance();
  private static DatabaseManager dm = DatabaseManager.getInstance();
  private static HashMap<String, Object> restrictionMap = new HashMap<>();

  private QoSManagerService() throws AssertionError {
    throw new AssertionError("QoSManagerService is a non-instantiable class");
  }

  /**
   * Verifies if the requestedQoS is possible on the selected providers.
   *
   * @param message QoSVerify parameters.
   *
   * @return Returns if is possible or not and why.
   */
  static QoSVerificationResponse qosVerify(QoSVerify message) {
    log.info("QoS: Verifying QoS paramteres.");

    NetworkDevice consumerNetworkDevice = getNetworkDeviceFromSystem(message.getConsumer());
    List<ResourceReservation> consumerReservations = getReservationsFromSystem(message.getConsumer());

    QoSVerificationResponse qosVerificationResponse = new QoSVerificationResponse();
    for (ArrowheadSystem provider : message.getProviders()) {
      NetworkDevice providerNetworkDevice = getNetworkDeviceFromSystem(provider);
      if (providerNetworkDevice == null) {
        continue;
      }
      List<ResourceReservation> providerReservations = getReservationsFromSystem(provider);

      Network network = providerNetworkDevice.getNetwork();
      if (network == null) {
        continue;
      }

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

  //TODO exception handling
  //TODO rewrite the reserve similar to verification, this needs to handle a list of providers, so the orch does not have to send multiple HTTP
  // request in a blocking for loop

  private static NetworkDevice getNetworkDeviceFromSystem(ArrowheadSystem system) {
    restrictionMap.clear();
    restrictionMap.put("systemGroup", system.getSystemGroup());
    restrictionMap.put("systemName", system.getSystemName());
    ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("system", retrievedSystem);
    DeployedSystem deployedSystem = dm.get(DeployedSystem.class, restrictionMap);

    return deployedSystem.getNetworkDevice();
  }

  private static List<ResourceReservation> getReservationsFromSystem(ArrowheadSystem system) {
    restrictionMap.clear();
    restrictionMap.put("systemGroup", system.getSystemGroup());
    restrictionMap.put("systemName", system.getSystemName());
    ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);

    //TODO this will get all the QoS reservations where the system is either consumer or provider.
    //limiting the filtering to just consumers/providers might be desired (need an extra boolean for argument for that)
    restrictionMap.clear();
    restrictionMap.put("consumer", retrievedSystem);
    restrictionMap.put("provider", retrievedSystem);
    List<MessageStream> messageStreams = dm.getAllOfEither(MessageStream.class, restrictionMap);
    List<ResourceReservation> reservations = new ArrayList<>();
    for (MessageStream ms : messageStreams) {
      reservations.add(ms.getQualityOfService());
    }

    return reservations;
  }

  /**
   * Reserves a QoS on the consumer and provider stream.
   *
   * @param message QoSReservation parameters.
   *
   * @return Returns if the reservation was possible.
   *
   * @throws ReservationException The reservation on the devices was not possible.
   * @throws DriverNotFoundException The network type doesnt have a driver assigned.
   */
  static QoSReservationResponse qosReserve(QoSReserve message) throws ReservationException, DriverNotFoundException, IOException {
    ArrowheadSystem consumer = message.getConsumer();
    ArrowheadSystem provider = message.getProvider();

    //TODO consider to delete these 2 variables, since they are not really used
    NetworkDevice consumerNetworkDevice = getNetworkDeviceFromSystem(consumer);
    NetworkDevice providerNetworkDevice = getNetworkDeviceFromSystem(provider);
    if (providerNetworkDevice == null) {
      throw new ReservationException("");
    }

    Network network = providerNetworkDevice.getNetwork();
    if (network == null) {
      return new QoSReservationResponse(false);
    }

    //Generate commands
    Map<String, String> commands = new HashMap<>();
    try {
      commands = driverFactory
          .generateCommands(network.getNetworkType(), network.getNetworkConfigurations(), provider, consumer, message.getService(),
                            message.getCommands(), message.getRequestedQoS());
    } catch (Exception e) {
      e.printStackTrace();
    }

    //TODO this "UP" state was hardcoded in their MessageStream constructor
    ResourceReservation reservation = new ResourceReservation("UP", message.getRequestedQoS());
    MessageStream stream = new MessageStream(message.getService(), consumer, provider, reservation, commands, network.getNetworkType());
    dm.save(stream);

    //Send MonitorRule to QoSMonitor
    Map<String, String> parameters = message.getRequestedQoS();
    parameters.putAll(network.getNetworkConfigurations());
    MonitorRule rule = new MonitorRule(network.getNetworkType().toUpperCase(), provider, consumer, parameters, false);
    Response response = Utility.sendRequest(QoSMain.MONITOR_URL, "POST", rule);
    //TODO it is uncertain what's the response from the QoSMonitor if something goes wrong, hopefully it sets the status code properly
    boolean ruleApplied = response.getStatusInfo().getFamily() == Family.SUCCESSFUL;

    return new QoSReservationResponse(ruleApplied,
                                      new QoSReservationCommand(message.getService(), message.getConsumer(), message.getProvider(), commands,
                                                                message.getRequestedQoS()));
  }

}
