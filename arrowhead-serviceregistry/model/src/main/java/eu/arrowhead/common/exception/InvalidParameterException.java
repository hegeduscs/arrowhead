package eu.arrowhead.common.exception;

public class InvalidParameterException extends RuntimeException{
		
	private static final long serialVersionUID = -2803416860045403235L;

	public InvalidParameterException(String message) {
		super(message);
	}
}
