/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

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
package eu.arrowhead.qos.drivers.implementations;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.messages.QoSReservationCommand;
import eu.arrowhead.qos.drivers.IQoSDriver;
import eu.arrowhead.qos.drivers.ReservationInfo;
import eu.arrowhead.qos.drivers.ReservationResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public class FTTSE implements IQoSDriver {

  protected final String STREAM_PARAMETERS_ASSYNCHRONOUS_HARD_REAL_TIME = "1";
  protected final String STREAM_PARAMETERS_ASSYNCHRONOUS_SOFT_REAL_TIME = "2";
  private final String NETWORK_EC = "EC";
  private final String NETWORK_STREAM_ID = "stream_id";
  private final String NETWORK_ENTRYPOINT_URL = "ENTRYPOINT_URL";
  private final String NETWORK_MTU = "MTU";

  public FTTSE() {
  }


  @Override
  public ReservationResponse reserveQoS(ReservationInfo info) {
    Map<String, String> networkConfiguration = info.getNetworkConfiguration();
    ArrowheadSystem provider = info.getProvider();
    ArrowheadSystem consumer = info.getConsumer();
    ArrowheadService service = info.getService();
    Map<String, String> commands = info.getCommands();
    Map<String, String> requestedQoS = info.getRequestedQoS();

    if (!validateNetworkCOnfiguration(networkConfiguration)) {
      throw new IllegalStateException("Theres no enouch network information.");
    }

    String url = networkConfiguration.get(NETWORK_ENTRYPOINT_URL);
    Integer ec = Integer.parseInt(networkConfiguration.get(NETWORK_EC));
    Integer streamID = Integer.parseInt(networkConfiguration.get(NETWORK_STREAM_ID));
    streamID++;
    Integer mtu = Integer.parseInt(networkConfiguration.get(NETWORK_MTU));
    // Update Network Configurations
    networkConfiguration.put(NETWORK_STREAM_ID, streamID.toString());
    // IF COMMANDS ARE NULL - THE DRIVER WILL GENERATE THE COMMANDS BASED ON
    // IF THE REQUESTED QOS
    if (commands == null && (requestedQoS != null || requestedQoS.size() == 0)) {
      commands = generateCommands(streamID, ec, mtu, requestedQoS);
      if (commands == null) {
        throw new IllegalStateException("Invalid Input");
      }
    } else if ((requestedQoS == null || requestedQoS.size() == 0) && commands == null) {
      commands = generateCommands(streamID, ec, mtu, requestedQoS);
      if (commands == null) {
        throw new IllegalStateException("Theres no enouch network information.");
      }
    } else {
      throw new IllegalStateException("Invalid Input");
    }

    // CONTACT THE ENTRYPOINT
    ClientConfig configuration = new ClientConfig();
    configuration.property(ClientProperties.CONNECT_TIMEOUT, 30000);
    configuration.property(ClientProperties.READ_TIMEOUT, 30000);
    Client client = ClientBuilder.newClient(configuration);
    URI uri = UriBuilder.fromPath(url + "/configure").build();

    WebTarget target = client.target(uri);
    Response response = target.request().header("Content-type", "application/json").post(Entity.json(new QoSReservationCommand(service, provider, consumer, commands, requestedQoS)));

    if (response.getStatus() > 199 && response.getStatus() < 300) {
      return new ReservationResponse(true, null, commands);
    }

    //everything is possible!
    return new ReservationResponse(false, "Not Possible", null);
  }

  /**
   * This method will see if there are enough parameters to generate the commands.
   */
  private boolean validateNetworkCOnfiguration(Map<String, String> networkConfiguration) {
    if (!networkConfiguration.containsKey(NETWORK_EC) || !networkConfiguration.
                                                                                  containsKey(NETWORK_ENTRYPOINT_URL) || !networkConfiguration.containsKey(NETWORK_STREAM_ID) || !networkConfiguration

        .containsKey(NETWORK_MTU)) {
      return false;
    }

    try {
      Integer.parseInt(networkConfiguration.get(NETWORK_EC));
      Integer.parseInt(networkConfiguration.get(NETWORK_STREAM_ID));
      Integer.parseInt(networkConfiguration.get(NETWORK_MTU));
    } catch (NumberFormatException e) {
      return false;
    }

    return true;
  }

  /**
   * This function will generate the stream commands.
   *
   * @param streamID Stream ID.
   * @param elementaryCycle Master paramter.
   * @param mtu Maximum Transmission Unit of the switch.
   * @param requestedQoS Consumer requested QoS.
   *
   * @return Returns the parameters of the stream to be configured.
   */

  public Map<String, String> generateCommands(Integer streamID, Integer elementaryCycle, Integer mtu, Map<String, String> requestedQoS) {
    Map<String, String> commands = new HashMap<>();
    Integer period = 5;
    Integer size = calculateSize(mtu);

    String STREAM_PARAMETERS_STREAM_ID = "ID";
    commands.put(STREAM_PARAMETERS_STREAM_ID, streamID.toString());
    String STREAM_PARAMETERS_SYNCHRONOUS_TYPE = "SYNCHRONOUS";
    String STREAM_PARAMETERS_PERIOD = "PERIOD";
    String STREAM_PARAMETERS_SIZE = "SIZE";
    if (requestedQoS == null || requestedQoS.isEmpty()) {
      String STREAM_PARAMETERS_BEST_EFFORT = "3";
      commands.
                  put(STREAM_PARAMETERS_SYNCHRONOUS_TYPE, STREAM_PARAMETERS_BEST_EFFORT);
      commands.put(STREAM_PARAMETERS_PERIOD, period.toString());
      commands.put(STREAM_PARAMETERS_SIZE, size.toString());
      return commands;
    }

    // CHECK DELAY
    String DELAY = "delay";
    if (requestedQoS.get(DELAY) != null) {
      Integer delay = Integer.parseInt(requestedQoS.get(DELAY));
      period = (int) ((double) delay / (double) elementaryCycle);
    }
    commands.put(STREAM_PARAMETERS_PERIOD, period.toString());

    /*
     * if period less than 1 the stream cannot be possible - this must never
     * happen because the verify method must verify this!
     */
    Integer MINIMUM_PERIOD = 1;
    if (period < MINIMUM_PERIOD) {
      throw new IllegalArgumentException("Period < MINIMUM_PERIOD");
    }

    // CHECK BANDWIDTH
    String BANDWIDTH = "bandwidth";
    if (requestedQoS.get(BANDWIDTH) != null) {
      Integer bandwidth = Integer.parseInt(requestedQoS.get(BANDWIDTH));
      size = (((elementaryCycle * period) * bandwidth) / 1000);
      if (size == 0) {
        size = 1;
      }
    }
    commands.put(STREAM_PARAMETERS_SIZE, size.toString());
    String STREAM_PARAMETERS_SYNCHRONOUS = "0";
    commands.
                put(STREAM_PARAMETERS_SYNCHRONOUS_TYPE, STREAM_PARAMETERS_SYNCHRONOUS);

    return commands;
  }

  public int calculateSize(Integer mtu) {
    final Integer MAXIMUM_SIZE = (mtu * 50) / 10;
    return MAXIMUM_SIZE;
  }

}
