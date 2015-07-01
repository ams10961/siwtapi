package ams10961.siwt.services.twitter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;

/*
 *
 * 
 */

@ApplicationScoped
public class TwitterService {

	@Inject
	private transient Logger logger;
	
	@Inject
	TwitterCredentials twitterCredentials;

	public static final String TWITTER_PREFIX = "@";

	private static final String OAH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_GET = "GET";
	private static final String EQUALS = "=";
	private static final String QUOTE = "\"";
	private static final String AMPERSAND = "&";
	private static final String QUESTION_MARK = "?";
	private static final String COMMA = ", ";
	private static final String SPACE = " ";

	private static final String OAH_AUTHORISATION = "Authorization";
	private static final String OAH_OAUTH = "OAuth";
	private static final String OAH_CONSUMER_KEY = "oauth_consumer_key";
	private static final String OAH_NONCE = "oauth_nonce";
	private static final String OAH_SIGNATURE = "oauth_signature";
	private static final String OAH_SIGNATURE_METHOD = "oauth_signature_method";
	private static final String OAH_SIGNATURE_METHOD_VALUE = "HMAC-SHA1";
	private static final String OAH_TIMESTAMP = "oauth_timestamp";
	private static final String OAH_VERSION = "oauth_version";
	private static final String OAH_VERSION_VALUE = "1.0";
	public static final String OAH_TOKEN = "oauth_token";
	public static final String OAH_VERIFIER = "oauth_verifier";
	public static final String OAH_TOKEN_SECRET = "oauth_token_secret";

	private static final String CRYPTO_SPEC = "HmacSHA1";
	private static final String TRANSPORT_LAYER_SECURITY = "https";
	private static final int TRANSPORT_LAYER_SECURITY_PORT = 443;
	private static final String HTTP_ENCODING = "UTF-8";

	// Twitter
	private static final String TWITTER_ENDPOINT_HOST = "api.twitter.com";
	public static final String TWITTER_USER_ID = "user_id";
	public static final String TWITTER_SCREEN_NAME = "screen_name";

	// Twitter Login
	private static final String TWITTER_REQUEST_TOKEN_ENDPOINT = "https://api.twitter.com/oauth/request_token";
	private static final String TWITTER_REQUEST_TOKEN_ENDPOINT_PATH = "/oauth/request_token";

	private static final String TWITTER_AUTHENTICATION_ENDPOINT = "https://api.twitter.com/oauth/authenticate";

	private static final String TWITTER_ACCESS_TOKEN_ENDPOINT = "https://api.twitter.com/oauth/access_token";
	private static final String TWITTER_ACCESS_TOKEN_ENDPOINT_PATH = "/oauth/access_token";

	// Twitter services

	// Update Status
	private static final String TWITTER_STATUSES_UPDATE_ENDPOINT = "https://api.twitter.com/1.1/statuses/update.json";
	private static final String TWITTER_STATUSES_UPDATE_ENDPOINT_PATH = "/1.1/statuses/update.json";
	private static final String TWITTER_STATUSES_UPDATE_KEY = "status";

	// User Info
	private static final String TWITTER_USERS_SHOW_ENDPOINT = "https://api.twitter.com/1.1/users/show.json";
	private static final String TWITTER_USERS_SHOW_ENDPOINT_PATH = "/1.1/users/show.json";
	public static final String TWITTER_USERS_SHOW_NAME = "name";
	public static final String TWITTER_USERS_SHOW_DESCRIPTION = "description";

	/*
	 * if present, remove twitter prefix
	 */
	public String removeTwitterPrefix(String screenName) {
		if ((screenName != null) && (screenName.length() > 0)) {
			String updatedScreenName = screenName.trim();
			if (updatedScreenName.startsWith(TWITTER_PREFIX)) {
				return updatedScreenName.substring(1);
			} else {
				return updatedScreenName;
			}
		} else {
			return null;
		}

	}

