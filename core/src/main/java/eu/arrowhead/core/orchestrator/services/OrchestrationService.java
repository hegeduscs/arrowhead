package eu.arrowhead.core.orchestrator.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.QueryResult;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

public class OrchestrationService {
	
	private ServiceRequestForm example;
	private static ServiceRequestForm currentEntry;
	private static QueryResult currentQuery;
	private static String SR = "http://localhost:8080/core/orchestration/SR";
	Client client = ClientBuilder.newClient();

	
	public OrchestrationService(){
	}
	
	public ServiceRequestForm getExample(){
		List<String> interfaces = new ArrayList<String>();
		ArrowheadSystem requesterSystem = new ArrowheadSystem("A", "Kiscica", "192.168.1.1", "8080", "authenticated");
		interfaces.add("A interface");
		interfaces.add("B interface");
		example = new ServiceRequestForm(new ArrowheadService("ServiceGroup", "serviceDefinition", interfaces, "metaData"),"requestedQoS", requesterSystem, 5);
		System.out.println(example.getOrchestrationFlags().toString());
		return example;
	}
	
	public ServiceRequestForm setCurrentForm(ServiceRequestForm srf){
		currentEntry = srf;
		return currentEntry;
	}
	
	public ServiceRequestForm getCurrentForm(){
		return currentEntry;
	}
	
	public QueryResult sendSQF(){
		ServiceQueryForm example = createSQF();
		Response queryResponse = client.target(SR).request().put(Entity.json(example));
		currentQuery = queryResponse.readEntity(QueryResult.class);
		return currentQuery;
	}
	
	public ServiceQueryForm createSQF(){
		ServiceQueryForm example = new ServiceQueryForm();
		example.setPingProviders(false);
		example.setServiceMetaData("MetadataProba");
		example.setTSIG_key("Key");
		List<String> interfaces = new ArrayList<String>();
		interfaces.add("Cica");
		interfaces.add("Kutya");
		example.setServiceInterfaces(interfaces);
		return example;
	}
	
	

}
