/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.InterCloudAuthorization;
import eu.arrowhead.common.database.IntraCloudAuthorization;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.ArrowheadToken;
import eu.arrowhead.common.messages.InterCloudAuthRequest;
import eu.arrowhead.common.messages.InterCloudAuthResponse;
import eu.arrowhead.common.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.messages.TokenData;
import eu.arrowhead.common.messages.TokenGenerationRequest;
import eu.arrowhead.common.messages.TokenGenerationResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

public final class AuthorizationService {

  static final DatabaseManager dm = DatabaseManager.getInstance();

  private static final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(AuthorizationService.class.getName());

  private AuthorizationService() throws AssertionError {
    throw new AssertionError("AuthorizationService is a non-instantiable class");
  }

  /**
   * Checks whether the consumer System can use a Service from a list of provider Systems.
   *
   * @return IntraCloudAuthResponse
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  public static IntraCloudAuthResponse isSystemAuthorized(IntraCloudAuthRequest request) {
    if (!request.isValid()) {
      log.error("isSystemAuthorized BadPayloadException");
      throw new BadPayloadException("Bad payload: missing/incomplete consumer, service or providerList in the request.",
                                    Status.BAD_REQUEST.getStatusCode(), BadPayloadException.class.getName(),
                                    "AuthorizationService:isSystemAuthorized");
    }

    restrictionMap.put("systemName", request.getConsumer().getSystemName());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.error("Consumer is not in the database. isSystemAuthorized DataNotFoundException");
      throw new DataNotFoundException("Consumer System is not in the authorization database. " + request.getConsumer().getSystemName(),
                                      Status.NOT_FOUND.getStatusCode(), DataNotFoundException.class.getName(),
                                      "AuthorizationService:isSystemAuthorized");
    }

    IntraCloudAuthResponse response = new IntraCloudAuthResponse();
    HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<>();
    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + request.getService().toString() + " is not in the database. Returning NOT AUTHORIZED state for the consumer.");
      for (ArrowheadSystem provider : request.getProviders()) {
        authorizationState.put(provider, false);
      }
      response.setAuthorizationMap(authorizationState);
      return response;
    }

    IntraCloudAuthorization authRight;
    int authorizedCount = 0;
    for (ArrowheadSystem provider : request.getProviders()) {
      restrictionMap.clear();
      restrictionMap.put("systemName", provider.getSystemName());
      ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);

      restrictionMap.clear();
      restrictionMap.put("consumer", consumer);
      restrictionMap.put("provider", retrievedSystem);
      restrictionMap.put("service", service);
      authRight = dm.get(IntraCloudAuthorization.class, restrictionMap);

      if (authRight == null) {
        authorizationState.put(provider, false);
      } else {
        authorizationState.put(provider, true);
        authorizedCount++;
      }
    }

    log.info(
        "IntraCloud auth check for consumer " + request.getConsumer().getSystemName() + " returns with " + authorizedCount + " possible provider");
    response.setAuthorizationMap(authorizationState);
    return response;
  }

  /**
   * Checks whether an external Cloud can use a local Service.
   *
   * @return boolean
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  public static InterCloudAuthResponse isCloudAuthorized(InterCloudAuthRequest request) {
    if (!request.isValid()) {
      log.error("isCloudAuthorized BadPayloadException");
      throw new BadPayloadException("Bad payload: missing/incomplete cloud or service in the request payload.", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), "AuthorizationService:isCloudAuthorized");
    }

    restrictionMap.put("operator", request.getCloud().getOperator());
    restrictionMap.put("cloudName", request.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.error("Requester cloud is not in the database. isCloudAuthorized DataNotFoundException");
      throw new DataNotFoundException("Consumer Cloud is not in the authorization database. " + request.getCloud().toString(),
                                      Status.NOT_FOUND.getStatusCode(), DataNotFoundException.class.getName(),
                                      "AuthorizationService:isCloudAuthorized");
    }

    restrictionMap.clear();
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + request.getService().toString() + " is not in the database. Returning NOT AUTHORIZED state for the consumer.");
      return new InterCloudAuthResponse(false);
    }

    InterCloudAuthorization authRight;
    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    restrictionMap.put("service", service);
    authRight = dm.get(InterCloudAuthorization.class, restrictionMap);

    boolean isAuthorized = false;
    if (authRight != null) {
      isAuthorized = true;
    }

    log.info("Consumer Cloud is authorized: " + isAuthorized);
    return new InterCloudAuthResponse(isAuthorized);
  }

  /**
   * Generates ArrowheadTokens for each consumer/service/provider trio
   *
   * @return TokenGenerationResponse
   */
  public static TokenGenerationResponse tokenGeneration(TokenGenerationRequest request) {
    if (!request.isValid()) {
      log.error("tokenGeneration BadPayloadException");
      throw new BadPayloadException("TokenGenerationRequest has missing/incomplete fields.", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), "AuthorizationService:tokenGeneration");
    }

    // Get the tokens from the service class (can throw run time exceptions)
    List<ArrowheadToken> tokens = TokenGenerationService.generateTokens(request);
    List<TokenData> tokenDataList = new ArrayList<>();

    // Only add the successfully created tokens to the response, with the matching provider System
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i) != null) {
        TokenData tokenData = new TokenData(request.getProviders().get(i), tokens.get(i).getToken(), tokens.get(i).getSignature());
        tokenDataList.add(tokenData);
      }
    }

    log.info("Token generation returns with " + tokenDataList.size() + " arrowhead tokens.");
    return new TokenGenerationResponse(tokenDataList);
  }

}
