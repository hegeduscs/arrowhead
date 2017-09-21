package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadSystem;

public class ICNResult {

	private int serverSocketPort;
	private String authorizationToken;
	private ArrowheadSystem provider;

	public ICNResult(int serverSocketPort, String authorizationToken, ArrowheadSystem provider) {
		this.serverSocketPort = serverSocketPort;
		this.authorizationToken = authorizationToken;
		this.provider = provider;
	}

	public int getServerSocketPort() {
		return serverSocketPort;
	}

	public void setServerSocketPort(int serverSocketPort) {
		this.serverSocketPort = serverSocketPort;
	}

	public String getAuthorizationToken() {
		return authorizationToken;
	}

	public void setAuthorizationToken(String authorizationToken) {
		this.authorizationToken = authorizationToken;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		this.provider = provider;
	}

}
