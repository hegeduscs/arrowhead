package eu.arrowhead.common.messages;

public class GatewayAtConsumerResponse {

	private int serverSocketPort;

	public GatewayAtConsumerResponse(int serverSocketPort) {
		this.serverSocketPort = serverSocketPort;
	}

	public int getServerSocketPort() {
		return serverSocketPort;
	}

	public void setServerSocketPort(int serverSocketPort) {
		this.serverSocketPort = serverSocketPort;
	}

}
