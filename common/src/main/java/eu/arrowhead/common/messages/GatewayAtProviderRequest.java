package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;

public class GatewayAtProviderRequest {

	private String brokerName;
	private Integer brokerPort;
	private ArrowheadSystem provider;
	private boolean isSecure;

	public GatewayAtProviderRequest(String brokerName, Integer brokerPort, ArrowheadSystem provider, boolean isSecure) {
		this.brokerName = brokerName;
		this.brokerPort = brokerPort;
		this.provider = provider;
		this.isSecure = isSecure;
	}

	public String getBrokerName() {
		return brokerName;
	}

	public void setBrokerName(String brokerName) {
		this.brokerName = brokerName;
	}

	public Integer getBrokerPort() {
		return brokerPort;
	}

	public void setBrokerPort(Integer brokerPort) {
		this.brokerPort = brokerPort;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		this.provider = provider;
	}

	public boolean getIsSecure() {
		return isSecure;
	}

	public void setIsSecure(Boolean isSecure) {
		this.isSecure = isSecure;
	}

}
