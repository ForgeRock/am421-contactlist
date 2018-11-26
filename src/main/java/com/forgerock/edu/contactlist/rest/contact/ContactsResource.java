package com.forgerock.edu.contactlist.rest.contact;

import com.forgerock.edu.contactlist.dao.ContactDAO;
import com.forgerock.edu.contactlist.entity.Contact;
import com.forgerock.edu.contactlist.entity.ContactGroup;
import com.forgerock.edu.contactlist.entity.ContactId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.CreatedMessage;
import com.forgerock.edu.contactlist.rest.exception.ConstraintViolatedException;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import com.forgerock.edu.contactlist.util.JsonUtil;
import java.net.URI;
import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.UriInfo;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.ErrorResultIOException;
import org.forgerock.opendj.ldap.SearchResultReferenceIOException;

/**
 *
 * @author vrg
 */
@Protected
public class ContactsResource {

    private final LDAPConnectionFactory connectionFactory;
    private final ContactGroup group;

    public ContactsResource(LDAPConnectionFactory connectionFactory, ContactGroup group) {
        this.connectionFactory = connectionFactory;
        this.group = group;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contacts/create", "uma_manage"})
    public Response addContact(JsonObject contactJSON, @Context UriInfo uriInfo) throws ErrorResultException {
        ContactId contactId = new ContactId(group.getId());
        Contact contact = new Contact(contactId);
        contact.setJsonObject(contactJSON);
        if (contact.getFirstName()== null || contact.getFirstName().isEmpty()) {
            throw new ConstraintViolatedException("firstName cannot be empty.");
        }
        if (contact.getLastName()== null || contact.getLastName().isEmpty()) {
            throw new ConstraintViolatedException("lastName cannot be empty.");
        }
        
        connectionFactory.getConnection().add(contact.getLdapState());
        
        URI contactURI = uriInfo.getAbsolutePathBuilder()
                .path("{contactId}")
                .resolveTemplate("contactId", contact.getId().getRDNAttributeValue())
                .build();
        return Response.created(contactURI)
                .entity(new CreatedMessage("Contact created successfully", contactURI))
                .build();
}
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contacts/read", "uma_view", "uma_manage"})
    public JsonArray getContactList() throws ErrorResultException, ErrorResultIOException, SearchResultReferenceIOException {
        return new ContactDAO(connectionFactory)
                .findAllByParentId(group.getId())
                .map(contact -> contact.getJsonObject())
                .collect(JsonUtil.toJsonArray());
    }

    @Path("{id}")
    public ContactResource getContact(@PathParam("id") String id) throws ErrorResultException {
        ContactId contactId = new ContactId(group.getId(), id);

        Contact contact
                = new ContactDAO(connectionFactory)
                .findById(contactId);
        
        return new ContactResource(connectionFactory, group, contact);
    }
}
