package eu.arrowhead.common.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceMetadata {

	private String key;
	private String value;

	public ServiceMetadata() {
		
	}

	public ServiceMetadata(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
