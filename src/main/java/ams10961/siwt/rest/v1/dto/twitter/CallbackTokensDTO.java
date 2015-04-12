package ams10961.siwt.rest.v1.dto.twitter;

import javax.validation.constraints.NotNull;


/*
 * Non-persisted DTO used primarily for REST requests and responses.
 */
public class CallbackTokensDTO {
	
	@NotNull
	// TODO: add pattern constraint
	private String token;
	
	@NotNull
	// TODO: add pattern constraint
	private String verifier;
	
	private long sessionTimeout;

	public CallbackTokensDTO() {
		// do nothing
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[TwitterCallbackDTO");
		out.append("|").append(getToken());
		out.append("|").append(getVerifier());
		out.append("|").append(getSessionTimeout());
		out.append("]");
		return out.toString();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getVerifier() {
		return verifier;
	}

	public void setVerifier(String verifier) {
		this.verifier = verifier;
	}

	public long getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(long sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}



}
