package com.forgerock.edu.contactlist.rest.userprofile;

import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.dao.UserDAO;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.CreatedMessage;
import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import com.forgerock.edu.contactlist.util.JsonUtil;
import java.net.URI;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.ErrorResultIOException;
import org.forgerock.opendj.ldap.SearchResultReferenceIOException;

/**
 *
 * @author vrg
 */
@Path("profiles")
@Protected
public class UserProfilesResource {
    
    public final static Logger LOGGER = Logger.getLogger("contactlist.rest.UserProfilesResource");

    private final LDAPConnectionFactory connectionFactory;

    public UserProfilesResource() {
        this(LDAPConnectionFactoryImpl.INSTANCE);
    }

    public UserProfilesResource(LDAPConnectionFactory connection) {
        this.connectionFactory = connection;
    }

    @Path("{uid}")
    public UserProfileResource getUserProfile(@PathParam("uid") String userId) throws ErrorResultException {

        User user
                = new UserDAO(connectionFactory)
                .findByIdAndAddPrivileges(new UserId(userId));

        return new UserProfileResource(connectionFactory, user);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"users/read"})
    public JsonArray getUserList(@Context SecurityContext securityContext) throws ErrorResultException, ErrorResultIOException, SearchResultReferenceIOException {
        UserDAO userDAO = new UserDAO(connectionFactory);
        return userDAO
                .findAll()
                .map(group -> group.getJsonObject())
                .collect(JsonUtil.toJsonArray());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users/create")
    public Response addUser(JsonObject userObject,
            @Context UriInfo uriInfo) throws ErrorResultException {
        UserId userId = new UserId(null);
        User user = new User(userId);
        user.setJsonObject(userObject);

        connectionFactory.getConnection().add(user.getLdapState());
        URI profileURI = uriInfo.getAbsolutePathBuilder()
                .path("{uid}")
                .resolveTemplate("uid", user.getId().getRDNAttributeValue())
                .build();

        return Response.created(profileURI)
                .entity(new CreatedMessage("User profile created successfully", profileURI))
                .build();
    }
}
