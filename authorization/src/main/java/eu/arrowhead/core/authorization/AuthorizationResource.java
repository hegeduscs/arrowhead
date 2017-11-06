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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

/**
 * This is the REST resource for the Authorization Core System.
 */
@Path("authorization")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

  private static Logger log = Logger.getLogger(AuthorizationResource.class.getName());
  private HashMap<String, Object> restrictionMap = new HashMap<>();
  static DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Authorization Resource.";
  }


  /**
   * Checks whether the consumer System can use a Service from a list of provider Systems.
   *
   * @return IntraCloudAuthResponse
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("intracloud")
  public Response isSystemAuthorized(IntraCloudAuthRequest request) {
    if (!request.isValid()) {
      log.error("isSystemAuthorized BadPayloadException");
      throw new BadPayloadException("IntraCloudAuthRequest bad payload: missing/incomplete consumer, service or providerList in the request.");
    }

    restrictionMap.put("systemGroup", request.getConsumer().getSystemGroup());
    restrictionMap.put("systemName", request.getConsumer().getSystemName());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.error("Consumer is not in the database. isSystemAuthorized DataNotFoundException");
      throw new DataNotFoundException("Consumer System is not in the authorization database. " + request.getConsumer().toString());
    }

    IntraCloudAuthResponse response = new IntraCloudAuthResponse();
    HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<>();
    restrictionMap.clear();
    restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + request.getService().toString() + " is not in the database. Returning NOT AUTHORIZED state for the consumer.");
      for (ArrowheadSystem provider : request.getProviders()) {
        authorizationState.put(provider, false);
      }
      response.setAuthorizationMap(authorizationState);
      return Response.status(Status.OK).entity(response).build();
    }

    IntraCloudAuthorization authRight;
    int authorizedCount = 0;
    for (ArrowheadSystem provider : request.getProviders()) {
      restrictionMap.clear();
      restrictionMap.put("systemGroup", provider.getSystemGroup());
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

    log.info("IntraCloud auth check for consumer " + request.getConsumer().toString() + " returns with " + authorizedCount + " possible provider");
    response.setAuthorizationMap(authorizationState);
    return Response.status(Status.OK).entity(response).build();
  }

  /**
   * Checks whether an external Cloud can use a local Service.
   *
   * @return boolean
   *
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("intercloud")
  public Response isCloudAuthorized(InterCloudAuthRequest request) {
    if (!request.isPayloadUsable()) {
      log.error("isCloudAuthorized BadPayloadException");
      throw new BadPayloadException("InterCloudAuthRequest bad payload: missing/incomplete cloud or service in the request payload.");
    }

    restrictionMap.put("operator", request.getCloud().getOperator());
    restrictionMap.put("cloudName", request.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.error("Requester cloud is not in the database. (isCloudAuthorized DataNotFoundException)");
      throw new DataNotFoundException("Consumer Cloud is not in the authorization database. " + request.getCloud().toString());
    }

    restrictionMap.clear();
    restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + request.getService().toString() + " is not in the database. Returning NOT AUTHORIZED state for the consumer.");
      return Response.status(Status.OK).entity(new InterCloudAuthResponse(false)).build();
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
    return Response.status(Status.OK).entity(new InterCloudAuthResponse(isAuthorized)).build();
  }

  /**
   * Generates ArrowheadTokens for each consumer/service/provider trio
   *
   * @return TokenGenerationResponse
   */
  @PUT
  @Path("token")
  public Response tokenGeneration(TokenGenerationRequest request) {
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
    return Response.status(Status.OK).entity(new TokenGenerationResponse(tokenDataList)).build();
  }

}
