package eu.arrowhead.common.messages;

public class GatewayAtProviderResponse {

	private String queueName;
	private String payloadEncrytion;

	public GatewayAtProviderResponse(String queueName, String payloadEncrytion) {
		this.queueName = queueName;
		this.payloadEncrytion = payloadEncrytion;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getPayloadEncrytion() {
		return payloadEncrytion;
	}

	public void setPayloadEncrytion(String payloadEncrytion) {
		this.payloadEncrytion = payloadEncrytion;
	}

}
