package ams10961.siwt.rest.v1.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

@RunWith(Arquillian.class)
public class SecurityTest extends RestTestBase {
	
	final static Logger logger = LoggerFactory.getLogger(SecurityTest.class);
	
	
	/*
	 * 
	 */
	@Test 
	@RunAsClient  
	@InSequence(5)
	public void securityAuthenticationCreateFailInsecure () {
		
		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("security:authentication:create:fail:insecure");
		
		// make REST request
		Response response = getNonSecureClient()
				.path(AuthenticationRest.AUTHENTICATIONS_RESTURL)
				.request(MediaType.WILDCARD_TYPE)  // because posting null
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.post(null);
		
		// check 
		assertNotNull(response);
		logger.info("response: {}, expecting:{}",response.getStatus(),Response.Status.FORBIDDEN.getStatusCode());
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(),response.getStatus());
	}
	
	/*
	 * 
	 */
	@Test 
	@RunAsClient  
	@InSequence(10)
	public void securityAuthenticationCreateFailOrigin () {
		
		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("security:authentication:create:fail:origin");
		
		// make REST request
		Response response = getSecureClient()
				.path(AuthenticationRest.AUTHENTICATIONS_RESTURL)
				.request(MediaType.WILDCARD_TYPE)  // because posting null
				.post(null);

		// check
		assertNotNull(response);
		logger.info("response: {}, expecting:{}",response.getStatus(),Response.Status.FORBIDDEN.getStatusCode());
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(),response.getStatus());
	}

}
