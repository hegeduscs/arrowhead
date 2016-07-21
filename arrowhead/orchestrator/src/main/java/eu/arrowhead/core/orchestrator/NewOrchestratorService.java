package eu.arrowhead.core.orchestrator;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;

public final class NewOrchestratorService {
	
	private static Logger log = Logger.getLogger(NewOrchestratorService.class.getName());

	public static Response externalServiceRequest(){
		queryServiceRegistry(null, false, false);
		return null;
	}
	
	private static Response queryServiceRegistry(ArrowheadService service,
			boolean metadataSearch, boolean pingProviders){
		log.info("Entered the queryServiceRegistry method.");
		
		//Compiling the URI and the request payload
		String srURI = SysConfig.getServiceRegistryURI();
		srURI = UriBuilder.fromPath(srURI).path(service.getServiceGroup())
				.path(service.getServiceDefinition()).toString();
		String tsig_key = SysConfig.getCoreSystem("serviceregistry").getAuthenticationInfo();
		ServiceQueryForm queryForm = new ServiceQueryForm(service.getServiceMetadata(), 
				service.getInterfaces(), pingProviders, metadataSearch, tsig_key);
		
		//Sending back the query result
		Response srResponse = Utility.sendRequest(srURI, "PUT", queryForm);
		log.info("ServiceRegistry queried for requested Service: " + service.getServiceDefinition());
		ServiceQueryResult result = srResponse.readEntity(ServiceQueryResult.class);
		if(result.isPayloadEmpty()){
			log.info("ServiceRegistry query came back empty.");
			return Response.noContent().entity(null).build();
		}
		
		log.info("ServiceRegistry query successful. Number of providers: " 
				+ result.getServiceQueryData().size());
		return Response.ok().entity(result).build();
	}
}
