package com.forgerock.edu.contactlist.rest.oauth2;

import com.forgerock.edu.contactlist.rest.security.tokenstore.Identity;
import com.forgerock.edu.contactlist.rest.security.tokenstore.OAuth2AccessToken;
import com.forgerock.edu.contactlist.rest.security.tokenstore.Token;
import com.forgerock.edu.contactlist.rest.security.tokenstore.TokenManager;
import com.forgerock.edu.contactlist.rest.security.tokenstore.UMAConstants;
import com.forgerock.edu.contactlist.uma.AccessToken;
import com.forgerock.edu.contactlist.uma.UMAClient;
import com.forgerock.edu.contactlist.util.OpenIDConnectClient;
import java.net.URI;
import javax.inject.Singleton;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Authentication/token info REST resource. Mapped to {@code "auth"}
 *
 * @author vrg
 */
@Path("oauth2")
@Singleton
public class OAuth2HandlerResource {

    UMAClient umaClient;
    OpenIDConnectClient openIDClient = new OpenIDConnectClient();

    public OAuth2HandlerResource() {
        this.umaClient = new UMAClient();
    }

    public OAuth2HandlerResource(UMAClient umaClient) {
        this.umaClient = umaClient;
    }

    /**
     * Redirecting the browser to this resource starts a new authorization flow
     * that will try to obtain the special PAT assigned to the resource owner.
     * This is needed because the resource set registration endpoint of the
     * authorization server requires a token, that is assigned to the resource
     * owner's identity and which should be also assigned to the scope named
     * {@code uma_protection}.
     *
     * @param gotoURL
     * @return returns with a redirection to the authorization server's authorization
     * endpoint with the necessary parameters to initiate an authorization code
     * flow with the {@code uma_protection} scope, and with the {@code redirection_uri},
     * that points to the {@link #handleAuthorizationResponse(java.lang.String, java.lang.String) response handler}.
     */
    @Path("obtainPAT")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response startPATforUserFlow(@QueryParam("goto") String gotoURL) {
        return Response.temporaryRedirect(
                URI.create(umaClient.getURLForObtainingUserPAT(gotoURL)))
                .build();
    }

    /**
     * This is the resource which receives the authorization code that is sent
     * by the authorization server.
     * @param code
     * @param state
     * @return 
     */
    @Path("responseHandler")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleAuthorizationResponse(
            @QueryParam("code") String code,
            @QueryParam("state") String state) {
        AccessToken accessToken = umaClient.getUserPATforUserWithAuthCode(code);
//        JsonObject tokenInfo = umaClient.introspectToken(UMAConstants.RESOURCE_SERVER_IDENTITY.getUserId(),
//                UMAConstants.RESOURCE_SERVER_SECRET, accessToken.getTokenId());
        OpenIDConnectClient openIDClient = new OpenIDConnectClient();
        JsonObject userInfo = openIDClient.getUserInfo(accessToken.getTokenId());
        OAuth2AccessToken token = new OAuth2AccessToken(
                Token.TokenType.PROTECTION_API_TOKEN,
                accessToken.getTokenId(),
                Identity.builder()
                .user()
                .realm("/")
                .userId(userInfo.getString("sub"))
                .build(), accessToken.getExpiresIn(),
                accessToken.getExpiresIn());

        TokenManager.getInstane().putProtectionApiTokenForUser(token);
        System.out.println("Token is put into token manager: " + token);

        try {
            return Response.temporaryRedirect(URI.create(state))
                    .build();
        } catch (RuntimeException ex) {
            return Response.ok("Success, PAT for user is " + accessToken + ", info : " + userInfo).build();
        }
    }

}
