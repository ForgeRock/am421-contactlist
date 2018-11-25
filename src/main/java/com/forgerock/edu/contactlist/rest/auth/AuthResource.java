package com.forgerock.edu.contactlist.rest.auth;

import com.forgerock.edu.contactlist.rest.security.LDAPUserStore;
import com.forgerock.edu.contactlist.rest.security.AuthToken;
import com.forgerock.edu.contactlist.rest.security.AuthTokenStore;
import com.forgerock.edu.contactlist.rest.security.UserStore;
import com.forgerock.edu.contactlist.rest.exception.IncorrectPasswordException;
import com.forgerock.edu.contactlist.rest.exception.InvalidTokenIdException;
import com.forgerock.edu.contactlist.rest.SuccessMessage;
import com.forgerock.edu.contactlist.rest.security.ContactListPrincipal;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import com.forgerock.edu.contactlist.util.JsonUtil;
import java.security.Principal;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * Authentication/token info REST resource. Mapped to {@code "auth"}
 *
 * @author vrg
 */
@Path("auth")
@Singleton
public class AuthResource {

    public final static int VALID_FOR_TEN_MINUTES = 600;

    private final UserStore userStore = new LDAPUserStore();
    private final AuthTokenStore tokenStore = new AuthTokenStore();

    public AuthTokenStore getTokenStore() {
        return tokenStore;
    }

    /**
     * Test resource, returns with "success"
     *
     * @return "success"
     */
    @GET
    @Path("test")
    public String test() {
        return "success";
    }

    /**
     * Authenticates user. {@link Credentials} should be passed in as JSON
     * object.
     *
     * @param credentials
     *
     * @return AuthToken if the username/password combination is valid.
     * @see AuthTokenStore
     * @see MockUserStore
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AuthToken login(Credentials credentials) {
        System.out.println("login called with " + credentials);
        if (userStore.isPasswordMatching(credentials.getUser(), credentials.getPassword())) {
            User user = userStore.findByUid(credentials.getUser());
            if (user != null && (user.getActive() == null || user.getActive())) {
                return tokenStore.createTokenForUser(user, VALID_FOR_TEN_MINUTES);
            }
        }
        throw new IncorrectPasswordException();
    }

    @Path("tokens/{tokenId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TokenInfo getTokenInfo(@PathParam("tokenId") String tokenId) {
        AuthToken token = tokenStore.findByTokenId(tokenId);
        if (token != null) {
            return new TokenInfo(token);
        } else {
            throw new InvalidTokenIdException();
        }
    }

    @Path("tokenInfo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Protected
    public TokenInfo getTokenInfo(@Context SecurityContext securityContext) {
        Principal userPrincipal = securityContext.getUserPrincipal();
        if (userPrincipal instanceof ContactListPrincipal) {
            ContactListPrincipal principal = (ContactListPrincipal) userPrincipal;
            String tokenId = principal.getTokenId();

            AuthToken token = tokenStore.findByTokenId(tokenId);
            if (token != null) {
                return new TokenInfo(token);
            }
        }
        throw new InvalidTokenIdException();
    }

    @Path("privileges")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Protected
    public JsonArray getPrivileges(@Context SecurityContext securityContext) {
        Principal userPrincipal = securityContext.getUserPrincipal();
        if (userPrincipal instanceof ContactListPrincipal) {
            ContactListPrincipal principal = (ContactListPrincipal) userPrincipal;
            return principal.getUser().getPrivileges()
                    .stream()
                    .collect(JsonUtil.toJsonArrayOfStrings());
        } else {
            return Json.createArrayBuilder().build();
        }
    }

    @Path("tokens/{tokenId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public SuccessMessage logout(@PathParam("tokenId") String tokenId) {
        AuthToken removedToken = tokenStore.removeToken(tokenId);
        if (removedToken != null) {
            return new SuccessMessage("Successfully deleted token");
        } else {
            throw new InvalidTokenIdException();
        }
    }

}
