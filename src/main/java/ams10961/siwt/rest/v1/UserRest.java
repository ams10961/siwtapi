package ams10961.siwt.rest.v1;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import ams10961.siwt.entities.User;
import ams10961.siwt.entities.persistence.UserPersistence;
import ams10961.siwt.rest.v1.dto.UserDTO;
import ams10961.siwt.rest.v1.security.authentication.AuthenticationService;

/*
 * needs to be a stateless EJB to ensure a transaction for the lazily loaded collections
 */

@Stateless
@Path("/users")
public class UserRest {

	public final static String USERS_RESTURL = "users";

	@Inject
	private transient Logger logger;

	@EJB
	private UserPersistence userPersistence;

	@Inject
	private Validator validator;

	@Inject
	AuthenticationService authenticationService;

	/*
	 * CREATE
	 */

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(UserDTO userDTO) {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("creating:{}", userDTO.toString());
			}

			// Validates DTO using bean validation
			Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
			if (!violations.isEmpty()) {
				Map<String, String> responseObj = new HashMap<String, String>();
				for (ConstraintViolation<?> violation : violations) {
					responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
				}
				return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
			}

			// create user
			User user = new User();
			user.setHandle(userDTO.getHandle());

			// persist user
			user = userPersistence.create(user);

			// return value, now populated
			userDTO = new UserDTO(user);

			if (logger.isDebugEnabled()) {
				logger.debug("created:{}", userDTO.toString());
			}
			
			// if successful, refresh authentication
			authenticationService.revalidateAuthentication();

			// response
			return Response.created(new URI(userDTO.getHref())).entity(userDTO).build();

		} catch (Exception e) {
			logger.error("generic exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("see server logs").build();
		}

	}

	/*
	 * READ
	 */

	@GET
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("uuid") String uuid) {

		User user = userPersistence.findByUuid(uuid);
		if (user != null) {
			// check whether request user authorised to perform this operation
			User requester = authenticationService.getRequester();
			if (requester.isAdmin() || user.isCreator(requester)) {
				
				// if successful, refresh authentication
				authenticationService.revalidateAuthentication();
				
				return Response.ok(new UserDTO(user)).build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity("neither resource owner nor admin").build();
			}	
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path("/handle/{handle}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserByHandle(@PathParam("handle") String handle) {

		User user = userPersistence.findByHandle(handle);
		if (user != null) {
			// check whether request user authorised to perform this operation
			User requester = authenticationService.getRequester();
			if (requester.isAdmin() || user.isCreator(requester)) {
				
				// if successful, refresh authentication
				authenticationService.revalidateAuthentication();
				
				return Response.ok(new UserDTO(user)).build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity("neither resource owner nor admin").build();
			}			
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	/*
	 * UPDATE
	 */
	@PUT
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUser(UserDTO userDTO, @PathParam("uuid") String uuid) {

		try {
			// Validates DTO using bean validation
			Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
			if (!violations.isEmpty()) {
				Map<String, String> responseObj = new HashMap<String, String>();
				for (ConstraintViolation<?> violation : violations) {
					responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
				}
				return Response.status(Response.Status.BAD_REQUEST).entity(responseObj).build();
			}

			User user = userPersistence.findByUuid(uuid);
			if (user != null) {
				// check whether request user authorised to perform this
				// operation
				User requester = authenticationService.getRequester();
				if (requester.isAdmin() || user.isCreator(requester)) {
					
					// if successful, refresh authentication
					authenticationService.revalidateAuthentication();
					
					// TODO: implement update logic
					return Response.status(Response.Status.NOT_IMPLEMENTED).build();
				} else {
					return Response.status(Response.Status.FORBIDDEN).entity("neither resource owner nor admin").build();
				}
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			logger.error("runtime exception", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/*
	 * DELETE
	 */
	@DELETE
	@Path("/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUser(@PathParam("uuid") String uuid) {

		User user = userPersistence.findByUuid(uuid);
		if (user != null) {
			// check whether request user authorised to perform this operation
			User requester = authenticationService.getRequester();
			if (requester.isAdmin() || user.isCreator(requester)) {
				
				// if successful, refresh authentication
				authenticationService.revalidateAuthentication();
				
				// TODO: implement delete logic
				// return Response.accepted().build();
				return Response.status(Response.Status.NOT_IMPLEMENTED).build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity("neither resource owner nor admin").build();
			}
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

}
