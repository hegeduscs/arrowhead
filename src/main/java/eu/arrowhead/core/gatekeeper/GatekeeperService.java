/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.Broker;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ConnectToConsumerRequest;
import eu.arrowhead.common.messages.ConnectToConsumerResponse;
import eu.arrowhead.common.messages.GSDAnswer;
import eu.arrowhead.common.messages.GSDPoll;
import eu.arrowhead.common.messages.GSDRequestForm;
import eu.arrowhead.common.messages.GSDResult;
import eu.arrowhead.common.messages.GatewayConnectionInfo;
import eu.arrowhead.common.messages.ICNEnd;
import eu.arrowhead.common.messages.ICNProposal;
import eu.arrowhead.common.messages.ICNRequestForm;
import eu.arrowhead.common.messages.ICNResult;
import eu.arrowhead.common.messages.OrchestrationForm;
import eu.arrowhead.common.messages.OrchestrationResponse;
import eu.arrowhead.core.ArrowheadMain;
import eu.arrowhead.core.gateway.GatewayService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

public final class GatekeeperService {

  static final int timeout = ArrowheadMain.getProp().getIntProperty("gateway_socket_timeout", 30000);
  static final DatabaseManager dm = DatabaseManager.getInstance();

  private static final String GATEWAY_ADDRESS = ArrowheadMain.getProp().getProperty("gateway_address");
  private static final Logger log = Logger.getLogger(GatekeeperService.class.getName());

  private GatekeeperService() throws AssertionError {
    throw new AssertionError("GatekeeperService is a non-instantiable class");
  }

  /**
   * This function represents the consumer-side of GlobalServiceDiscovery, where the GateKeeper of the consumer System tries to find a provider Cloud
   * for the requested Service.
   *
   * @return GSDResult
   */
  public static GSDResult GSDRequest(GSDRequestForm requestForm) {
    if (!requestForm.isValid()) {
      log.error("GSDRequest BadPayloadException");
      throw new BadPayloadException("Bad payload: requestedService is missing or it is not valid.", Status.BAD_REQUEST.getStatusCode(),
                                    "GatekeeperService:GSDRequest");
    }

    ArrowheadCloud ownCloud = Utility.getOwnCloud();
    GSDPoll gsdPoll = new GSDPoll(requestForm.getRequestedService(), ownCloud);

    // If no preferred Clouds were given, send GSD poll requests to the neighbor Clouds
    List<String> cloudURIs = new ArrayList<>();
    if (requestForm.getSearchPerimeter().isEmpty()) {
      cloudURIs = Utility.getNeighborCloudURIs();
    }
    // If there are preferred Clouds given, send GSD poll requests there
    else {
      String uri;
      for (ArrowheadCloud cloud : requestForm.getSearchPerimeter()) {
        try {
          uri = Utility.getUri(cloud.getAddress(), cloud.getPort(), cloud.getGatekeeperServiceURI(), cloud.isSecure(), false);
        }
        // We skip the clouds with missing information
        catch (NullPointerException ex) {
          continue;
        }
        cloudURIs.add(uri);
      }
    }
    log.info("Sending GSD poll request to " + cloudURIs.size() + " clouds.");

    // Finalizing the URIs, process the responses
    List<GSDAnswer> gsdAnswerList = new ArrayList<>();
    Response response;
    int i = 0;
    for (String uri : cloudURIs) {
      uri = UriBuilder.fromPath(uri).path("gsd_poll").toString();
      try {
        response = Utility.sendRequest(uri, "PUT", gsdPoll);
      }
      // We skip those that did not respond positively, add the rest to the result list
      catch (ArrowheadException ex) {
        // If it is the last iteration and we had no positive responses to the GSD, send exception instead
        if (i == cloudURIs.size() - 1 && gsdAnswerList.isEmpty()) {
          ex.printStackTrace();
          log.error("GSD failed for all potential provider clouds! See stack traces for details in console output.");
          throw new ArrowheadException("GSD failed for all potential provider clouds! The last exception message: " + ex.getMessage(),
                                       ex.getErrorCode(), "GatekeeperService:GSDRequest");
        } else {
          System.out.println("GSD request failed at: " + uri);
          ex.printStackTrace();
          System.out.println("Continuing the GSD with the next cloud!");
          continue;
        }
      } finally {
        i++;
      }
      gsdAnswerList.add(response.readEntity(GSDAnswer.class));
    }

    // Sending back the results. The orchestrator will validate the results (result list might be empty) and decide how to proceed.
    log.info("GSDRequest: Sending " + gsdAnswerList.size() + " GSDPoll results to Orchestrator.");
    return new GSDResult(gsdAnswerList);
  }

