package eu.arrowhead.common.messages;

public class ConnectToProviderResponse {

	private String queueName;
	private String controlQueueName;
	private String payloadEncrytion;

	public ConnectToProviderResponse(String queueName, String controlQueueName, String payloadEncrytion) {
		this.queueName = queueName;
		this.controlQueueName = controlQueueName;
		this.payloadEncrytion = payloadEncrytion;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getControlQueueName() {
		return controlQueueName;
	}

	public void setControlQueueName(String controlQueueName) {
		this.controlQueueName = controlQueueName;
	}

	public String getPayloadEncrytion() {
		return payloadEncrytion;
	}

	public void setPayloadEncrytion(String payloadEncrytion) {
		this.payloadEncrytion = payloadEncrytion;
	}

}
