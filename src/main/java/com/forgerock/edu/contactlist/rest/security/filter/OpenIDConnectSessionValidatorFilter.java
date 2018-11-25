package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.auth.AuthResource;
import com.forgerock.edu.contactlist.rest.security.ContactListPrincipal;
import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.security.AuthToken;
import com.forgerock.edu.contactlist.rest.security.AuthTokenStore;
import com.forgerock.edu.contactlist.util.FilterUtil;
import com.forgerock.edu.contactlist.util.OpenIDConnectClient;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

/**
 * This filter tries to set the SecurityContext by trying to interpret the token
 * found in the {@code Authorization} header as an OpenID Connect access token.
 * This filter extracts the bearer token from the Authorization header if
 * presents, then collects the user information assigned to this access token
 * with the configured OpenID Connect Provider's user info endpoint. Then
 * creates a {@link ContactListPrincipal} from the provided information. This
 * principal is wrapped into a {@link ContactListSecurityContext} object, which
 * is responsible for authorization related functionality. This filter also
 * caches the retrieved information for one minute in the local tokenStore.
 *
 * @see ContactListSecurityContext
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION + 1)
public class OpenIDConnectSessionValidatorFilter implements ContainerRequestFilter {

    private final static Logger LOGGER = Logger.getLogger(OpenIDConnectSessionValidatorFilter.class.getName());

    @Inject
    private AuthResource authResource;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "OpenIDConnectSessionValidatorFilter.filter");
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
            JsonObject userInfo = new OpenIDConnectClient().getUserInfo(tokenId);
            if (userInfo == null) {
                return;
            }

            AuthToken authToken = convertToAuthToken(tokenId, userInfo);

            authResource.getTokenStore().storeToken(authToken);

            ContactListPrincipal principal = new ContactListPrincipal(authToken.getUser(), tokenId);
            SecurityContext ctx = new ContactListSecurityContext(principal, true, "Bearer token");
            requestContext.setSecurityContext(ctx);

            //setting this property to signal the next filters that there is a new session
            requestContext.setProperty(FilterContstants.IS_NEW_SESSION_KEY, Boolean.TRUE);

            LOGGER.log(Level.FINE, "SecurityContext is set for user {0}", authToken.getUser());
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Unexpected exception during validating session", ex);
        }
    }

    /**
     * Converts the given userInfo object into a local AuthToken instance and
     * registers it into the {@link AuthTokenStore}. This method first creates a
     * {@link User} object from the data found in the given userInfo object,
     * then wraps it into an {@link AuthToken} object. This newly created
     * AuthToken's expiration time is one minute or less, if the
     * {@code expires_in} property found in the given {@code userInfo} object is
     * less.
     *
     * @param accessToken The access token value. This will be the id of the
     * created {@link AuthToken}.
     * @param userInfo The user info returned by the OpenID Connect provider
     * @return The converted and stored token
     *
     * @see AuthToken#AuthToken(java.lang.String,
     * com.forgerock.edu.contactlist.rest.auth.User, int)
     * @see
     * AuthTokenStore#storeToken(com.forgerock.edu.phonebook.rest.auth.AuthToken)
     */
    public AuthToken convertToAuthToken(String accessToken, JsonObject userInfo) {

        JsonArray privilegesArray = userInfo.getJsonArray("contactlist-privileges");
        Set<String> privileges = new HashSet<>();
        if (privilegesArray != null) {
            for (int i = 0; i < privilegesArray.size(); i++) {
                String privilege = privilegesArray.getString(i);
                privileges.add(privilege);
            }
        }
        //TODO Ch5L1Ex4: Read the field named "expires_in" from userInfo and store it in expiresIn variable. 
        //TODO Ch5L1Ex4: Use userInfo.get() method and cast it to JsonNumber -> this way there will not be an exception thrown if the value is missing.
        JsonNumber expiresIn = null;
        int expirationTimeInSeconds = expiresIn != null ? expiresIn.intValue() : 60;

        User user = User.builder()
                //TODO Ch5L1Ex4: Ex4: Extract the user's full name from the field named "name". Use userInfo.getString(String fieldName) method.
                .uid("uid")
                //TODO Ch5L1Ex4: Extract the user's full name from the field named "name"
                .name("Full Name")
                //TODO Ch5L1Ex4: Extract the user's email from the field named "email"
                .email("Email address")
                //TODO Ch5L1Ex4: Extract the user's family name from the field named "family_name"
                .familyName("Family Name")
                //TODO Ch5L1Ex4: Extract the user's given name from the field named "given_name"
                .givenName("Given name")
                .privileges(privileges)
                .build();

        LOGGER.log(Level.INFO, "User extracted from OAuth2 userinfo reponse: {0}", user);
        int ttl = Math.min(60, expirationTimeInSeconds);
        AuthToken authToken = new AuthToken(accessToken, user, ttl, ttl);
        return authToken;
    }

}
