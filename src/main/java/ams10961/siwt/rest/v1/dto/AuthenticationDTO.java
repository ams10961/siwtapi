package ams10961.siwt.rest.v1.dto;

import javax.ws.rs.ApplicationPath;

import ams10961.siwt.entities.Authentication;
import ams10961.siwt.rest.v1.AuthenticationRest;
import ams10961.siwt.rest.v1.JaxRsActivator;
import ams10961.siwt.util.TimeFormatting;

/*
 * Non-persisted DTO used primarily for REST requests and responses.
 * 
 */
public class AuthenticationDTO {

	private String uuid;
	private String status;
	private String ipAddress;
	private String creationTime;
	private String lastValidatedTime;
	private long timeout;
	
	
	// relative reference 
	private String href;
	
	// needed for jackson
	public AuthenticationDTO() {
		// nothing for now
	}

	public AuthenticationDTO(Authentication authentication) {

		// authentication
		this.setUuid(authentication.getUuid());
		
		// text version of times
		this.setCreationTime(TimeFormatting.iso8601(authentication.getCreationTime()));
		this.setLastValidatedTime(TimeFormatting.iso8601(authentication.getLastValidatedTime()));
		this.setStatus(authentication.getStatusString());
		this.setIpAddress(authentication.getIpAddress());

		// HREF - relative to application rather than absolute
		StringBuilder hrefBuild = new StringBuilder();
		hrefBuild.append("/").append(JaxRsActivator.class.getAnnotation(ApplicationPath.class).value().substring(1));
		hrefBuild.append("/").append(AuthenticationRest.AUTHENTICATIONS_RESTURL);
		hrefBuild.append("/").append(this.getUuid());		

		this.setHref(hrefBuild.toString());
	}
	
	/*
	 * logging, for example, during testing 
	 */
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[AuthenticationDTO");
		out.append("|").append(this.getStatus());
		out.append("|").append(this.getIpAddress());		
		out.append("|").append(this.getCreationTime());
		out.append("|").append(this.getLastValidatedTime());
		out.append("|").append(this.getHref());
		out.append("]");
		return out.toString();
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public String getLastValidatedTime() {
		return lastValidatedTime;
	}

	public void setLastValidatedTime(String lastValidationTime) {
		this.lastValidatedTime = lastValidationTime;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	
}

