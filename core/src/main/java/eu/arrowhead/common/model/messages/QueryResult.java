package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueryResult {
	
	List<ProvidedService> Providers = new ArrayList<ProvidedService>();
	
	public QueryResult(){
		
	}

	public QueryResult(List<ProvidedService> providers) {
		Providers = providers;
	}

	public List<ProvidedService> getProviders() {
		return Providers;
	}

	public void setProviders(List<ProvidedService> providers) {
		Providers = providers;
	}
	
	public void addProvider(ProvidedService provider){
		Providers.add(provider);
	}
	
	
}
