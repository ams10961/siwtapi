package ams10961.siwt.rest.v1.security;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import ams10961.siwt.Constants;

/*
 * If response is successful, add CORS headers 
 */
@Provider
@Priority(5)
public class CORSFilter implements ContainerResponseFilter {

	@Context
	HttpServletRequest httpRequest;

	@Context
	HttpServletResponse httpResponse;

	public static final String ORIGIN_HEADER = "origin";
	public static final String ORIGIN_IP = "ORIGIN_IP";

	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
	public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	public static final String METHOD_OPTIONS = "OPTIONS";

	@Inject
	private transient Logger logger;

	/*
	 * If allowed, add CORS Headers to response corresponding to the type of
	 * request
	 */

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		
		int status = httpResponse.getStatus();

		// add CORS headers if successful
		if ((status == HttpServletResponse.SC_OK) || (status == HttpServletResponse.SC_CREATED)	|| (status == HttpServletResponse.SC_ACCEPTED)) {

			// get and trim host and origin
			String host = requestContext.getHeaderString(HttpHeaders.HOST);
			String origin = requestContext.getHeaderString(ORIGIN_HEADER);

			// for all requests add ACCESS_CONTROL_ALLOW_ORIGIN header
			if (host.startsWith(Constants.LOCALHOST) || host.startsWith(Constants.LOCALHOST_IP)) {
				// development testing case first
				if (logger.isDebugEnabled()) {
					logger.debug("deployed to localhost >{}<, allowing local origin >{}<", host, origin);
				}
				httpResponse.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
			} else if ((origin != null) && (origin.trim().endsWith(Constants.DEPLOYMENT_DOMAIN))) {
				// deployment
				if (logger.isDebugEnabled()) {
					logger.debug("deployed remotely >{}<, allowing origin >{}<", host, origin);
				}
				httpResponse.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin.trim());
			} else {
				logger.error("unrecognised origin, should have been caught earlier>{}<, IP >{}<", origin, httpRequest.getRemoteAddr());
			}

			// for OPTIONS requests add ACCESS_CONTROL_ALLOW_HEADERS/METHODS
			// headers
			if (httpRequest.getMethod().equalsIgnoreCase(METHOD_OPTIONS)) {
				// OPTIONS request headers
				String accessControlRequestHeaders = httpRequest.getHeader(ACCESS_CONTROL_REQUEST_HEADERS);
				String accessControlRequestMethods = httpRequest.getHeader(ACCESS_CONTROL_REQUEST_METHOD);
				if (accessControlRequestHeaders != null) {
					httpResponse.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, accessControlRequestHeaders);
				} else {
					logger.warn("missing OPTIONS request headers");
				}
				if (accessControlRequestMethods != null) {
					httpResponse.addHeader(ACCESS_CONTROL_ALLOW_METHODS, accessControlRequestMethods);
				} else {
					logger.warn("missing OPTIONS request methods");
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("status code is not successful, not adding CORS headers >{}<",status);
			}			
		}
	}

}
