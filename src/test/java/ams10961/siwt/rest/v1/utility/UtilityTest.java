package ams10961.siwt.rest.v1.utility;

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

import ams10961.siwt.rest.v1.RestTestBase;
import ams10961.siwt.rest.v1.TestConstants;
import ams10961.siwt.rest.v1.UtilityRest;

@RunWith(Arquillian.class)
public class UtilityTest extends RestTestBase {
	
	final static Logger logger = LoggerFactory.getLogger(UtilityTest.class);
	
	/*
	 * 
	 */
	@Test 
	@RunAsClient  
	@InSequence(1)
	public void pingApi () {
		
		logger.info(TestConstants.LOGGING_SEPARATOR);
		logger.info("utility:activate:succeed");
		
		// make REST request
		Response response = getSecureClient()
				.path(UtilityRest.UTILITY_RESTURL)
				.path("/activate")
				.request(MediaType.APPLICATION_JSON_TYPE) 
				.header(TestConstants.HTTP_ORIGIN, TestConstants.ORIGIN_VALUE)
				.get();
		
		// check 
		assertNotNull(response);
		logger.info("response: {}, expecting:{}",response.getStatus(),Response.Status.OK.getStatusCode());
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
	}

}
