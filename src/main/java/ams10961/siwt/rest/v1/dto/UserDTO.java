package ams10961.siwt.rest.v1.dto;

import java.util.List;
import java.util.Vector;

import javax.validation.constraints.NotNull;
import javax.ws.rs.ApplicationPath;

import ams10961.siwt.entities.User;
import ams10961.siwt.rest.v1.UserRest;
import ams10961.siwt.util.TimeFormatting;

/*
 * Non-persisted DTO used primarily for REST requests and responses.
 */
public class UserDTO {
	
	@NotNull
	private char type;

	private String uuid;

	@NotNull
	private String handle;
	
	private String creationTime;

	private String href;


	// needed for jackson
	public UserDTO() {
		// dummy constructor 
	}

	public UserDTO(User user) {
		this.setUuid(user.getUuid());
		this.setType(user.getType());
		this.setHandle(user.getHandle());
		this.setCreationTime(TimeFormatting.iso8601(user.getCreationTime()));
		
		// HREF - relative rather than absolute
		StringBuilder hrefBuild = new StringBuilder();
		hrefBuild.append("/").append(ams10961.siwt.rest.v1.JaxRsActivator.class.getAnnotation(ApplicationPath.class).value().substring(1));
		hrefBuild.append("/").append(UserRest.USERS_RESTURL);
		hrefBuild.append("/").append(this.getUuid());		
		this.setHref(hrefBuild.toString());
	}

	// multi-constructor
	public static List<UserDTO> createMultiple(List<User> userList) {
		Vector<UserDTO> returnValues = new Vector<UserDTO>();
		for (User user : userList) {
			returnValues.add(new UserDTO(user));
		}
		return returnValues;
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[UserDTO");
		out.append("|").append(this.getType());
		out.append("|").append(this.getHandle());
		out.append("|").append(this.getCreationTime());
		out.append("|").append(this.getHref());
		out.append("]");
		return out.toString();
	}
	

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}
	
	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
