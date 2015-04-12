package ams10961.siwt.rest.v1.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

/*
 * reject non secure transport requests
 */

@Provider
@Priority(1)
public class SecureFilter implements ContainerRequestFilter {

	/*
	 * SSL encoding is indicated using this header when SSL offloaded before app server
	 */
	private static final String X_FORWARDED_PROTO = "x-forwarded-proto";
	private static final String X_FORWARDED_PROTO_SECURE = "https";

	@Inject
	private transient Logger logger;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		// Security context
		SecurityContext securityContext = requestContext.getSecurityContext();

		/* get list of request headers */
		MultivaluedMap<String, String> headerMap = requestContext.getHeaders();
		Set<String> headerNames = headerMap.keySet();

		// First Check whether session is over a secure protocol
		if (securityContext.isSecure()) {
			logger.debug("request context is secure");
		} else {
			// use fallback method to establish security when SSL is offloaded before app server
			if (headerMap.containsKey(X_FORWARDED_PROTO)) {
				String protocol = (String) headerMap.getFirst(X_FORWARDED_PROTO);
				if (X_FORWARDED_PROTO_SECURE.equalsIgnoreCase(protocol)) {
					logger.debug("request contains secure protocol header {}, value {}", X_FORWARDED_PROTO, protocol);
				} else {
					logger.warn("protocol header >{}< value not secure >{}<", X_FORWARDED_PROTO, protocol);
					// reject request
					requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Please use SSL for API requests").build());
				}
			} else {
				logger.warn("session not secure, and secure protocol header not found, logging headers and values");
				for (String headerName : headerNames) {
					List<String> headerValues = headerMap.get(headerName);
					for (String headerValue : headerValues) {
						logger.warn("header:{} value:{}", headerName, headerValue);
					}
				}
				// reject request
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Please use SSL for API requests").build());
			}
		}
	}
}