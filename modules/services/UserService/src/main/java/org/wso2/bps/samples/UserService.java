package org.wso2.bps.samples;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/userservice/")
public class UserService {

	Map<String, User> users;

	public UserService() {

		users = new HashMap<String, User>();
	}

	// Get method
	@GET
	@Path("/users/name/{id}")
	@Produces(MediaType.TEXT_XML)
	public String doGET(@PathParam("id") String id) {

		System.out.println("...Getting User, User id is: " + id);
		try {
			User u = users.get(id);
			return "<username>" + u.getName() + "</username>";

		} catch (Exception ex) {
			return "<username> not found.</username>";
		}
	}

	// Put method
	@PUT
	@Path("/users/name/{id}/{name}")
	@Produces(MediaType.TEXT_XML)
	public String doPut(@PathParam("id") String id,
			@PathParam("name") String name) {
		System.out.println("...Inserting User, User id is: " + id
				+ " name is: " + name);
		try {
			if (users.containsKey(id)) {
				return "<error>user id :" + id + " already exist.</error>";
			}
			users.put(id, new User(id, name));
			return "<result>" + name + " for id : " + id
					+ " was added.</result>";

		} catch (Exception ex) {
			return "<error>" + ex + "</error>";
		}
	}

	// Post method
	@POST
	@Path("/users/name/{id}/{name}")
	@Produces(MediaType.TEXT_XML)
	public String doPost(@PathParam("id") String id,
			@PathParam("name") String name) {

		System.out.println("...Updating User, User id is: " + id + " name is: "
				+ name);
		try {
			User u = users.get(id);
			u.setName(name);
			return "<result> Name of user id: " + id + " was changed to "
					+ name + "</result>";

		} catch (Exception ex) {
			return "<error>user not found</error>";
		}
	}

	// Delete method
	@DELETE
	@Path("/users/name/{id}")
	@Produces(MediaType.TEXT_XML)
	public String doDelete(@PathParam("id") String id,
			@PathParam("name") String name) {

		System.out.println("...Deleting User, User id is: " + id);
		try {
			if (!users.containsKey(id)) {
				return "<error>user not found</error>";
			}
			users.remove(id);
			return "<result>user id: " + id + " deleted.</result>";

		} catch (Exception ex) {
			return "<error>user not found</error>";
		}
	}

}
