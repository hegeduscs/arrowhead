package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceQueryForm {

	String ServiceMetaData;
	List<String> ServiceInterfaces = new ArrayList<String>();
	boolean PingProviders;
	String TSIG_key;

	public ServiceQueryForm() {

	}

	public ServiceQueryForm(String serviceMetaData, List<String> serviceInterfaces, boolean pingProviders,
			String tSIG_key) {
		ServiceMetaData = serviceMetaData;
		ServiceInterfaces = serviceInterfaces;
		PingProviders = pingProviders;
		TSIG_key = tSIG_key;
	}

	public String getServiceMetaData() {
		return ServiceMetaData;
	}

	public void setServiceMetaData(String serviceMetaData) {
		ServiceMetaData = serviceMetaData;
	}

	public boolean isPingProviders() {
		return PingProviders;
	}

	public void setPingProviders(boolean pingProviders) {
		PingProviders = pingProviders;
	}

	public String getTSIG_key() {
		return TSIG_key;
	}

	public void setTSIG_key(String tSIG_key) {
		TSIG_key = tSIG_key;
	}

	public List<String> getServiceInterfaces() {
		return ServiceInterfaces;
	}

	public void setServiceInterfaces(List<String> serviceInterfaces) {
		ServiceInterfaces = serviceInterfaces;
	}

}
