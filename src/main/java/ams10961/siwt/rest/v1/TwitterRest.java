package ams10961.siwt.rest.v1;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import ams10961.siwt.rest.v1.dto.twitter.RedirectDTO;
import ams10961.siwt.rest.v1.dto.twitter.ValidatedUserDTO;
import ams10961.siwt.services.twitter.TwitterException;
import ams10961.siwt.services.twitter.TwitterService;

@RequestScoped
@Path("/twitter")
public class TwitterRest {

	public final static String TWITTER_RESTURL = "twitter";

	@Inject
	private transient Logger logger;

	@Inject
	private TwitterService twitterService;
	
	/*
	 * REDIRECT URL
	 */
	@GET
	@Path("/redirect")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRedirectUrl() {

		try {
			String authenticationUrl = null;
			Map<String, String> requestTokenValues = null;

			// get request token
			requestTokenValues = (Map<String, String>) twitterService.getRequestToken();

			// generate authentication redirection URL
			authenticationUrl = twitterService.getAuthorisationURL(requestTokenValues);

			// return results object
			RedirectDTO redirectDTO = new RedirectDTO();
			redirectDTO.setRedirectURL(authenticationUrl);
			
			return Response.ok().entity(redirectDTO).build();

		} catch (TwitterException e) {
			logger.error("Get Request Token Exception", e);
			Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("error", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseObj).build();
		} catch (Exception e) {
			logger.error("runtime exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/*
	 * GET USER DETAILS
	 */
	@GET
	@Path("/users/show/{screenName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response usersShow(@PathParam("screenName") String screenName) {

		try {
			logger.info("twitter users/show for:{}", screenName);

			ValidatedUserDTO twitterUserDTO = new ValidatedUserDTO();
			twitterUserDTO.setScreenName(screenName);

			// return 3XX if not found
			JSONObject result = twitterService.userShow(screenName);
			if (result == null) {
				Map<String, String> responseObj = new HashMap<String, String>();
				responseObj.put("error", "user not found");
				return Response.status(Response.Status.SEE_OTHER).entity(responseObj).build();
			}

			// otherwise extract details, and return
			try {
				twitterUserDTO.setDescription(result.getString("description"));
			} catch (JSONException e) {
				logger.warn("no description availabe for twitter user:{}", result.toString());
			}
			return Response.ok(twitterUserDTO).build();

		} catch (TwitterException e) {
			logger.error("Get Request Token Exception", e);
			Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("error", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseObj).build();
		} catch (Exception e) {
			logger.error("runtime exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}