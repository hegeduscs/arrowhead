package eu.arrowhead.core.orchestrator.services;

import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.QueryResult;
import eu.arrowhead.common.model.messages.ServiceQueryForm;

public class DummySRImitator {
	
	ServiceQueryForm serviceQueryForm = new ServiceQueryForm();
	ServiceQueryForm example = new ServiceQueryForm();
	
	public DummySRImitator(){
		
	}
	
	public ServiceQueryForm getExample(){
		example.setPingProviders(false);
		example.setServiceMetaData("MetadataProba");
		example.setTSIG_key("Key");
		List<String> interfaces = new ArrayList<String>();
		interfaces.add("Cica");
		interfaces.add("Kutya");
		example.setServiceInterfaces(interfaces);
		return example;
	}
	
	
	public QueryResult getResult(ServiceQueryForm sqf){
		serviceQueryForm = sqf;
		QueryResult queryResult = new QueryResult();
		ProvidedService pi1 = new ProvidedService(new ArrowheadSystem("Aitia", "A", "192.168.1.1", "8080", "authenticated"),
				"serviceURI", "interface");
		ProvidedService pi2 = new ProvidedService(new ArrowheadSystem("Aitia", "B", "192.168.1.1", "8081", "authenticated"),
				"serviceURI", "interface");
		queryResult.addProvider(pi1);
		queryResult.addProvider(pi2);
		return queryResult;
	}

}
