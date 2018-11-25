package com.forgerock.edu.contactlist.rest.userprofile;

import com.forgerock.edu.contactlist.dao.UserDAO;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import com.forgerock.edu.contactlist.rest.SuccessMessage;
import com.forgerock.edu.contactlist.util.LDAPRequestUtil;
import com.forgerock.edu.contactlist.rest.exception.OptimisticLockException;
import com.forgerock.edu.contactlist.rest.SuccessfulUpdate;
import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.exception.ConstraintViolatedException;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.ModificationType;
import org.forgerock.opendj.ldap.requests.ModifyRequest;
import org.forgerock.opendj.ldap.requests.Request;
import org.forgerock.opendj.ldap.requests.Requests;

/**
 *
 * @author vrg
 */
@Protected
public class UserProfileResource {

    public final static Logger LOGGER = Logger.getLogger(UserProfileResource.class.getName());//"contactlist.rest.UserProfileResource"
    
    private final LDAPConnectionFactory connectionFactory;
    private final User user;

    public UserProfileResource(LDAPConnectionFactory connectionFactory, User user) {
        this.connectionFactory = connectionFactory;
        this.user = user;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users/read")
    public Response get(@HeaderParam("If-None-Match") String revision) {
        if (user.getRevision().equals(revision)) {
            return Response.notModified().header("ETag", user.getRevision())
                    .build();
        }
        return Response.ok().header("ETag", user.getRevision())
                .entity(user.getJsonObject())
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users/delete")
    public Response deleteUser(@HeaderParam("If-Match") String revision,
            @Context SecurityContext securityContext) throws ErrorResultException {
        if (securityContext.getUserPrincipal().getName().equalsIgnoreCase(user.getUid())) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new ErrorMessage("Not allowed to delete your own profile.", ErrorType.NOT_AUTHORIZED))
                    .build();
        }
        if (revision != null && !Objects.equals(user.getRevision(), revision)) {
            throw new OptimisticLockException(user.getRevision(), revision,
                    "Revision is not matching");
        }
        UserDAO dao = new UserDAO(connectionFactory);
        dao.delete(user);
        return Response.ok(new SuccessMessage("Successfully deleted contact group.")).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("users/modify")
    public Response modifyUser(JsonObject groupObject, @HeaderParam("If-Match") String revision) throws ErrorResultException {
        User changedUser = new User(user);
        changedUser.setJsonObject(groupObject);
        if (changedUser.getId() == null) {
            changedUser.setId(user.getId());
        }
        if (changedUser.getName() == null || changedUser.getName().isEmpty()) {
            throw new ConstraintViolatedException("The name cannot be empty.");
        }
        if (changedUser.getFamilyName() == null || changedUser.getFamilyName().isEmpty()) {
            throw new ConstraintViolatedException("The familyName cannot be empty.");
        }
        if (changedUser.getGivenName() == null || changedUser.getGivenName().isEmpty()) {
            throw new ConstraintViolatedException("The givenName cannot be empty.");
        }

        if (revision != null && !Objects.equals(user.getRevision(), revision)) {
            throw new OptimisticLockException(user.getRevision(), revision,
                    "Revision is not matching");
        }
        
        final String newPassword = changedUser.getPassword();
        changedUser.setPassword(null);

        List<Request> modifyRequests = LDAPRequestUtil.createModifyRequests(user, changedUser);

        if (newPassword != null && !changedUser.getPassword().isEmpty()) {
            LOGGER.log(Level.INFO, "Changing password for user {0}", user.getId());
            ModifyRequest modifyPasswordReq = Requests.newModifyRequest(user.getId().getDN());
            modifyPasswordReq.addModification(ModificationType.REPLACE,
                    "userPassword", 
                    newPassword);
            modifyRequests.add(modifyPasswordReq);
        }

        UserDAO userDAO = new UserDAO(connectionFactory);

        boolean wasChange = userDAO.updateGroupMembershipsBasedOnPrivilegeSet(changedUser);

        String newRevision = user.getRevision();

        if (!modifyRequests.isEmpty()) {
            LOGGER.log(Level.FINE, "Sending modifications to LDAP: {0}", modifyRequests);
            userDAO.executeRequests(modifyRequests);
            newRevision = userDAO.getRevision(user.getId());
            wasChange = true;
        }

        if (wasChange) {
            return Response.ok(new SuccessfulUpdate("User profile updated successfully", newRevision))
                    .header("ETag", newRevision).build();
        } else {
            return Response.notModified(user.getRevision()).build();
        }
    }
}
