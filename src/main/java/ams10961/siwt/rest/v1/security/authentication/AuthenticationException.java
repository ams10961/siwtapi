package ams10961.siwt.rest.v1.security.authentication;

public class AuthenticationException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum AuthenticationExceptionType {
	    NONEXISTENT, EXPIRED, SUSPCIOUS, UNKNOWN
	}

	private AuthenticationExceptionType type = AuthenticationExceptionType.UNKNOWN;

	public AuthenticationException() {
		super();
	}
	
	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthenticationExceptionType getType() {
		return type;
	}

	public void setType(AuthenticationExceptionType type) {
		this.type = type;
	}
}
