package com.forgerock.edu.contactlist.rest.contact;

import com.forgerock.edu.contactlist.dao.ContactDAO;
import com.forgerock.edu.contactlist.entity.Contact;
import com.forgerock.edu.contactlist.entity.ContactGroup;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.SuccessMessage;
import com.forgerock.edu.contactlist.rest.SuccessfulUpdate;
import com.forgerock.edu.contactlist.rest.exception.ConstraintViolatedException;
import com.forgerock.edu.contactlist.rest.exception.OptimisticLockException;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import com.forgerock.edu.contactlist.util.LDAPRequestUtil;
import java.util.List;
import java.util.Objects;
import javax.annotation.security.RolesAllowed;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.requests.Request;

/**
 *
 * @author vrg
 */
@Protected
public class ContactResource {

    private final LDAPConnectionFactory connectionFactory;
    private final ContactGroup group;
    private final Contact contact;

    public ContactResource(LDAPConnectionFactory connectionFactory, ContactGroup group, Contact contact) {
        this.connectionFactory = connectionFactory;
        this.group = group;
        this.contact = contact;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contacts/read", "uma_view", "uma_manage"})
    public Response get(@HeaderParam("If-None-Match") String revision) {
        if (contact.getRevision().equals(revision)) {
            return Response.notModified().header("ETag", contact.getRevision())
                    .build();
        }
        return Response.ok().header("ETag", contact.getRevision())
                .entity(contact.getJsonObject())
                .build();
    }
        
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contacts/modify", "uma_manage"})
    public Response modifyContact(JsonObject groupObject, @HeaderParam("If-Match") String revision) throws ErrorResultException {
        Contact changedContact = new Contact(contact);
        changedContact.setJsonObject(groupObject);
        if (changedContact.getId() == null) {
            changedContact.setId(contact.getId());
        }
        if (changedContact.getFirstName()== null || changedContact.getFirstName().isEmpty()) {
            throw new ConstraintViolatedException("firstName cannot be empty.");
        }
        if (changedContact.getLastName()== null || changedContact.getLastName().isEmpty()) {
            throw new ConstraintViolatedException("lastName cannot be empty.");
        }

        if (revision != null && !Objects.equals(contact.getRevision(), revision)) {
            throw new OptimisticLockException(contact.getRevision(), revision,
                    "Revision is not matching");
        }

        List<Request> modifyRequests = LDAPRequestUtil.createModifyRequests(contact, changedContact);

        if (modifyRequests.isEmpty()) {
            return Response.notModified(contact.getRevision()).build();
        } else {
            ContactDAO dao = new ContactDAO(connectionFactory);
            dao.executeRequests(modifyRequests);
            String newRevision = dao.getRevision(contact.getId().getDN());
            return Response.ok(new SuccessfulUpdate("Contact updated successfully", newRevision))
                    .header("ETag", newRevision).build();
        }

    }    
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contacts/delete", "uma_manage"})
    public Response deleteContact(@HeaderParam("If-Match") String revision) throws ErrorResultException {
        if (revision != null && !Objects.equals(group.getRevision(), revision)) {
            throw new OptimisticLockException(group.getRevision(), revision,
                    "Revision is not matching");
        }
        ContactDAO dao = new ContactDAO(connectionFactory);
        dao.delete(contact);
        return Response.ok(new SuccessMessage("Successfully deleted contact group.")).build();
    }
}
