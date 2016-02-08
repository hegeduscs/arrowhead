package eu.arrowhead.common.model.orchestrator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueryResult {
	
	List<ProvidedInterface> Providers = new ArrayList<ProvidedInterface>();
	
	public QueryResult(){
		
	}

	public QueryResult(List<ProvidedInterface> providers) {
		Providers = providers;
	}

	public List<ProvidedInterface> getProviders() {
		return Providers;
	}

	public void setProviders(List<ProvidedInterface> providers) {
		Providers = providers;
	}
	
	public void addProvider(ProvidedInterface provider){
		Providers.add(provider);
	}
	
	
}
