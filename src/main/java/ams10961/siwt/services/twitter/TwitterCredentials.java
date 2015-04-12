package ams10961.siwt.services.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

/*
 * Read twitter credentials from a file system location
 * 
 * OPENSHIFT_DATA_DIR/twitter.properties
 * 
 */

@Singleton
public class TwitterCredentials {

	@Inject
	Logger logger;

	// TODO: update OpenShift specificity - load from classpath sufficiently protected?
	protected static final String HOME_DIRECTORY_SYSTEM_PROPERTY = "user.home";
	protected static final String DATA_DIRECTORY = "app-root/data";
	protected static final String TWITTER_CREDENTIALS_FILE = "twitter.properties";

	// Twitter login
	protected static final String TWITTER_LOGIN_CONSUMER_KEY = "twitter.login.consumer.key";
	protected static final String TWITTER_LOGIN_CONSUMER_SECRET = "twitter.login.consumer.secret";

	// Twitter Services
	protected static final String TWITTER_SERVICES_CONSUMER_KEY = "twitter.services.consumer.key";
	protected static final String TWITTER_SERVICES_CONSUMER_SECRET = "twitter.services.consumer.secret";

	protected static final String TWITTER_SERVICES_ACCESS_TOKEN = "twitter.services.access.token";
	protected static final String TWITTER_SERVICES_ACCESS_TOKEN_SECRET = "twitter.services.access.token.secret";

	private Properties props = new Properties();

	@PostConstruct
	private void initialise() {
		
//		Properties properties = System.getProperties();
//		Enumeration<Object> elements =  properties.keys();
//		while (elements.hasMoreElements()) {
//			String key = (String) elements.nextElement();
//			String value = (String) properties.get(key);
//			logger.info(">{}< = >{}<",key,value);
//		}

		// build credentials properties file path
		StringBuilder path = new StringBuilder();
		path.append(System.getProperty(HOME_DIRECTORY_SYSTEM_PROPERTY));
		if (!path.toString().endsWith("/")) {
			path.append("/");
		}
		path.append(DATA_DIRECTORY);
		if (!path.toString().endsWith("/")) {
			path.append("/");
		}
		path.append(TWITTER_CREDENTIALS_FILE);
		logger.info("Twitter credentials file path: >{}<", path.toString());

		// load properties object
		try {
			props.load(new FileInputStream(new File(path.toString())));

			/* just log first few characters */
			logger.info("{}={}", TWITTER_LOGIN_CONSUMER_KEY, String.format("%.4s...", getValue(TWITTER_LOGIN_CONSUMER_KEY)));
			logger.info("{}={}", TWITTER_LOGIN_CONSUMER_SECRET, String.format("%.4s...", getValue(TWITTER_LOGIN_CONSUMER_SECRET)));
			logger.info("{}={}", TWITTER_SERVICES_CONSUMER_KEY, String.format("%.4s...", getValue(TWITTER_SERVICES_CONSUMER_KEY)));
			logger.info("{}={}", TWITTER_SERVICES_CONSUMER_SECRET, String.format("%.4s...", getValue(TWITTER_SERVICES_CONSUMER_SECRET)));
			logger.info("{}={}", TWITTER_SERVICES_ACCESS_TOKEN, String.format("%.4s...", getValue(TWITTER_SERVICES_ACCESS_TOKEN)));
			logger.info("{}={}", TWITTER_SERVICES_ACCESS_TOKEN_SECRET,
					String.format("%.4s...", getValue(TWITTER_SERVICES_ACCESS_TOKEN_SECRET)));

		} catch (IOException e) {
			logger.error("Couldn't read twitter property file {}",e, path.toString());
		}

	}

	/* clients query values here */
	public String getValue(String propKey) {
		return this.props.getProperty(propKey);
	}
}