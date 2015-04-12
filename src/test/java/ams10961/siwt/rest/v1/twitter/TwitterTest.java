package ams10961.siwt.rest.v1.twitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import ams10961.siwt.rest.v1.AuthenticationRest;
import ams10961.siwt.rest.v1.RestTestBase;
import ams10961.siwt.rest.v1.TestConstants;
import ams10961.siwt.rest.v1.TwitterRest;
import ams10961.siwt.rest.v1.dto.AuthenticationDTO;
import ams10961.siwt.rest.v1.dto.twitter.RedirectDTO;

@RunWith(Arquillian.class)
public class TwitterTest extends RestTestBase {

	final static Logger logger = LoggerFactory.getLogger(TwitterTest.class);
	
	private static AuthenticationDTO user1AuthenticationDTO = null;

	@Test @RunAsClient  @InSequence(200)
	public void getRedirectUrl() {

		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("twitter:redirect:success");
		
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
	
		// get redirection URL
		response = getSecureClient()
				.path(TwitterRest.TWITTER_RESTURL)
				.path("/redirect")
				.request(MediaType.APPLICATION_JSON)
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.header(HttpHeaders.AUTHORIZATION, user1AuthenticationDTO.getUuid())
				.get();
		
		// check not null
		assertNotNull(response);
		
		// check response code
		logger.info("response: {}, expecting:{}",response.getStatus(),Response.Status.OK.getStatusCode());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		
		// log the response
		RedirectDTO redirectUrl = response.readEntity(RedirectDTO.class);
		logger.info("returned: {}", redirectUrl.toString());
	}

}
