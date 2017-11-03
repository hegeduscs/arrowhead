package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;

public class ConnectToProviderRequest {

	private String brokerName;
	private Integer brokerPort;
	private ArrowheadSystem provider;
	private boolean isSecure;
	private int timeout;

	public ConnectToProviderRequest(String brokerName, Integer brokerPort, ArrowheadSystem provider, boolean isSecure,
			int timeout) {
		this.brokerName = brokerName;
		this.brokerPort = brokerPort;
		this.provider = provider;
		this.isSecure = isSecure;
		this.timeout = timeout;
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	

}
