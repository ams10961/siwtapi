package ams10961.siwt.rest.v1;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import ams10961.siwt.Constants;
import ams10961.siwt.entities.Authentication;
import ams10961.siwt.entities.Session;
import ams10961.siwt.entities.Session.SessionStatus;
import ams10961.siwt.entities.User;
import ams10961.siwt.entities.persistence.AuthenticationPersistence;
import ams10961.siwt.entities.persistence.PersistenceException;
import ams10961.siwt.entities.persistence.SessionPersistence;
import ams10961.siwt.entities.persistence.UserPersistence;
import ams10961.siwt.rest.v1.dto.SessionDTO;
import ams10961.siwt.rest.v1.dto.twitter.CallbackTokensDTO;
import ams10961.siwt.rest.v1.security.OriginFilter;
import ams10961.siwt.rest.v1.security.authentication.AuthenticationService;
import ams10961.siwt.services.twitter.TwitterException;
import ams10961.siwt.services.twitter.TwitterService;

/*
 * needs to be a stateless EJB to ensure a transaction for the lazily loaded collections
 */
@Stateless
@Path("/sessions")
public class SessionRest {

	public final static String SESSIONS_RESTURL = "sessions";

	@Inject
	private transient Logger logger;

	@Inject
	private TwitterService twitterService;

	@EJB
	private SessionPersistence sessionPersistence;
	
	@EJB
	private AuthenticationPersistence authenticationPersistence;
	
	@EJB
	private UserPersistence userPersistence;

	@Inject
	private Validator validator;
	
	@Inject
	AuthenticationService authenticationService;
	
