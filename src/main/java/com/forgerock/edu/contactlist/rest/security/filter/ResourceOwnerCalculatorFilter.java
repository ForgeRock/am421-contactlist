package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * This filter extracts the {@link ContactListSecurityContext} from the current
 * requestContext and sets the sets its resourceOwner bit based on the 
 * current URI Path and the current user's id.
 * This filter uses the following pattern: 
 * <p>@{code /owned-groups/&lt;uid&gt;/*}</p>
 * If the uid extracted from the path matches the uid that can be found in 
 * the security context, then the special role called {@code resource-owner} is
 * added to the security context.
 *
 * @see ContactListSecurityContext
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION + 10)
public class ResourceOwnerCalculatorFilter implements ContainerRequestFilter {

    public final static Logger LOGGER = Logger.getLogger(ResourceOwnerCalculatorFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "ResourceOwnerCalculatorFilter started");
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext instanceof ContactListSecurityContext) {
            LOGGER.log(Level.FINE, "Found ContactListSecurityContext");
            ContactListSecurityContext sc = (ContactListSecurityContext) securityContext;
            boolean resourceOwner = isResourceOwner(requestContext.getUriInfo(),
                    sc.getUserPrincipal().getName());
            sc.setResourceOwner(resourceOwner);
        }
    }

    private boolean isResourceOwner(final UriInfo uriInfo, final String uid) {
        List<PathSegment> segments = uriInfo.getPathSegments();

        if (segments.size() > 1
                && segments.get(0).getPath().equals("owned-groups")
                && segments.get(1).getPath().equals(uid)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Resource owner detected {0}", uriInfo.getPath());
            }
            return true;
        }
        return false;
    }
}
