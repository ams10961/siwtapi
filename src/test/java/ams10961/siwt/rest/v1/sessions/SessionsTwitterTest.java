package ams10961.siwt.rest.v1.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpHeaders;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ams10961.siwt.Constants;
import ams10961.siwt.rest.v1.AuthenticationRest;
import ams10961.siwt.rest.v1.RestTestBase;
import ams10961.siwt.rest.v1.SessionRest;
import ams10961.siwt.rest.v1.TestConstants;
import ams10961.siwt.rest.v1.dto.AuthenticationDTO;
import ams10961.siwt.rest.v1.dto.SessionDTO;
import ams10961.siwt.rest.v1.dto.twitter.CallbackTokensDTO;

@RunWith(Arquillian.class)
public class SessionsTwitterTest extends RestTestBase {
	
	final static Logger logger = LoggerFactory.getLogger(SessionsTwitterTest.class);

	private static AuthenticationDTO user1AuthenticationDTO;
	private static AuthenticationDTO user2AuthenticationDTO;
	private static AuthenticationDTO adminUserAuthenticationDTO;
	private static SessionDTO user1SessionDTO;
	private static SessionDTO user2SessionDTO;
	private static SessionDTO adminUserSessionDTO;

	/*
	 * user1 session create
	 */
	@Test	
	@RunAsClient	
	@InSequence(100)
	public void sessionsTwitterCreateUser1() {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:create:user1");
		
		// create an authentication object
		Response response = getSecureClient()
				.path(AuthenticationRest.AUTHENTICATIONS_RESTURL)
				.request(MediaType.WILDCARD_TYPE)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.post(null);
		
		// check 
		assertNotNull(response);
		assertEquals(Response.Status.CREATED.getStatusCode(),response.getStatus());
		user1AuthenticationDTO = response.readEntity(AuthenticationDTO.class);

		// create callback parameters
		CallbackTokensDTO twitterCallbackDTO = new CallbackTokensDTO();
		twitterCallbackDTO.setToken(Constants.TESTING_TWITTER_USERNAME1);
		twitterCallbackDTO.setVerifier(Constants.TESTING_TWITTER_CALLBACK_VERIFIER);
		twitterCallbackDTO.setSessionTimeout(TestConstants.TEST_TIMEOUT_LONG);

		// create session using test callback parameters
		logger.info("creating: {}", twitterCallbackDTO.toString());
		response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/twitter")
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user1AuthenticationDTO.getUuid())				
				.post(Entity.entity(twitterCallbackDTO,	MediaType.APPLICATION_JSON), Response.class);

		// check not null
		assertNotNull(response);

		// check response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.CREATED.getStatusCode());
		assertEquals(Response.Status.CREATED.getStatusCode(),
				response.getStatus());

		// log the response
		user1SessionDTO = response.readEntity(SessionDTO.class);
		logger.info("created: {}", user1SessionDTO.toString());
		