  /**
   * This function represents the consumer-side of InterCloudNegotiations, where the Gatekeeper sends information about the requester System. (SSL
   * secured)
   *
   * @return ICNResult
   */
  public static ICNResult ICNRequest(ICNRequestForm requestForm) {
    if (!requestForm.isValid()) {
      log.error("ICNRequest BadPayloadException");
      throw new BadPayloadException("Bad payload: missing/incomplete ICNRequestForm.", Status.BAD_REQUEST.getStatusCode(),
                                    "GatekeeperService:ICNRequest");
    }

    requestForm.getNegotiationFlags().put("useGateway", ArrowheadMain.USE_GATEWAY);
    // Compiling the payload and then getting the request URI
    ICNProposal icnProposal = new ICNProposal(requestForm.getRequestedService(), Utility.getOwnCloud(), requestForm.getRequesterSystem(),
                                              requestForm.getPreferredSystems(), requestForm.getNegotiationFlags(), null, timeout, null);

    if (ArrowheadMain.USE_GATEWAY) {
      icnProposal.setPreferredBrokers(dm.getAll(Broker.class, null));
      icnProposal.setGatewayPublicKey(GatewayService.GATEWAY_PUBLIC_KEY);
    }

    String icnUri = Utility.getUri(requestForm.getTargetCloud().getAddress(), requestForm.getTargetCloud().getPort(),
                                   requestForm.getTargetCloud().getGatekeeperServiceURI(), requestForm.getTargetCloud().isSecure(), false);
    icnUri = UriBuilder.fromPath(icnUri).path("icn_proposal").toString();
    // Sending the request, the response payload is use_gateway flag dependent
    Response response = Utility.sendRequest(icnUri, "PUT", icnProposal);

    // If the gateway services are not requested, then just send back the ICN results to the Orchestrator right away
    if (!ArrowheadMain.USE_GATEWAY) {
      log.info("ICNRequest: returning ICNResult to Orchestrator.");
      return response.readEntity(ICNResult.class);
    }
    // The partner Gatekeeper will return an ICNEnd if use_gateway = true
    ICNEnd icnEnd = response.readEntity(ICNEnd.class);

    // Compiling the gateway request payload
    Map<String, String> metadata = requestForm.getRequestedService().getServiceMetadata();
    boolean isSecure = metadata.containsKey("security") && !metadata.get("security").equals("none");
    GatewayConnectionInfo gwConnInfo = icnEnd.getGatewayConnInfo();
    ConnectToConsumerRequest connectionRequest = new ConnectToConsumerRequest(gwConnInfo.getBrokerName(), gwConnInfo.getBrokerPort(),
                                                                              gwConnInfo.getQueueName(), gwConnInfo.getControlQueueName(),
                                                                              requestForm.getRequesterSystem(),
                                                                              icnEnd.getOrchestrationForm().getProvider(), Utility.getOwnCloud(),
                                                                              requestForm.getTargetCloud(), requestForm.getRequestedService(),
                                                                              isSecure, timeout, gwConnInfo.getGatewayPublicKey());

    ConnectToConsumerResponse connectToConsumerResponse = GatewayService.connectToConsumer(connectionRequest);

    ArrowheadSystem gatewaySystem = new ArrowheadSystem();
    gatewaySystem.setSystemName("gateway");
    gatewaySystem.setAddress(GATEWAY_ADDRESS);
    gatewaySystem.setPort(connectToConsumerResponse.getServerSocketPort());
    gatewaySystem.setAuthenticationInfo(GatewayService.GATEWAY_PUBLIC_KEY);
    icnEnd.getOrchestrationForm().setProvider(gatewaySystem);
    List<OrchestrationForm> orchResponse = new ArrayList<>();
    orchResponse.add(icnEnd.getOrchestrationForm());

    log.info("ICNRequest: returning ICNResult (with gateway address) to Orchestrator.");
    return new ICNResult(new OrchestrationResponse(orchResponse));
  }

}
