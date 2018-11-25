package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.auth.AuthResource;
import com.forgerock.edu.contactlist.rest.auth.TokenInfo;
import com.forgerock.edu.contactlist.rest.exception.InvalidTokenIdException;
import com.forgerock.edu.contactlist.rest.security.ContactListPrincipal;
import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.util.FilterUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

/**
 * This filter creates a custom SecurityContext and assigns it with the current
 * request based on {@code Authorization} header. This filter extracts the
 * bearer token from the Authorization header if presents, then uses
 * {@link AuthResource#getTokenInfo(java.lang.String)} to gather token
 * information. If a valid {@link TokenInfo} object is returned, then extracts
 * the {@link User} object from it and creates a {@link ContactListPrincipal}.
 * This principal is wrapped into a {@link ContactListSecurityContext} object,
 * which is responsible for authorization related functionality.
 *
 * @see ContactListSecurityContext
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION)
public class LocalTokenStoreSessionValidatorFilter implements ContainerRequestFilter {


    public final static Logger LOGGER = Logger.getLogger(LocalTokenStoreSessionValidatorFilter.class.getName());

    @Inject
    private AuthResource authResource;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            LOGGER.log(Level.FINE, "AUTHORIZATION_FILTER started");
            String tokenId = FilterUtil.extractBearerToken(requestContext);
            TokenInfo tokenInfo = authResource.getTokenInfo(tokenId);

            //if authResource.getTokenInfo did not throw an exeption
            ContactListPrincipal principal = new ContactListPrincipal(tokenInfo.getUser(), tokenId);
            SecurityContext ctx = new ContactListSecurityContext(principal, true, "Bearer token");
            requestContext.setSecurityContext(ctx);
            LOGGER.log(Level.FINE, "SecurityContext is set for user {0}", tokenInfo.getUser());
        } catch (InvalidTokenIdException ex) {
            LOGGER.log(Level.FINE, "No token or invalid token", ex);
        }
    }
}
