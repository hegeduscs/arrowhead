package eu.arrowhead.common.exception;

public class AuthorizationException extends RuntimeException{

	private static final long serialVersionUID = -6404483587525575152L;

	public AuthorizationException(String message) {
		super(message);
	}
}
