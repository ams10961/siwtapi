package ams10961.siwt.rest.v1;

public class TestConstants {
	
	// nasty hard coded hack, should be possible to read this dynamically - MBean?
	public static final String JBOSS_TESTING_SECURE_PORT = "8543";
	
	public static final String HTTP_ORIGIN = "origin";
	public static final String HTTP_AUTHORIZATION = "authorization";
	public static final String ORIGIN_VALUE = "127.0.0.1:";

	// this needs to have a different name from anything deployed on the local server
	public static final String TEST_WAR = "siwtapitest.war";
	
	public static final String TEST_VALID_TWITTER_USER = "twitter";
	public static final String TEST_NON_EXISTENT_TWITTER_USER = "xYzA1baty8ab49";
	
	public static final long TEST_TIMEOUT_SHORT = 1000;
	public static final long TEST_TIMEOUT_LONG = 100000;
	
	public static final String LOGGING_SEPARATOR = "----------------------------------------------------------------------------------";
}
