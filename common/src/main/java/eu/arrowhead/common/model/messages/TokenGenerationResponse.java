package eu.arrowhead.common.model.messages;

import java.util.List;

public class TokenGenerationResponse {

	private List<String> token;
	private List<String> signature;

	public TokenGenerationResponse() {
	}

	public List<String> getToken() {
		return token;
	}

	public List<String> getSignature() {
		return signature;
	}

	public void setToken(List<String> token) {
		this.token = token;
	}

	public void setSignature(List<String> signature) {
		this.signature = signature;
	}
}