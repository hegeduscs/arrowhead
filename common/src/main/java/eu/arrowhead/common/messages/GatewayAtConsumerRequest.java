package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;

public class GatewayAtConsumerRequest {

	private String brokerName;
	private int brokerPort;
	private String queueName;
	private ArrowheadSystem consumer;
	private Boolean isSecure;
	private String payloadEcryption;

	public GatewayAtConsumerRequest(String brokerName, Integer brokerPort, String queueName, ArrowheadSystem consumer,
			Boolean isSecure, String payloadEcryption) {
		this.brokerName = brokerName;
		this.brokerPort = brokerPort;
		this.queueName = queueName;
		this.consumer = consumer;
		this.isSecure = isSecure;
		this.payloadEcryption = payloadEcryption;
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

	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
	}

	public Boolean getIsSecure() {
		return isSecure;
	}

	public void setIsSecure(Boolean isSecure) {
		this.isSecure = isSecure;
	}

	public String getPayloadEcryption() {
		return payloadEcryption;
	}

	public void setPayloadEcryption(String payloadEcryption) {
		this.payloadEcryption = payloadEcryption;
	}

}