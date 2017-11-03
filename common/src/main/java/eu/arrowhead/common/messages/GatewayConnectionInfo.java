package eu.arrowhead.common.messages;

import java.security.PublicKey;

public class GatewayConnectionInfo {

	private String brokerName;
	private int brokerPort;
	private String queueName;
	private String controlQueueName;
	private PublicKey gatewayPublicKey;

	public GatewayConnectionInfo(String brokerName, int brokerPort, String queueName, String controlQueueName,
			PublicKey gatewayPublicKey) {
		this.brokerName = brokerName;
		this.brokerPort = brokerPort;
		this.queueName = queueName;
		this.controlQueueName = controlQueueName;
		this.gatewayPublicKey = gatewayPublicKey;
	}

	public String getBrokerName() {
		return brokerName;
	}

	public void setBrokerName(String brokerName) {
		this.brokerName = brokerName;
	}

	public int getBrokerPort() {
		return brokerPort;
	}

	public void setBrokerPort(int brokerPort) {
		this.brokerPort = brokerPort;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public void setControlQueueName(String controlQueueName) {
		this.controlQueueName = controlQueueName;
	}

	public String getControlQueueName() {
		return controlQueueName;
	}

	public PublicKey getGatewayPublicKey() {
		return gatewayPublicKey;
	}

	public void setGatewayPublicKey(PublicKey gatewayPublicKey) {
		this.gatewayPublicKey = gatewayPublicKey;
	}

}
