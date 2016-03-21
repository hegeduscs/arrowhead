package eu.arrowhead.common.exception;

public class BadPayloadException extends RuntimeException{
	

	private static final long serialVersionUID = -1835158132522682880L;

	public BadPayloadException(String message) {
		super(message);
	}
}
