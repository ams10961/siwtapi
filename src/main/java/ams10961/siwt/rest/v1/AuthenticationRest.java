package ams10961.siwt.rest.v1;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import ams10961.siwt.Constants;
import ams10961.siwt.entities.Authentication;
import ams10961.siwt.entities.persistence.AuthenticationPersistence;
import ams10961.siwt.entities.persistence.PersistenceException;
import ams10961.siwt.rest.v1.dto.AuthenticationDTO;
import ams10961.siwt.rest.v1.security.OriginFilter;
import ams10961.siwt.rest.v1.security.authentication.AuthenticationService;

/*
 * needs to be a stateless EJB to ensure a transaction for the lazily loaded collections
 */
@Stateless
@Path("/authentications")
public class AuthenticationRest {

	public final static String AUTHENTICATIONS_RESTURL = "authentications";

	@Inject
	private transient Logger logger;
	
	@EJB
	private AuthenticationPersistence authenticationPersistence;
	
	@Inject
	AuthenticationService authenticationService;
	
	/*
	 * CREATE AN AUTHENTICATION TOKEN FOR REQUESTS
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll 
	public Response create(@Context HttpServletRequest req) {

		try {
			String ipAddress = (String)req.getAttribute(OriginFilter.ORIGIN_IP);

			// limit the number of simultaneous sessions
			int sessions = authenticationPersistence.countAll();
			if (sessions >= Constants.MAX_SESSIONS) {
				// response
				return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();			
			}
			
			// TODO: perform any additional checks here, for example, IP address filtering
			
			// create new authentication object for the session in volatile persistence
			Authentication authentication = authenticationPersistence.create(ipAddress);

			// create a DTO from it for the REST response
			AuthenticationDTO authenticationDTO = new AuthenticationDTO(authentication);
			
			// response
			return Response.created(new URI(authenticationDTO.getHref())).entity(authenticationDTO).build();

		} catch (URISyntaxException e) {
			logger.error("created resource URI exception");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (PersistenceException e) {
			logger.error("create resource persistence exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("runtime exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

}