		// validate
		assertNotNull(user1SessionDTO);
		assertNotNull(user1SessionDTO.getUuid());
		assertEquals(user1SessionDTO.getUser().getHandle(), Constants.TESTING_TWITTER_USERNAME1);
	}
	
	/*
	 * user2 sesssion create
	 */
	@Test	
	@RunAsClient	
	@InSequence(111)
	public void sessionsTwitterCreateUser2 () {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:create:user2");
	
		// create an authentication object
		Response response = getSecureClient()
				.path(AuthenticationRest.AUTHENTICATIONS_RESTURL)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.post(null);
		
		// check 
		assertNotNull(response);
		assertEquals(Response.Status.CREATED.getStatusCode(),response.getStatus());
		user2AuthenticationDTO = response.readEntity(AuthenticationDTO.class);

		// create callback parameters
		CallbackTokensDTO twitterCallbackDTO = new CallbackTokensDTO();
		twitterCallbackDTO.setToken(Constants.TESTING_TWITTER_USERNAME2);
		twitterCallbackDTO.setVerifier(Constants.TESTING_TWITTER_CALLBACK_VERIFIER);
		twitterCallbackDTO.setSessionTimeout(TestConstants.TEST_TIMEOUT_LONG);

		// create session using test callback parameters
		logger.info("creating: {}", twitterCallbackDTO.toString());
		response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/twitter")
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user2AuthenticationDTO.getUuid())				
				.post(Entity.entity(twitterCallbackDTO,	MediaType.APPLICATION_JSON), Response.class);

		// check not null
		assertNotNull(response);

		// check response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.CREATED.getStatusCode());
		assertEquals(Response.Status.CREATED.getStatusCode(),
				response.getStatus());

		// log the response
		user2SessionDTO = response.readEntity(SessionDTO.class);
		logger.info("created: {}", user2SessionDTO.toString());
		
		// validate
		assertNotNull(user2SessionDTO);
		assertNotNull(user2SessionDTO.getUuid());
		assertEquals(user2SessionDTO.getUser().getHandle(), Constants.TESTING_TWITTER_USERNAME2);
	}
	
	/*
	 * admin user sesssion create
	 */
	@Test	
	@RunAsClient	
	@InSequence(112)
	public void sessionsTwitterCreateAdmin() {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:create:admin");
		
		// create an authentication object
		Response response = getSecureClient()
				.path(AuthenticationRest.AUTHENTICATIONS_RESTURL)
				.request(MediaType.WILDCARD_TYPE)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.post(null);
		
		// check 
		assertNotNull(response);
		assertEquals(Response.Status.CREATED.getStatusCode(),response.getStatus());
		adminUserAuthenticationDTO = response.readEntity(AuthenticationDTO.class);

		// create callback parameters
		CallbackTokensDTO twitterCallbackDTO = new CallbackTokensDTO();
		twitterCallbackDTO.setToken(Constants.TESTING_TWITTER_ADMINUSERNAME);
		twitterCallbackDTO.setVerifier(Constants.TESTING_TWITTER_CALLBACK_VERIFIER);
		twitterCallbackDTO.setSessionTimeout(TestConstants.TEST_TIMEOUT_LONG);

		// create session using test callback parameters
		logger.info("creating: {}", twitterCallbackDTO.toString());
		response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/twitter")
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, adminUserAuthenticationDTO.getUuid())				
				.post(Entity.entity(twitterCallbackDTO,	MediaType.APPLICATION_JSON), Response.class);

		// check not null
		assertNotNull(response);

		// check response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.CREATED.getStatusCode());
		assertEquals(Response.Status.CREATED.getStatusCode(),
				response.getStatus());

		// log the response
		adminUserSessionDTO = response.readEntity(SessionDTO.class);
		logger.info("created: {}", adminUserSessionDTO.toString());
		
		// validate
		assertNotNull(adminUserSessionDTO);
		assertNotNull(adminUserSessionDTO.getUuid());
		assertEquals(adminUserSessionDTO.getUser().getHandle(), Constants.TESTING_TWITTER_ADMINUSERNAME);
	}
	

	@Test	
	@RunAsClient	
	@InSequence(120)
	public void sessionsTwitterReadSuccessOwner1 () {
		
		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:read:success:owner");

		// user1 UUID and authentication
		Response response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/").path(user1SessionDTO.getUuid())
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user1SessionDTO.getAuthentication())
				.get();

		// check not null
		assertNotNull(response);
		
		// check response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.OK.getStatusCode());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		user1SessionDTO = response.readEntity(SessionDTO.class);
		logger.info("retrieved: {}", user1SessionDTO.toString());
		assertNotNull(user1SessionDTO);
		assertNotNull(user1SessionDTO.getUuid());
		assertNotNull(user1SessionDTO.getUser());
		assertNotNull(user1SessionDTO.getLoginTime());
		assertEquals(user1SessionDTO.getUser().getHandle(), Constants.TESTING_TWITTER_USERNAME1);

	}
	
	@Test	
	@RunAsClient	
	@InSequence(121)
	public void sessionsTwitterReadSuccessOwner2 () {
		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:read:success:owner");

		// user2 UUID and authentication
		Response response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/").path(user2SessionDTO.getUuid())
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user2SessionDTO.getAuthentication())
				.get();

		// check not null
		assertNotNull(response);
		
		// check response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.OK.getStatusCode());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		// log and validate
		user2SessionDTO = response.readEntity(SessionDTO.class);
		logger.info("retrieved: {}", user2SessionDTO.toString());
		assertNotNull(user2SessionDTO);
		assertNotNull(user2SessionDTO.getUuid());
		assertNotNull(user2SessionDTO.getUser());
		assertNotNull(user2SessionDTO.getLoginTime());
		assertEquals(user2SessionDTO.getUser().getHandle(), Constants.TESTING_TWITTER_USERNAME2);

	}
	
	@Test	
	@RunAsClient	
	@InSequence(122)
	public void sessionsTwitterReadSuccessAdmin () {
		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:read:success:admin");

		// admin UUID and authentication
		Response response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/").path(adminUserSessionDTO.getUuid())
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, adminUserSessionDTO.getAuthentication())
				.get();

		// check not null
		assertNotNull(response);
		
		// check response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.OK.getStatusCode());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		// log and validate
		adminUserSessionDTO = response.readEntity(SessionDTO.class);
		logger.info("retrieved: {}", adminUserSessionDTO.toString());
		assertNotNull(adminUserSessionDTO);
		assertNotNull(adminUserSessionDTO.getUuid());
		assertNotNull(adminUserSessionDTO.getUser());
		assertNotNull(adminUserSessionDTO.getLoginTime());
		assertEquals(adminUserSessionDTO.getUser().getHandle(), Constants.TESTING_TWITTER_ADMINUSERNAME);
	}
	
	@Test	
	@RunAsClient	
	@InSequence(132)
	public void sessionsTwitterReadFailOwnership () {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:read:fail:ownership");

		// user2 authentication, user1 uuid
		Response response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/").path(user1SessionDTO.getUuid())
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user2SessionDTO.getAuthentication())
				.get();

		// check not null
		assertNotNull(response);
		
		// log and validate response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.FORBIDDEN.getStatusCode());
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test	
	@RunAsClient 	
	@InSequence(142)
	public void sessionsTwitterDeleteFailOwnership() {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:delete:fail:ownership");

		// retrieve sender
		Response response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/").path(user1SessionDTO.getUuid())
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user2SessionDTO.getAuthentication())
				.delete();

		// check not null
		assertNotNull(response);

		// log and validate response code
		logger.info("response: {}, expecting:{}", response.getStatus(),	Response.Status.FORBIDDEN.getStatusCode());
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}

	@Test	
	@RunAsClient	
	@InSequence(150)
	public void sessionsTwitterDeleteSuccessOwner() {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:delete:success:owner");

		// delete twitter session
		Response response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/").path(user1SessionDTO.getUuid())
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user1SessionDTO.getAuthentication())
				.delete();

		// check not null
		assertNotNull(response);

		// log and validate response code
		logger.info("response: {}, expecting:{}", response.getStatus(), Response.Status.ACCEPTED.getStatusCode());
		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
	}
	

	@Test	
	@RunAsClient	
	@InSequence(151)
	public void sessionsTwitterDeleteSuccessAdmin() {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("sessions:twitter:delete:success:admin");


		// delete twitter session
		Response response = getSecureClient()
				.path(SessionRest.SESSIONS_RESTURL)
				.path("/").path(user2SessionDTO.getUuid())
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, adminUserSessionDTO.getAuthentication())
				.delete();

		// check not null
		assertNotNull(response);

		// log and validate response code
		logger.info("response: {}, expecting:{}", response.getStatus(), Response.Status.ACCEPTED.getStatusCode());
		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
	}

}