	/*
	 * CREATE SESSION BASED ON TWITTER AUTHENTICATION
	 */
	@POST
	@Path("/twitter/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createTwitterSession(CallbackTokensDTO callbackTokensDTO, @Context HttpServletRequest req) {

		try {
			// check callback token object DTO using bean validation
			Set<ConstraintViolation<CallbackTokensDTO>> violations = validator.validate(callbackTokensDTO);
			if (!violations.isEmpty()) {
				Map<String, String> responseObj = new HashMap<String, String>();
				for (ConstraintViolation<?> violation : violations) {
					responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
				}
				return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
			}
			
			// create a new session object
			Session twitterSession = new Session();
			
			// get the IP address
			String ipAddress = (String)req.getAttribute(OriginFilter.ORIGIN_IP);
			twitterSession.setIpAddress(ipAddress);
		
			// if testing, the token is the user name
			if (Constants.TESTING_TWITTER_CALLBACK_VERIFIER.equalsIgnoreCase(callbackTokensDTO.getVerifier())) {
				// double check development/testing values are from and to local host
				String host = req.getHeader(HttpHeaders.HOST);
				String origin = req.getHeader(OriginFilter.ORIGIN_HEADER);
				if (host.startsWith(Constants.LOCALHOST_IP) && origin.startsWith(Constants.LOCALHOST_IP)) {
					twitterSession.setExternalHandle(callbackTokensDTO.getToken());
					// optional additional timeout parameter for testing
					twitterSession.setInactivityTimeout(callbackTokensDTO.getSessionTimeout());
				} else {
					logger.error("rejecting attempt to use testing credentials in non-local environment from >{}<",ipAddress);
					return Response.status(Response.Status.UNAUTHORIZED).build();
				}
			} else {
				// call twitter API
				HashMap<String, String> accessTokenResult = twitterService.getAccessToken(callbackTokensDTO.getToken(),
						callbackTokensDTO.getVerifier());
				twitterSession.setExternalHandle(accessTokenResult.get(TwitterService.TWITTER_SCREEN_NAME));
				twitterSession.setExternalId(accessTokenResult.get(TwitterService.TWITTER_USER_ID));
				/* set standard timeout */
				twitterSession.setInactivityTimeout(Constants.INACTIVITY_EXPIRY_MS);
			}

			// user already exists? if not, then create
			User searchUser = userPersistence.findByHandle(twitterSession.getExternalHandle());
			if (searchUser != null) {
				logger.info("Found existing user:{}", searchUser.toString());
				// add the existing user to the new session
				twitterSession.setUser(searchUser);
			} else {
				User createdUser = new User();
				createdUser.setType(User.TYPE_TWITTER);
				createdUser.setHandle(twitterSession.getExternalHandle());
				// create and persist the user (will receive an ID)
				createdUser = userPersistence.create(createdUser);
				// add it to the new session
				twitterSession.setUser(createdUser);
				logger.info("Created new user: {}",createdUser.toString());
			}
			
			// delete the old authentication
			Authentication guestAuthentication = authenticationPersistence.findById(authenticationService.getAuthentication().getId());
			authenticationPersistence.delete(guestAuthentication);
			
			// close any existing session objects for this user, remove authentication objects
			List<Session> existingSessions = sessionPersistence.findByExternalHandle(twitterSession.getExternalHandle());
			for (Session existingSession : existingSessions) {
				// close existing session
				existingSession.setStatus(SessionStatus.SUPERSEDED);
				Date now = new Date(System.currentTimeMillis());
				existingSession.setClosureTime(now);
				sessionPersistence.save(existingSession);
				// remove any related authentication objects
				authenticationPersistence.deleteAuthenticationObjects(existingSession);
			}

			// persist new session, generate UUID, generate authentication record
			twitterSession = sessionPersistence.create(twitterSession);
			// logger.info("created session {}",twitterSession);
			
			// create new authentication object for the session in volatile persistence
			Authentication authentication = authenticationPersistence.create(twitterSession);
			// logger.info("created authentication {}",authentication);

			// create a session DTO for the rest service reply
			SessionDTO createdSessionDTO = new SessionDTO(twitterSession, authentication);

			// return newly created session object
			return Response.created(new URI(createdSessionDTO.getHref())).entity(createdSessionDTO).build();

		} catch (TwitterException e) {
			logger.error("twitter exception");
			return Response.status(Response.Status.BAD_REQUEST).build();
		} catch (URISyntaxException e) {
			logger.error("created resource URI exception");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (PersistenceException e) {
			logger.error("session persistence exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("unexpected runtime exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	/*
	 * GET SESSION, e.g. to validate on page reload
	 */
	@GET
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSession(@PathParam("uuid") String uuid) {
	
		try {
			Session session = sessionPersistence.findByUuid(uuid);
	
			if (session!=null) {
				User requester = authenticationService.getRequester();
				if (requester.isAdmin() || session.isCreator(requester)) {
					SessionDTO sessionDTO = new SessionDTO(session, authenticationService.getAuthentication());
					
					// if successful, refresh authentication
					authenticationService.revalidateAuthentication();
					
					return Response.ok(new URI(sessionDTO.getHref())).entity(sessionDTO).build();
				} else {
					return Response.status(Response.Status.FORBIDDEN).entity("neither resource owner nor admin").build();
				}	
			} else {
				logger.error("session not found with uuid >{}<",uuid);
				return Response.status(Status.NOT_FOUND).entity("session not found").build();
			}

		} catch (URISyntaxException e) {
			logger.error("URI syntax exception");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		} catch (Exception e) {
			logger.error("unexpected runtime exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/*
	 * DELETE SESSION
	 */
	@DELETE
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteSession(@PathParam("uuid") String uuid) throws PersistenceException {

		try {
			Session session = sessionPersistence.findByUuid(uuid);
			if (session!=null) {
				// check whether request user authorised to perform this operation
				User requester = authenticationService.getRequester();
				if (requester.isAdmin() || session.isCreator(requester)) {
					// mark session as closed
					sessionPersistence.close(session);
					
					// remove any associated authentication sessions
					authenticationPersistence.deleteAuthenticationObjects(session);

					// don't refresh authentication
					return Response.status(Response.Status.ACCEPTED).build();
				} else {
					return Response.status(Response.Status.FORBIDDEN).entity("neither resource owner nor admin").build();
				}	
			} else {
				logger.error("session uuid not found >{}<",uuid);
				return Response.status(Status.NOT_FOUND).entity("session not found").build();
			}
		} catch (Exception e) {
			logger.error("unexpected runtime exception, session uuid >{}< {}", uuid, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
