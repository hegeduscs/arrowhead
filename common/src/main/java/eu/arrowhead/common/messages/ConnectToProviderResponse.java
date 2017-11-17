package eu.arrowhead.common.messages;

public class ConnectToProviderResponse {

	private String queueName;
	private String controlQueueName;
  private String payloadEncryption;

	public ConnectToProviderResponse() {
	}

  public ConnectToProviderResponse(String queueName, String controlQueueName, String payloadEncryption) {
    this.queueName = queueName;
		this.controlQueueName = controlQueueName;
    this.payloadEncryption = payloadEncryption;
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

  public String getPayloadEncryption() {
    return payloadEncryption;
  }

  public void setPayloadEncryption(String payloadEncryption) {
    this.payloadEncryption = payloadEncryption;
  }

}
