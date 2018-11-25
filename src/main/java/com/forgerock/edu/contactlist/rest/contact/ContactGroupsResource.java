package com.forgerock.edu.contactlist.rest.contact;

import com.forgerock.edu.contactlist.dao.ContactGroupDAO;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.dao.UserDAO;
import com.forgerock.edu.contactlist.entity.ContactGroup;
import com.forgerock.edu.contactlist.entity.ContactGroupId;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.CreatedMessage;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import com.forgerock.edu.contactlist.util.JsonUtil;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.forgerock.opendj.ldap.EntryNotFoundException;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.ErrorResultIOException;
import org.forgerock.opendj.ldap.SearchResultReferenceIOException;

/**
 *
 * @author vrg
 */
@Path("owned-groups/{ownerId}")
@Protected
public class ContactGroupsResource {

    private final static Logger LOGGER = Logger.getLogger("contactlist.rest.ContactGroupsResource");

    private final LDAPConnectionFactory connectionFactory;

    public ContactGroupsResource() {
        this(LDAPConnectionFactoryImpl.INSTANCE);
    }

    public ContactGroupsResource(LDAPConnectionFactory connection) {
        this.connectionFactory = connection;
    }

    @Path("{groupid}")
    public ContactGroupResource getContactGroup(@PathParam("ownerId") String ownerId,
            @PathParam("groupid") String groupId) throws ErrorResultException {

        ContactGroupId contactGroupId = new ContactGroupId(new UserId(ownerId), groupId);

        ContactGroup group
                = new ContactGroupDAO(connectionFactory)
                .findById(contactGroupId);

        return new ContactGroupResource(connectionFactory, group);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contact-groups/read"})
    public JsonArray getContactGroupList(@PathParam("ownerId") String ownerId) throws ErrorResultException, ErrorResultIOException, SearchResultReferenceIOException {
        LOGGER.log(Level.INFO, "Trying to read contact groups for user {0}", ownerId);
        UserId owner = new UserId(ownerId);
        UserDAO userDAO = new UserDAO(connectionFactory);
        try {
            userDAO.findById(owner);
        } catch (EntryNotFoundException ex) {
            LOGGER.log(Level.INFO, "Automatically creating user profile {0}", ownerId);
            userDAO.createUserByUID(ownerId);
        }
        return new ContactGroupDAO(connectionFactory)
                .findAllByParentId(owner)
                .map(group -> group.getJsonObject())
                .collect(JsonUtil.toJsonArray());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"resource-owner", "contact-groups/create"})
    public Response addContactGroup(@PathParam("ownerId") String ownerId,
            JsonObject groupObject,
            @Context UriInfo uriInfo,
            @Context SecurityContext sctx) throws ErrorResultException {
        UserId owner = new UserId(ownerId);
        ContactGroup group = new ContactGroup(new ContactGroupId(owner));
        group.setJsonObject(groupObject);
        new UserDAO(connectionFactory).findById(owner);

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "Adding contact group {0}", group.getJsonObject());
        }

        connectionFactory.getConnection().add(group.getLdapState());
        URI groupURI = uriInfo.getAbsolutePathBuilder()
                .path("{groupId}")
                .resolveTemplate("groupId", group.getId().getRDNAttributeValue())
                .build();

        return Response.created(groupURI)
                .entity(new CreatedMessage("Contact group created successfully", groupURI))
                .build();
    }
}