	/*
	 * add prefix, if not already present
	 */
	public String addTwitterPrefix(String screenName) {
		if ((screenName != null) && (screenName.length() > 0)) {
			String updatedScreenName = screenName.trim();
			if (updatedScreenName.startsWith(TWITTER_PREFIX)) {
				return updatedScreenName;
			} else {
				return TWITTER_PREFIX + updatedScreenName;
			}
		} else {
			return null;
		}
	}

	/*
	 * 
	 */
	public String generateNumberUsedOnce() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/*
	 * 
	 */
	private String currentTimeInSeconds() {
		Calendar now = Calendar.getInstance();
		return new Long(now.getTimeInMillis() / 1000).toString();
	}

	/*
	 * Updated UrlEncode method to handle *, +, ~ correctly
	 */
	private String encode(String value) {
		String encoded = null;
		try {
			encoded = URLEncoder.encode(value, HTTP_ENCODING);
		} catch (UnsupportedEncodingException ignore) {
		}
		StringBuilder buffer = new StringBuilder(encoded.length());
		char focus;
		for (int i = 0; i < encoded.length(); i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				buffer.append("%2A");
			} else if (focus == '+') {
				buffer.append("%20");
			} else if (focus == '%' && (i + 1) < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
				buffer.append('~');
				i += 2;
			} else {
				buffer.append(focus);
			}
		}
		return buffer.toString();
	}

	/*
	 * 
	 */
	private String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException {
		SecretKey secretKey = null;

		byte[] keyBytes = keyString.getBytes();
		secretKey = new SecretKeySpec(keyBytes, CRYPTO_SPEC);

		Mac mac = Mac.getInstance(CRYPTO_SPEC);
		mac.init(secretKey);

		byte[] text = baseString.getBytes();

		return new String(Base64.encodeBase64(mac.doFinal(text))).trim();
	}

	/*
	 * https://dev.twitter.com/docs/auth/creating-signature
	 */
	private String generateOauthSignature(HashMap<String, String> signatureParameters, String endPoint, String consumerSecret,
			String tokenSecret, String httpMethod) throws GeneralSecurityException, UnsupportedEncodingException {

		// Using a tree map will ensure that keys are in alphabetical order
		TreeMap<String, String> paramMap = new TreeMap<String, String>();

		// add values
		paramMap.put(OAH_SIGNATURE_METHOD, OAH_SIGNATURE_METHOD_VALUE);
		paramMap.put(OAH_VERSION, OAH_VERSION_VALUE);

		// add dynamic values
		paramMap.putAll(signatureParameters);

		// construct parameter string, URL encode keys and values, TreeMap keeps
		// alphabetical order
		StringBuilder paramString = new StringBuilder();
		Set<String> keys = paramMap.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = (String) paramMap.get(key);
			// added encoded key/value pairs
			paramString.append(encode(key));
			paramString.append(EQUALS);
			paramString.append(encode(value));
			if (it.hasNext()) {
				paramString.append(AMPERSAND);
			}
		}

		StringBuilder baseString = new StringBuilder();
		baseString.append(httpMethod);
		baseString.append(AMPERSAND);
		baseString.append(encode(endPoint));
		baseString.append(AMPERSAND);
		baseString.append(encode(paramString.toString()));

		StringBuilder signingKey = new StringBuilder();
		signingKey.append(consumerSecret);
		signingKey.append(AMPERSAND);
		// tokenSecret not always known at this point, e.g. during sign-in
		if (tokenSecret != null) {
			signingKey.append(tokenSecret);
		}

		// HmacSHA1 hash base string against consumer secret.
		String result = computeSignature(baseString.toString(), signingKey.toString());
		logger.debug("oauth signature:{}", result);
		return result;
	}

	/*
	 * 
	 */
	private String generateOauthHeader(HashMap<String, String> headerParameters, String signature) {

		// TreeMap ensures alphabetical order
		TreeMap<String, String> paramMap = new TreeMap<String, String>();

		// add values
		paramMap.put(OAH_SIGNATURE_METHOD, OAH_SIGNATURE_METHOD_VALUE);
		paramMap.put(OAH_VERSION, OAH_VERSION_VALUE);

		// add dynamic values
		paramMap.put(OAH_SIGNATURE, signature);
		paramMap.putAll(headerParameters);

		// construct the parameter String, URL encode, TreeMap ensures
		// alphabetical order
		StringBuilder authHeader = new StringBuilder();
		authHeader.append(OAH_OAUTH).append(SPACE);
		Set<String> keys = paramMap.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = (String) paramMap.get(key);
			// added encoded key/value pairs
			authHeader.append(encode(key));
			authHeader.append(EQUALS);
			authHeader.append(QUOTE);
			authHeader.append(encode(value));
			authHeader.append(QUOTE);
			if (it.hasNext()) {
				authHeader.append(COMMA);
			}
		}
		String result = authHeader.toString();
		logger.debug("oauth header:{}", result);
		return result;
	}

	/*
	 * 
	 */
	private String assembleRequestBody(HashMap<String, String> bodyParameters) {

		// Using a tree map will ensure that keys are in alphabetical order
		TreeMap<String, String> paramMap = new TreeMap<String, String>();

		// add any additional parameters
		paramMap.putAll(bodyParameters);

		// construct the parameter String, TreeMap should make sure it's
		// alphabetical
		StringBuilder paramString = new StringBuilder();
		Set<String> keys = paramMap.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = (String) paramMap.get(key);
			// added encoded key/value pairs
			paramString.append(encode(key));
			paramString.append(EQUALS);
			paramString.append(encode(value));
			if (it.hasNext()) {
				paramString.append(AMPERSAND);
			}
		}
		String result = paramString.toString();
		logger.debug("request body:{}", result);
		return result;
	}

	/*
	 * http://hc.apache.org/httpcomponents-client-ga/tutorial/html/fundamentals.html
	 */
	private String makeHttpRequest(String endPointHost, String endPointPath, String authHeader, String requestBody, String httpMethod)
			throws TwitterException {

		// client
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// request
		HttpRequestBase request = null;
		// response
		CloseableHttpResponse response = null;
		try {
			// specify the host, protocol, and port
			HttpHost target = new HttpHost(endPointHost, TRANSPORT_LAYER_SECURITY_PORT, TRANSPORT_LAYER_SECURITY);

			// POST or GET Only
			if (METHOD_POST.equalsIgnoreCase(httpMethod)) {
				// set request body for post
				HttpPost postRequest = new HttpPost(endPointPath);
				postRequest.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_FORM_URLENCODED));
				request = postRequest;
			} else {
				request = new HttpGet(endPointPath);
			}

			// OAUTH header
			request.addHeader(OAH_AUTHORISATION, authHeader);

			// add request body

			// Execute it
			response = httpclient.execute(target, request);

			// check response
			int statusCode = response.getStatusLine().getStatusCode();
			switch (statusCode) {
			case HttpStatus.SC_OK:
				// Get the response entity and return
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity);

			case HttpStatus.SC_NOT_FOUND:
				return null;

			default:
				logger.error("request:{}", request.toString());
				logger.error("response:{}", response.toString());
				throw new TwitterException("HTTP Request was not successful");
			}

		} catch (Exception e) {
			logger.error("http request problem", e);
			throw new TwitterException(e);
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				logger.warn("trouble closing http connection cleanly");
			}
		}
	}

	/*
	 * Sign in with Twitter Step 1 - get request token
	 */

	public HashMap<String, String> getRequestToken() throws TwitterException {

		// values for this request
		String _numberUsedOnce = generateNumberUsedOnce();
		String _timeStamp = currentTimeInSeconds();

		// signature parameters
		HashMap<String, String> signatureParameters = new HashMap<String, String>();
		signatureParameters.put(OAH_CONSUMER_KEY, twitterCredentials.getValue(TwitterCredentials.TWITTER_LOGIN_CONSUMER_KEY));
		signatureParameters.put(OAH_NONCE, _numberUsedOnce);
		signatureParameters.put(OAH_TIMESTAMP, _timeStamp);

		// generate the signature
		String oauthSignature = null;
		try {
			// signing secret not known for this call
			oauthSignature = generateOauthSignature(signatureParameters, TWITTER_REQUEST_TOKEN_ENDPOINT,
					twitterCredentials.getValue(TwitterCredentials.TWITTER_LOGIN_CONSUMER_SECRET), null, METHOD_POST);
		} catch (Exception e) {
			throw new TwitterException(e);
		}

		// generate the authorisation header
		// in this case, there are no body or URL parameters to add, so use
		// signature parameters
		String authHeader = generateOauthHeader(signatureParameters, oauthSignature);

		// make HTTP request, with empty request body
		String responseBody = makeHttpRequest(TWITTER_ENDPOINT_HOST, TWITTER_REQUEST_TOKEN_ENDPOINT_PATH, authHeader, "", METHOD_POST);
		logger.debug("request token: responseBody:{}", responseBody);

		if (responseBody == null) {
			throw new TwitterException("request token: unexpected null response from http request");
		}

		// parse the response
		// oauth_token=NPcudxy0yU5T3tBzho7iCotZ3cnetKwcTIRlX0iwRl0&
		// oauth_token_secret=veNRnAWe6inFuo8o2u8SLLZLjolYDmDP7SzL0YfYI&
		// oauth_callback_confirmed=true

		// encode key/value pairs in a results map
		HashMap<String, String> result = new HashMap<String, String>();

		// parse the response
		StringTokenizer responseBodyTokenizer = new StringTokenizer(responseBody, AMPERSAND);
		String keyValue = null;
		if (responseBodyTokenizer.countTokens() == 3) {
			while (responseBodyTokenizer.hasMoreTokens()) {
				keyValue = responseBodyTokenizer.nextToken();
				// split key and value
				StringTokenizer keyValueTokenizer = new StringTokenizer(keyValue, EQUALS);
				if (keyValueTokenizer.countTokens() == 2) {
					String key = keyValueTokenizer.nextToken();
					String value = keyValueTokenizer.nextToken();
					result.put(key, value);
				} else {
					logger.error("request token, http responseBody:{}", responseBody);
					throw new TwitterException("Response body has unexpected format");
				}
			}
		} else {
			logger.error("request token: http responseBody:{}", responseBody);
			throw new TwitterException("Response body has unexpected format");
		}

		// check the correct values exist
		if (result.containsKey(OAH_TOKEN) && result.containsKey(OAH_TOKEN_SECRET) && result.containsKey(OAH_CALLBACK_CONFIRMED)) {
			return result;
		} else {
			logger.error("request token: http responseBody:{}", responseBody);
			throw new TwitterException("Response body missing expected values");
		}
	}

	/*
	 * Generate Authorisation Endpoint URL
	 */
	public String getAuthorisationURL(Map<String, String> requestTokenResponse) {
		StringBuilder urlBuffer = new StringBuilder();
		urlBuffer.append(TWITTER_AUTHENTICATION_ENDPOINT);
		urlBuffer.append(QUESTION_MARK);
		urlBuffer.append(OAH_TOKEN);
		urlBuffer.append(EQUALS);
		urlBuffer.append(requestTokenResponse.get(OAH_TOKEN));
		return urlBuffer.toString();
	}

	/*
	 * sign in with twitter step 2 - convert request token to access token
	 */
	public HashMap<String, String> getAccessToken(String oauthToken, String oauthVerifier) throws TwitterException {

		String _numberUsedOnce = generateNumberUsedOnce();
		String _timeStamp = currentTimeInSeconds();

		String oauthSignature = null;

		// signature parameters
		HashMap<String, String> signatureParameters = new HashMap<String, String>();
		signatureParameters.put(OAH_CONSUMER_KEY, twitterCredentials.getValue(TwitterCredentials.TWITTER_LOGIN_CONSUMER_KEY));
		signatureParameters.put(OAH_NONCE, _numberUsedOnce);
		signatureParameters.put(OAH_TIMESTAMP, _timeStamp);
		// include token in signature parameters too
		signatureParameters.put(OAH_TOKEN, oauthToken);
		// include body parameter in the signature Parameters too
		signatureParameters.put(OAH_VERIFIER, oauthVerifier);

		// generate the signature
		try {
			// signing secret not known at this point
			oauthSignature = generateOauthSignature(signatureParameters, TWITTER_ACCESS_TOKEN_ENDPOINT,
					twitterCredentials.getValue(TwitterCredentials.TWITTER_LOGIN_CONSUMER_SECRET), null, METHOD_POST);
		} catch (Exception e) {
			throw new TwitterException(e);
		}

		// authorisation header parameters, excludes verifier parameter
		HashMap<String, String> authorisationParameters = new HashMap<String, String>();
		authorisationParameters.put(OAH_CONSUMER_KEY, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_CONSUMER_KEY));
		authorisationParameters.put(OAH_NONCE, _numberUsedOnce);
		authorisationParameters.put(OAH_TIMESTAMP, _timeStamp);
		// include access token
		authorisationParameters.put(OAH_TOKEN, oauthToken);
		// exclude verifier, actually sent in the body
		String authHeader = generateOauthHeader(authorisationParameters, oauthSignature);

		// generate the request parameters and body
		HashMap<String, String> bodyParameters = new HashMap<String, String>();
		bodyParameters.put(OAH_VERIFIER, oauthVerifier);
		String requestBody = assembleRequestBody(bodyParameters);

		// make the HTTP request
		String responseBody = makeHttpRequest(TWITTER_ENDPOINT_HOST, TWITTER_ACCESS_TOKEN_ENDPOINT_PATH, authHeader, requestBody,
				METHOD_POST);
		// check non-null
		if (responseBody == null) {
			throw new TwitterException("access token: unexpected null http responseBody");
		}

		// encode key/value pairs in a results map
		HashMap<String, String> result = new HashMap<String, String>();

		// parse the response
		logger.debug("access token: responseBody:{}", responseBody);
		StringTokenizer responseBodyTokenizer = new StringTokenizer(responseBody, AMPERSAND);

		// returns oauth_token, oauth_token_secret, user_id, and screen_name
		String keyValue = null;
		if (responseBodyTokenizer.countTokens() >= 4) {
			while (responseBodyTokenizer.hasMoreTokens()) {
				keyValue = responseBodyTokenizer.nextToken();
				// split key and value
				StringTokenizer keyValueTokenizer = new StringTokenizer(keyValue, EQUALS);
				if (keyValueTokenizer.countTokens() == 2) {
					String key = keyValueTokenizer.nextToken();
					String value = keyValueTokenizer.nextToken();
					result.put(key, value);
				} else {
					logger.error("access token: http responseBody:{}", responseBody);
					throw new TwitterException("Response body has unexpected format");
				}
			}
		} else {
			logger.error("access token: http responseBody:{}", responseBody);
			throw new TwitterException("Response body has unexpected format");
		}

		// check the correct values present
		if (result.containsKey(OAH_TOKEN) && result.containsKey(OAH_TOKEN_SECRET) && result.containsKey(TWITTER_USER_ID)
				&& result.containsKey(TWITTER_SCREEN_NAME)) {
			return result;
		} else {
			logger.error("access token: http responseBody:{}", responseBody);
			throw new TwitterException("Response body missing expected values");
		}
	}

	/*
	 * update status - post something
	 */
	public void statusUpdate(String status) throws TwitterException {

		// generate values for this request
		String _numberUsedOnce = generateNumberUsedOnce();
		String _timeStamp = currentTimeInSeconds();

		// signature parameters
		HashMap<String, String> signatureParameters = new HashMap<String, String>();
		signatureParameters.put(OAH_CONSUMER_KEY, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_CONSUMER_KEY));
		signatureParameters.put(OAH_NONCE, _numberUsedOnce);
		signatureParameters.put(OAH_TIMESTAMP, _timeStamp);
		// include access token
		signatureParameters.put(OAH_TOKEN, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_ACCESS_TOKEN));
		// include status in signature, although actually sent in the body
		signatureParameters.put(TWITTER_STATUSES_UPDATE_KEY, status);

		// generate the signature
		String oauthSignature = null;
		try {
			oauthSignature = generateOauthSignature(signatureParameters, TWITTER_STATUSES_UPDATE_ENDPOINT,
					twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_CONSUMER_SECRET), 
					twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_ACCESS_TOKEN_SECRET),
					METHOD_POST);
		} catch (Exception e) {
			throw new TwitterException(e);
		}

		// generate the authorisation header, exclude the body parameter
		HashMap<String, String> authorisationParameters = new HashMap<String, String>();
		authorisationParameters.put(OAH_CONSUMER_KEY, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_CONSUMER_KEY));
		authorisationParameters.put(OAH_NONCE, _numberUsedOnce);
		authorisationParameters.put(OAH_TIMESTAMP, _timeStamp);
		// include access token
		authorisationParameters.put(OAH_TOKEN, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_ACCESS_TOKEN));
		// do not include the status, sent in the body
		String authHeader = generateOauthHeader(authorisationParameters, oauthSignature);

		// generate the request body
		HashMap<String, String> bodyParameters = new HashMap<String, String>();
		bodyParameters.put(TWITTER_STATUSES_UPDATE_KEY, status);
		String requestBody = assembleRequestBody(bodyParameters);

		// make the HTTP request
		String responseBody = makeHttpRequest(TWITTER_ENDPOINT_HOST, TWITTER_STATUSES_UPDATE_ENDPOINT_PATH, authHeader, requestBody,
				METHOD_POST);
		logger.debug("status update: responseBody:{}", responseBody);
	}

	/*
	 * retrieve user info, or null if user not found
	 */
	public JSONObject userShow(String screenName) throws TwitterException {

		// generate values for this request
		String _numberUsedOnce = generateNumberUsedOnce();
		String _timeStamp = currentTimeInSeconds();

		// assemble signature parameters
		HashMap<String, String> signatureParameters = new HashMap<String, String>();
		signatureParameters.put(OAH_CONSUMER_KEY, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_CONSUMER_KEY));
		signatureParameters.put(OAH_NONCE, _numberUsedOnce);
		signatureParameters.put(OAH_TIMESTAMP, _timeStamp);
		// include access token
		signatureParameters.put(OAH_TOKEN, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_ACCESS_TOKEN));
		// include url encoded parameter in signature too
		signatureParameters.put(TWITTER_SCREEN_NAME, screenName);

		// generate the signature
		String oauthSignature = null;
		try {
			oauthSignature = generateOauthSignature(signatureParameters, TWITTER_USERS_SHOW_ENDPOINT,
					twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_CONSUMER_SECRET), 
					twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_ACCESS_TOKEN_SECRET),
					METHOD_GET);
		} catch (Exception e) {
			throw new TwitterException(e);
		}

		// generate the authorisation header, do not include body/URL parameters
		HashMap<String, String> authorisationParameters = new HashMap<String, String>();
		authorisationParameters.put(OAH_CONSUMER_KEY, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_CONSUMER_KEY));
		authorisationParameters.put(OAH_NONCE, _numberUsedOnce);
		authorisationParameters.put(OAH_TIMESTAMP, _timeStamp);
		authorisationParameters.put(OAH_TOKEN, twitterCredentials.getValue(TwitterCredentials.TWITTER_SERVICES_ACCESS_TOKEN));
		String authHeader = generateOauthHeader(authorisationParameters, oauthSignature);

		// add the URL encoded parameters to the endpoint path
		StringBuilder endPointPath = new StringBuilder();
		endPointPath.append(TWITTER_USERS_SHOW_ENDPOINT_PATH);
		endPointPath.append(QUESTION_MARK);
		endPointPath.append(encode(TWITTER_SCREEN_NAME));
		endPointPath.append(EQUALS);
		endPointPath.append(encode(screenName));

		// make HTTP request, body empty, obviously
		String responseBody = makeHttpRequest(TWITTER_ENDPOINT_HOST, endPointPath.toString(), authHeader, "", METHOD_GET);

		if (responseBody != null) {
			// log response
			logger.debug("user show: responseBody:{}", responseBody);

			// Return as JSON Object
			JSONTokener tokeniser = new JSONTokener(responseBody);

			try {
				return new JSONObject(tokeniser);
			} catch (JSONException e) {
				throw new TwitterException(e);
			}
		} else {
			// user not found
			return null;
		}
	}
}
