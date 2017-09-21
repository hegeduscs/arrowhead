package eu.arrowhead.common.messages;

public class GatewayConnInfo {

	private String brokerName;
	private int brokerPort;
	private String queueName;
	private String payloadEncryption;
	private String authorizationToken;

	public GatewayConnInfo(String brokerName, int brokerPort, String queueName, String payloadEncryption, String authorizationToken) {
		this.brokerName = brokerName;
		this.brokerPort = brokerPort;
		this.queueName = queueName;
		this.payloadEncryption = payloadEncryption;
		this.authorizationToken = authorizationToken;
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

	public String getPayloadEncryption() {
		return payloadEncryption;
	}

	public void setPayloadEncryption(String payloadEncryption) {
		this.payloadEncryption = payloadEncryption;
	}

	public String getAuthorizationToken() {
		return authorizationToken;
	}

	public void setAuthorizationToken(String tokauthorizationTokenen) {
		this.authorizationToken = authorizationToken;
	}

}
