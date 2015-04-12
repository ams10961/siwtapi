package ams10961.siwt.rest.v1.dto;

import javax.ws.rs.ApplicationPath;

import ams10961.siwt.entities.Authentication;
import ams10961.siwt.entities.Session;
import ams10961.siwt.rest.v1.JaxRsActivator;
import ams10961.siwt.rest.v1.SessionRest;
import ams10961.siwt.util.TimeFormatting;

/*
 * Non-persisted DTO used primarily for REST requests and responses.
 * 
 */
public class SessionDTO {

	private String uuid;
	
	// should be passed in authorization header
	private String authentication;
	
	// returned
	private String loginTime;
	
	// referenced user
	private UserDTO user;
	
	// relative reference 
	private String href;
	
	// needed for jackson
	public SessionDTO() {
		// nothing for now
	}

	public SessionDTO(Session session, Authentication authentication) {
		// uuid
		this.setUuid(session.getUuid());
		// authentication
		this.setAuthentication(authentication.getUuid());
		// text version of login time
		this.setLoginTime(TimeFormatting.iso8601(session.getCreationTime()));
		// referenced user
		if (session.getUser()!=null) {
			this.setUser(new UserDTO(session.getUser()));
		} else {
			this.setUser(null);
		}

		// HREF - relative to application rather than absolute
		StringBuilder hrefBuild = new StringBuilder();
		hrefBuild.append("/").append(JaxRsActivator.class.getAnnotation(ApplicationPath.class).value().substring(1));
		hrefBuild.append("/").append(SessionRest.SESSIONS_RESTURL);
		hrefBuild.append("/").append(this.getUuid());		
		this.setHref(hrefBuild.toString());
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[SessionDTO");
		out.append("|").append(this.getAuthentication());
		out.append("|").append(this.getUser()!=null ? this.getUser().toString() : "-");
		out.append("|").append(this.getLoginTime());
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

	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}
	
	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}
	
}

