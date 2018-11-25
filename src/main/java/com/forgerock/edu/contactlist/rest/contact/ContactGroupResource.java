package com.forgerock.edu.contactlist.rest.contact;

import com.forgerock.edu.contactlist.dao.ContactGroupDAO;
import com.forgerock.edu.contactlist.entity.ContactGroup;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import com.forgerock.edu.contactlist.rest.SuccessMessage;
import com.forgerock.edu.contactlist.util.LDAPRequestUtil;
import com.forgerock.edu.contactlist.rest.exception.OptimisticLockException;
import com.forgerock.edu.contactlist.rest.SuccessfulUpdate;
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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.requests.Request;

/**
 *
 * @author vrg
 */
@Protected
public class ContactGroupResource {

    private final static Logger LOGGER = Logger.getLogger("contactlist.rest.ContactGroupResource");
    private final LDAPConnectionFactory connectionFactory;
    private final ContactGroup group;

    public ContactGroupResource(LDAPConnectionFactory connectionFactory, ContactGroup group) {
        this.connectionFactory = connectionFactory;
        this.group = group;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contact-groups/read"})
    public Response get(@HeaderParam("If-None-Match") String revision) {
        if (group.getRevision().equals(revision)) {
            return Response.notModified().header("ETag", group.getRevision())
                    .build();
        }
        return Response.ok().header("ETag", group.getRevision())
                .entity(group.getJsonObject())
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner"})
    public Response handleAction(@QueryParam("_action") String action) {
        if ("share".equals(action)) {
            return Response.ok()
                    .entity(new SuccessMessage("Sharing initiated for contact group : " + group.getDisplayName()))
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Action is not supported: " + action,
                            ErrorType.BAD_REQUEST))
                    .build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contact-groups/delete"})
    public Response deleteContactGroup(@HeaderParam("If-Match") String revision) throws ErrorResultException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Deleting contact group {0}", group.getJsonObject());
        }
        if (revision != null && !Objects.equals(group.getRevision(), revision)) {
            throw new OptimisticLockException(group.getRevision(), revision,
                    "Revision is not matching");
        }
        ContactGroupDAO dao = new ContactGroupDAO(connectionFactory);
        dao.delete(group);
        return Response.ok(new SuccessMessage("Successfully deleted contact group.")).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contact-groups/modify"})
    public Response modifyContactGroup(JsonObject groupObject, @HeaderParam("If-Match") String revision) throws ErrorResultException {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Trying to modify contact group from {0} to {1}", new Object[]{group.getJsonObject(), groupObject});
        }
        ContactGroup changedGroup = new ContactGroup(group);
        changedGroup.setJsonObject(groupObject);
        if (changedGroup.getId() == null) {
            changedGroup.setId(group.getId());
        }
        if (changedGroup.getDisplayName() == null || changedGroup.getDisplayName().isEmpty()) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "The displayName cannot be empty. {0}", groupObject);
            }
            throw new ConstraintViolatedException("The displayName cannot be empty.");
        }

        if (revision != null && !Objects.equals(group.getRevision(), revision)) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Revision is not matching with current ({0}). {1}", new Object[]{group.getRevision(), revision});
            }
            throw new OptimisticLockException(group.getRevision(), revision,
                    "Revision is not matching");
        }

        List<Request> modifyRequests = LDAPRequestUtil.createModifyRequests(group, changedGroup);

        if (modifyRequests.isEmpty()) {
            return Response.notModified(group.getRevision()).build();
        } else {
            ContactGroupDAO dao = new ContactGroupDAO(connectionFactory);
            dao.executeRequests(modifyRequests);
            String newRevision = dao.getRevision(group.getId());
            return Response.ok(new SuccessfulUpdate("Contact Group updated successfully", newRevision))
                    .header("ETag", newRevision).build();
        }

    }

    @Path("contacts")
    public ContactsResource getContacts() {
        return new ContactsResource(connectionFactory, group);
    }
}
