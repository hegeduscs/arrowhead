package eu.arrowhead.core.authorization;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.InterCloudAuthorization;
import eu.arrowhead.common.database.IntraCloudAuthorization;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ArrowheadToken;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.InterCloudAuthResponse;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.model.messages.TokenGenerationRequest;
import eu.arrowhead.common.model.messages.TokenGenerationResponse;
import java.security.PublicKey;
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
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

  private static Logger log = Logger.getLogger(AuthorizationResource.class.getName());
  private DatabaseManager dm = DatabaseManager.getInstance();
  private HashMap<String, Object> restrictionMap = new HashMap<>();

  @GET
  public String getIt() {
    return "This is the Authorization Resource.";
  }


  /**
   * Generates token
   *
   * @return tokenGenerationResponse
   */
  @PUT
  @Path("/token")
  public Response resource(TokenGenerationRequest tokenGenerationRequest) {
    List<PublicKey> providerPublicKeys = AuthorizationService.getProviderPublicKeys(tokenGenerationRequest.getProviders());

    List<ArrowheadSystem> providers = tokenGenerationRequest.getProviders();

    TokenGenerationResponse tokenGenerationResponse = new TokenGenerationResponse();
    List<String> token = new ArrayList<>();
    List<String> signature = new ArrayList<>();

    try {
      for (int i = 0; i < providers.size(); i++) {
        PublicKey providerPublicKey = providerPublicKeys.get(i);
        ArrowheadSystem provider = providers.get(i);

        ArrowheadToken arrowheadToken = AuthorizationService
            .generateSingleToken(provider, providerPublicKey, tokenGenerationRequest.getConsumer(), tokenGenerationRequest.getConsumerCloud(),
                                 tokenGenerationRequest.getService(), tokenGenerationRequest.getDuration());

        token.add(arrowheadToken.getToken());
        tokenGenerationResponse.setToken(token);

        signature.add(arrowheadToken.getSignature());
        tokenGenerationResponse.setSignature(signature);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return Response.status(500).build();
    }

    return Response.status(200).entity(tokenGenerationResponse).build();
  }

  /**
   * Checks whether the consumer System can use a Service from a list of provider Systems.
   *
   * @return IntraCloudAuthResponse
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("/intracloud")
  public Response isSystemAuthorized(IntraCloudAuthRequest request) {
    log.info("Entered the AuthorizationResource:isSystemAuthorized function");

    if (!request.isPayloadUsable()) {
      log.info("AuthorizationResource:isSystemAuthorized BadPayloadException");
      throw new BadPayloadException("Bad payload: Missing/incomplete consumer, service" + " or providerList in the request payload.");
    }

    IntraCloudAuthResponse response = new IntraCloudAuthResponse();
    restrictionMap.put("systemGroup", request.getConsumer().getSystemGroup());
    restrictionMap.put("systemName", request.getConsumer().getSystemName());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.info("Consumer is not in the database. " + "(AuthorizationResource:isSystemAuthorized DataNotFoundException)");
      throw new DataNotFoundException("Consumer System is not in the authorization database. " + request.getConsumer().toString());
    }

    HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<>();
    log.info("authorizationState hashmap created");

    restrictionMap.clear();
    restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service is not in the database. Returning NOT AUTHORIZED state. " + request.getService().toString());
      for (ArrowheadSystem provider : request.getProviders()) {
        authorizationState.put(provider, false);
      }
      response.setAuthorizationMap(authorizationState);
      return Response.status(Status.OK).entity(response).build();
    }

    IntraCloudAuthorization authRight = new IntraCloudAuthorization();
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
      log.info("Authorization rights requested for System: " + request.getConsumer().toString());

      if (authRight == null) {
        authorizationState.put(provider, false);
        log.info("This (consumer/provider/service) request is NOT AUTHORIZED.");
      } else {
        authorizationState.put(provider, true);
        log.info("This (consumer/provider/service) request is AUTHORIZED.");
      }
    }

    log.info("Sending authorization response with " + authorizationState.size() + " entries.");
    response.setAuthorizationMap(authorizationState);
    return Response.status(Status.OK).entity(response).build();
  }

  /**
   * Checks whether an external Cloud can use a local Service.
   *
   * @return boolean
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("/intercloud")
  public Response isCloudAuthorized(InterCloudAuthRequest request) {
    log.info("Entered the AuthorizationResource:isCloudAuthorized function");

    if (!request.isPayloadUsable()) {
      log.info("AuthorizationResource:isCloudAuthorized BadPayloadException");
      throw new BadPayloadException("Bad payload: Missing/incomplete cloud or service in the request payload.");
    }

    restrictionMap.put("operator", request.getCloud().getOperator());
    restrictionMap.put("cloudName", request.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("Consumer is not in the database. " + "(AuthorizationResource:isCloudAuthorized DataNotFoundException)");
      throw new DataNotFoundException("Consumer Cloud is not in the authorization database. " + request.getCloud().toString());
    }

    boolean isAuthorized = false;
    restrictionMap.clear();
    restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service is not in the database. Returning NOT AUTHORIZED state." + request.getService().toString());
      return Response.status(Status.OK).entity(new InterCloudAuthResponse(isAuthorized)).build();
    }

    InterCloudAuthorization authRight = new InterCloudAuthorization();
    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    restrictionMap.put("service", service);
    authRight = dm.get(InterCloudAuthorization.class, restrictionMap);
    log.info("Authorization rights requested for Cloud: " + request.getCloud().toString());

    if (authRight != null) {
      log.info("This (cloud/service) request is AUTHORIZED.");
      isAuthorized = true;
    } else {
      log.info("This (cloud/service) request is NOT AUTHORIZED.");
    }

    return Response.status(Status.OK).entity(new InterCloudAuthResponse(isAuthorized)).build();
  }

}
