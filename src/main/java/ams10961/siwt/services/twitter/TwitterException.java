package ams10961.siwt.services.twitter;

public class TwitterException extends Exception {

	private static final long serialVersionUID = 1L;

	public TwitterException() {
		super();
	}
	
	public TwitterException(String message) {
		super(message);
	}

	public TwitterException(Throwable cause) {
		super(cause);
	}

	public TwitterException(String message, Throwable cause) {
		super(message, cause);
	}

}
