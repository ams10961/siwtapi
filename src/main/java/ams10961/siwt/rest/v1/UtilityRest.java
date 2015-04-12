package ams10961.siwt.rest.v1;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/*
 * 
 */
@Stateless
@Path("/utility")
public class UtilityRest {

	public final static String UTILITY_RESTURL = "utility";
	
	/*
	 * PING method to ensure the API is not passivated 
	 */
	@GET
	@Path("/activate")
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll 
	public Response activate () {
		// response
		return Response.ok().build();
	}
	

}
