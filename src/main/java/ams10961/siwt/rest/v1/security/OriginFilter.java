package ams10961.siwt.rest.v1.security;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import ams10961.siwt.Constants;

/*
 * Filter all REST requests by origin and host
 * 
 */
@Provider
@Priority(2)
public class OriginFilter implements ContainerRequestFilter {

	@Context
	HttpServletRequest httpRequest;

	@Context
	HttpServletResponse httpResponse;

	// TODO: update for your deployment
	public static final String ORIGIN_HEADER = "origin";
	public static final String ORIGIN_IP = "ORIGIN_IP";
	public static final String TESTING_FLAG = "testing";

	@Inject
	private transient Logger logger;

	/*
	 * Check request origin, reject if disallowed.
	 * 
	 * Add origin IP address as httpRequest attribute
	 */

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		/* log request headers */
		if (logger.isDebugEnabled()) {
			MultivaluedMap<String, String> requestHeaders = requestContext.getHeaders();
			Set<String> requestHeaderNames = requestHeaders.keySet();
			logger.debug("Checking request headers");
			for (String requestHeaderName : requestHeaderNames) {
				logger.debug("header >{}<, value >{}<", requestHeaderName, requestHeaders.get(requestHeaderName));
			}
		}

		// get and trim host and origin
		String host = requestContext.getHeaderString(HttpHeaders.HOST);
		String origin = requestContext.getHeaderString(ORIGIN_HEADER);

		// reject anything without a host, impossible?
		if ((host == null) || (host.trim().length() == 0)) {
			logger.warn("rejecting request without host, from IP >{}< ", httpRequest.getRemoteAddr());
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("rejecting request without host").build());
			return;
		} else {
			host = host.trim();
		}

		// reject anything without an origin
		if ((origin == null) || (origin.trim().length() == 0)) {
			logger.warn("rejecting request without origin, from IP >{}< ", httpRequest.getRemoteAddr());
			requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("rejecting request without origin").build());
			return;
		} else {
			origin = origin.trim();
		}

		// check origin
		if (host.startsWith(Constants.LOCALHOST) || host.startsWith(Constants.LOCALHOST_IP)) {
			// development testing case first
			if (logger.isDebugEnabled()) {
				logger.debug("deployed to localhost >{}<, allowing local origin >{}<", host, origin);
			}
			// indicate testing
			httpRequest.setAttribute(TESTING_FLAG, true);

		} else {
			// deployment
			// TODO: update this filter for your deployment
			if (origin.endsWith(Constants.DEPLOYMENT_DOMAIN)) {
				// deployment
				if (logger.isDebugEnabled()) {
					logger.debug("deployed remotely >{}<, allowing origin >{}<", host, origin);
				}
				// indicate testing
				httpRequest.setAttribute(TESTING_FLAG, false);
			} else {
				logger.warn("rejecting request from unrecognised origin >{}<, IP >{}<", origin, httpRequest.getRemoteAddr());
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("origin unrecognised").build());
				return;
			}
		}

		// add origin IP address as attribute to the request, used later as part
		// of authentication check
		String ipAddress = determineIpAddress(httpRequest);
		if (logger.isDebugEnabled()) {
			logger.debug("remote IP address >{}<", ipAddress);
		}
		httpRequest.setAttribute(ORIGIN_IP, ipAddress);
	}

	/*
	 * request.getRemoteAddr() does not always work for proxies
	 * 
	 * TODO: consider
	 * http://www.codereye.com/2010/01/get-real-ip-from-request-in-java.html
	 */
	
	public static final String CANONICAL_ORIGIN_IP_HEADER = "X-Forwarded-For";

	private static final String[] HEADERS_TO_TRY = { 
		CANONICAL_ORIGIN_IP_HEADER
	};
	
	/*
	 *  Alternatives to consider:
	
	    "Proxy-Client-IP",
	    "WL-Proxy-Client-IP"};
	    "HTTP_X_FORWARDED_FOR",
	    "HTTP_X_FORWARDED",
	    "HTTP_X_CLUSTER_CLIENT_IP",
	    "HTTP_CLIENT_IP",
	    "HTTP_FORWARDED_FOR",
	    "HTTP_FORWARDED",
	    "HTTP_VIA",
	    "REMOTE_ADDR" };
	*/
	
	private String determineIpAddress(HttpServletRequest request) {

	    for (String header : HEADERS_TO_TRY) {
	        String ip = request.getHeader(header);
	        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
	            return ip;
	        }
	    }
	    return request.getRemoteAddr();
	}
}
