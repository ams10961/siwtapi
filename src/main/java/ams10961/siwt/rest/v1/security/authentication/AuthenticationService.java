package ams10961.siwt.rest.v1.security.authentication;

import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import ams10961.siwt.entities.Authentication;
import ams10961.siwt.entities.Session;
import ams10961.siwt.entities.User;
import ams10961.siwt.entities.persistence.AuthenticationPersistence;
import ams10961.siwt.entities.persistence.SessionPersistence;
import ams10961.siwt.entities.persistence.UserPersistence;

@Provider
public class AuthenticationService {

	@Context
	HttpServletRequest request;
	
	@EJB
	private AuthenticationPersistence authenticationPersistence;

	@EJB
	private SessionPersistence sessionPersistence;

	@EJB
	private UserPersistence userPersistence;

	@Inject
	private transient Logger logger;
	
	/*
	 * returns the DTO object which has been added to the http request
	 */
	public Authentication getAuthentication() {
		return (Authentication) request.getAttribute(AuthenticationFilter.AUTHENTICATION_DATA);
	}

	/*
	 * refresh last validated of authentication object in volatile database 
	 */
	public void revalidateAuthentication() {

		Authentication authenticationDTO = getAuthentication();
		Date timeNow = new Date(System.currentTimeMillis());

		Authentication authentication = authenticationPersistence.findById(authenticationDTO.getId());
		authentication.setLastValidatedTime(timeNow);
		authenticationPersistence.save(authentication);
		
		// logger.info("revalidated authentication {}",authentication);
	}

	/*
	 * Convenience method used principally when creating objects
	 */
	public Session getSession() {

		// add the creator
		Session session = sessionPersistence.findById(getAuthentication().getSessionId());
		if (logger.isDebugEnabled()) {
			logger.debug("session {}", session.toString());
		}

		return session;
	}

	/*
	 * Convenience method used principally when creating objects
	 */
	public User getRequester() {

		// add the creator
		User user = userPersistence.findById(getAuthentication().getUserId());
		if (logger.isDebugEnabled()) {
			logger.debug("requester {}", user.toString());
		}

		return user;
	}

}
