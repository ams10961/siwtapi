package ams10961.siwt;

public class Constants {

	public static String APP_NAME = "siwtapi";
	
	public static final String LOCALHOST = "localhost";
	public static final String LOCALHOST_IP = "127.0.0.1:";
	
	public static final String DEPLOYMENT_DOMAIN = "ams10961.rhcloud.com";

	public static String EMPTY_STRING = "";
	public static String HTTP_PREFIX = "http";
	public static String HTTP_PREFIX_FULL = "http://";
	
	/* max sessions */
	public static final int MAX_SESSIONS = 10;
	
	// don't let an authentication object exist for longer than this
	public static long AUTHENTICATION_MAX_AGE_MS = 3600 * 1000;  /* one hour */

	// expire a session after inactivity
	public static long INACTIVITY_EXPIRY_MS= 300 * 1000; /* five minutes */
	
	/* testing users needed here for some reason */
	public static String TESTING_TWITTER_USERNAME1 = "twitterUser1";
	public static String TESTING_TWITTER_USERNAME2 = "twitterUser2";	
	public static String TESTING_TWITTER_ADMINUSERNAME = "twitterAdminUser";
	public static String TESTING_TWITTER_CALLBACK_VERIFIER = "9e280cbf4d4b243301cf8ff286e6792d10455d53";
	
	public static final String RESOURCE_METHOD_INVOKER = "org.jboss.resteasy.core.ResourceMethodInvoker";

}
