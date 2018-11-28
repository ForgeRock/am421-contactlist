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
import com.forgerock.edu.contactlist.rest.oauth2.OAuth2HandlerResource;
import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import com.forgerock.edu.contactlist.rest.security.tokenstore.Identity;
import com.forgerock.edu.contactlist.rest.security.tokenstore.Token;
import com.forgerock.edu.contactlist.rest.security.tokenstore.TokenManager;
import com.forgerock.edu.contactlist.uma.UMAClient;
import com.forgerock.edu.contactlist.uma.UMAResourceSet;
import com.forgerock.edu.contactlist.util.RestCallFailedException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.json.Json;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
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
    //DONE Ch6L1Ex2: Check "uma_view" as an allowed role.
    @RolesAllowed({"resource-owner", "contact-groups/read", "uma_view"})
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
    public Response handleAction(@QueryParam("_action") String action, @Context UriInfo uriInfo, @Context SecurityContext sctx) {
        switch (action) {
            case "share":
                return share(uriInfo, sctx);
            case "unshare":
                return unshare(uriInfo, sctx);
            default:
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage("Action is not supported: " + action,
                            ErrorType.BAD_REQUEST))
                    .build();
        }
    }

    /**
     * Share the given contact group if the PAT for the current resource owner
     * is available.
     *
     * @param uriInfo
     * @param sctx
     * @return The changed group with the assigned resource_set_id, or an error
     * message (status = 401 Unauthorized) with an URI, that points to
     * rest/oauth2/obtainPAT, if the PAT for the current user identity is not
     * available, so a new OAuth2 authorization flow should be started in order
     * to obtain it.
     * 
     * @see OAuth2HandlerResource#startPATforUserFlow(java.lang.String) 
     */
    @POST
    @Path("share")
    @RolesAllowed({"resource-owner"})
    public Response share(@Context UriInfo uriInfo, @Context SecurityContext sctx) {
        try {
            ContactGroupDAO dao = new ContactGroupDAO(connectionFactory);
            if (group.getResourceSetId() != null) {
                return returnGroupWithRevision(dao);
            }
            Token patForUser = getPatForUser(sctx);
            if (patForUser != null) {
                UMAResourceSet resourceSet = UMAResourceSet.builder()
                        .name(group.getDisplayName())
                        .type("http://app.test/contactlist/ContactGroup")
                        .addScope("view")
                        .addScope("manage")
                        .iconURI("http://app.test:8080/contactlist/images/contactgroup.png")
                        .build();

                UMAResourceSet newRS = new UMAClient().registerResourceSet(patForUser.getTokenId(), resourceSet);
                ContactGroup changedGroup = new ContactGroup(group);
                changedGroup.setResourceSetId(newRS.getId());
                changedGroup.setResourceOwnerPAToken(patForUser.getTokenId());

                return changeGroup(changedGroup, dao);
            } else {
                return userPatNeeded(uriInfo);
            }

        } catch (NoSuchMethodException | SecurityException | ErrorResultException ex) {
            LOGGER.log(Level.SEVERE, "Could not find method in OAuth2HandlerResource", ex);
            return Response.serverError()
                    .entity(new ErrorMessage(ex.getMessage(), ErrorType.INTERNAL_ERROR))
                    .build();
        }
    }

    @POST
    @Path("unshare")
    @RolesAllowed({"resource-owner"})
    public Response unshare(@Context UriInfo uriInfo, @Context SecurityContext sctx) {
        try {
            final String resourceSetId = group.getResourceSetId();
            ContactGroupDAO dao = new ContactGroupDAO(connectionFactory);
            if (resourceSetId == null) {
                return returnGroupWithRevision(dao);
            } else {
                Token patForUser = getPatForUser(sctx);
                if (patForUser != null) {
                    ContactGroup changedGroup = new ContactGroup(group);
                    final UMAClient umaClient = new UMAClient();
                    UMAResourceSet resourceSet = null;
                    try {
                        resourceSet = umaClient.getResourceSet(
                                patForUser.getTokenId(), resourceSetId);
                    } catch (RestCallFailedException callFailed) {
                        JsonObject error = callFailed.getResponse();
                        System.out.println("error: " + error);
                        if ("not_found".equals(error.getString("error", null))) {
                            changedGroup.setResourceSetId(null);
                        }
                    }
                    if (resourceSet != null) {
                        umaClient.unregisterResourceSet(patForUser.getTokenId(),
                                resourceSet.getId(), resourceSet.getRevision());
                        changedGroup.setResourceSetId(null);
                        changedGroup.setResourceOwnerPAToken(null);
                    }
                    return changeGroup(changedGroup, dao);
                } else {
                    return userPatNeeded(uriInfo);
                }
            }
        } catch (NoSuchMethodException | SecurityException | ErrorResultException ex) {
            LOGGER.log(Level.SEVERE, "Could not find method in OAuth2HandlerResource", ex);
            return Response.serverError()
                    .entity(new ErrorMessage(ex.getMessage(), ErrorType.INTERNAL_ERROR))
                    .build();
        }
    }

    private Response returnGroupWithRevision(ContactGroupDAO dao) throws ErrorResultException {
        String newRevision = dao.getRevision(group.getId());
        group.setRevision(newRevision);
        return Response.ok(group.getJsonObject())
                .header("ETag", newRevision).build();
    }

    private Response changeGroup(ContactGroup changedGroup, ContactGroupDAO dao) throws ErrorResultException {
        List<Request> modifyRequests = LDAPRequestUtil.createModifyRequests(group, changedGroup);

        if (modifyRequests.isEmpty()) {
            return Response.notModified(group.getRevision()).build();
        } else {
            dao.executeRequests(modifyRequests);
            String newRevision = dao.getRevision(group.getId());
            changedGroup.setRevision(newRevision);
            return Response.ok(changedGroup.getJsonObject())
                    .header("ETag", newRevision).build();
        }
    }

    private Response userPatNeeded(UriInfo uriInfo) throws UriBuilderException, NoSuchMethodException, IllegalArgumentException, SecurityException {
        URI startPATforUserURI = uriInfo.getBaseUriBuilder()
                .path(OAuth2HandlerResource.class)
                .path(OAuth2HandlerResource.class.getMethod("startPATforUserFlow", String.class))
                .build();
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorMessage(startPATforUserURI.toString(), ErrorType.USER_PAT_NEEDED))
                .build();
    }

    private Token getPatForUser(SecurityContext sctx) {
        String username = sctx.getUserPrincipal().getName();
        Token patForUser = TokenManager.getInstane().getProtectionApiTokenForUser(
                Identity.builder()
                .user()
                .realm("/")
                .userId(username)
                .build());
        return patForUser;
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
    @RolesAllowed({"resource-owner", "contact-groups/modify", "uma_manage"})
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

        return saveGroup(changedGroup);

    }

    Response saveGroup(ContactGroup changedGroup) throws ErrorResultException {
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
