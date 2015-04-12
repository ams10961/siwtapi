package ams10961.siwt.util;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import ams10961.siwt.entities.Authentication;
import ams10961.siwt.rest.v1.security.OriginFilter;
import ams10961.siwt.rest.v1.security.authentication.AuthenticationFilter;

/*
 * log requests 
 */
@Provider
@Priority(4)
public class RequestLogger implements ContainerRequestFilter {

	@Context
	HttpServletRequest httpRequest;

	@Inject
	private transient Logger logger;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		if (logger.isInfoEnabled()) {
			StringBuilder out = new StringBuilder();
			
			// http method and URI
			out.append(httpRequest.getMethod()).append("|");
			out.append(httpRequest.getRequestURI()).append("|");
			
			// authentication
			Authentication authentication = (Authentication)httpRequest.getAttribute(AuthenticationFilter.AUTHENTICATION_DATA);
			if (null!=authentication) {
				out.append(authentication.getUuid()).append("|");
				// out.append(authentication.getSessionUuid()).append("|");
			} 
			
			// client IP address
			String originIp = (String) httpRequest.getAttribute(OriginFilter.ORIGIN_IP);
			out.append(originIp);

			logger.info(out.toString());
		}
	}
}
