package eu.arrowhead.common.exception;

public class DataNotFoundException extends RuntimeException{
	
	
	private static final long serialVersionUID = -1622261264080480479L;

	public DataNotFoundException(String message) {
		super(message);
	}
}
