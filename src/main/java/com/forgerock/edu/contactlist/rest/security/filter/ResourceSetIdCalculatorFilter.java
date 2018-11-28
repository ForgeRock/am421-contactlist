package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.dao.ContactGroupDAO;
import com.forgerock.edu.contactlist.entity.ContactGroup;
import com.forgerock.edu.contactlist.entity.ContactGroupId;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

//DONE Ch6L1Ex2: Read the javadoc and investigate the code
/**
 * This filter extracts the {@link ContactListSecurityContext} from the current
 * requestContext and sets the sets the current {@code resource_set_id} in it.
 * This filter uses the following pattern:
 * <p>
 * @{code /owned-groups/&lt;uid&gt;/&lt;groupId&gt;/*}</p>
 * <p>
 * If the current contact group (identified by the {@code uid} and
 * {@code groupId} pair) is persistently assigned with a {@code resource_set_id}, then
 * it is assigned to the current security context.
 * </p>
 *
 * @see ContactListSecurityContext#setResourceSetId(java.lang.String)
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION + 11)
public class ResourceSetIdCalculatorFilter implements ContainerRequestFilter {

    public final static Logger LOGGER = Logger.getLogger(ResourceSetIdCalculatorFilter.class.getName());
    private final ContactGroupDAO dao = new ContactGroupDAO(LDAPConnectionFactoryImpl.INSTANCE);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "ResourceSetIdCalculatorFilter started");
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext instanceof ContactListSecurityContext) {
            LOGGER.log(Level.FINE, "Found ContactListSecurityContext");
            ContactListSecurityContext sc = (ContactListSecurityContext) securityContext;
            Optional<ContactGroup> contactGroup = getContactGroup(requestContext.getUriInfo());
            contactGroup.ifPresent( cg -> {
                sc.setResourceSetId(cg.getResourceSetId());
                sc.setResourceOwnerPAToken(cg.getResourceOwnerPAToken());
            });
        }
    }

    /**
     * Determines the ContactGroup assigned to the current URL.
     *
     * @param uriInfo
     * @return the contact group, or null, if the current resource is
     * not shared
     */
    private Optional<ContactGroup> getContactGroup(final UriInfo uriInfo) {
        List<PathSegment> segments = uriInfo.getPathSegments();

        if (segments.size() > 2
                && segments.get(0).getPath().equals("owned-groups")) {
            String owner = segments.get(1).getPath();
            String groupId = segments.get(2).getPath();
            try {
                ContactGroup group = dao.findById(new ContactGroupId(new UserId(owner), groupId));
                return Optional.ofNullable(group);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING,
                        "Contact group could not be loaded, path: {0}, Ex: {1}",
                        new Object[]{uriInfo.getPath(), ex});
            }
        }
        return Optional.empty();
    }

}
