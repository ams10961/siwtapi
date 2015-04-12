package ams10961.siwt.entities.persistence;

public class SessionException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum SessionExceptionType {
	    NONEXISTENT, EXPIRED, SUSPCIOUS, CLOSED, ABANDONED, OTHER
	}

	private SessionExceptionType type = SessionExceptionType.OTHER;

	public SessionException() {
		super();
	}

	public SessionException(String message) {
		super(message);
	}

	public SessionException(Throwable cause) {
		super(cause);
	}

	public SessionException(String message, Throwable cause) {
		super(message, cause);
	}

	public SessionExceptionType getType() {
		return type;
	}

	public void setType(SessionExceptionType type) {
		this.type = type;
	}
}
