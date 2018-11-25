package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.auth.AuthResource;
import com.forgerock.edu.contactlist.rest.security.ContactListPrincipal;
import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.security.AuthToken;
import com.forgerock.edu.contactlist.util.FilterUtil;
import com.forgerock.edu.contactlist.util.OpenAMClient;
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
 * This filter tries to set the SecurityContext by trying to interpret the token
 * found in the {@code Authorization} header as an OpenAM tokenId. This filter
 extracts the bearer token from the Authorization header if presents, then
 validates it with OpenAM's session validation REST API. If it is a valid
 token it gathers the user info from OpenAM, the builds the {@link User}
 * object based on this information and creates a {@link ContactListPrincipal}.
 * This principal is wrapped into a {@link ContactListSecurityContext} object,
 * which is responsible for authorization related functionality. This filter
 * also caches the retrieved information for one minute in the local tokenStore.
 *
 * @see ContactListSecurityContext
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION + 1)
public class OpenAMSessionValidatorFilter implements ContainerRequestFilter {

    private final static Logger LOGGER = Logger.getLogger(OpenAMSessionValidatorFilter.class.getName());

    @Inject
    private AuthResource authResource;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "OpenAMSessionValidatorFilter.filter");
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext instanceof ContactListSecurityContext) {
            LOGGER.log(Level.FINE, "ContactListSecurityContext found, exiting...");
            //If there is already a session is coming from the cache, we are done.
            return;
        }
        try {
            String tokenId = FilterUtil.extractBearerToken(requestContext);
            if (tokenId == null) {
                return;
            }
            String uid = OpenAMClient.validateSessionAndExtractUserId(tokenId);
            if (uid == null) {
                return;
            }
            User user = OpenAMClient.getUserProfile(uid, tokenId);
            //caching the token for one minute
            AuthToken token = new AuthToken(tokenId, user, 60, 60);
            authResource.getTokenStore().storeToken(token);

            ContactListPrincipal principal = new ContactListPrincipal(user, tokenId);
            SecurityContext ctx = new ContactListSecurityContext(principal, true, "Bearer token");
            requestContext.setSecurityContext(ctx);
            
            //setting this property to signal the next filters that there is a new session
            requestContext.setProperty(FilterContstants.IS_NEW_SESSION_KEY, Boolean.TRUE);
            
            LOGGER.log(Level.FINE, "SecurityContext is set for user {0}", user);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Unexpected exception during validating session", ex);
        }
    }

}
