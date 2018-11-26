package com.forgerock.edu.contactlist.rest.security.tokenstore;

import com.forgerock.edu.contactlist.uma.AccessToken;
import com.forgerock.edu.contactlist.uma.UMAClient;

/**
 *
 * @author vrg
 */
public class PATObtainer implements TokenFactory {

    private UMAClient umaClient;

    public PATObtainer(UMAClient umaClient) {
        this.umaClient = umaClient;
    }

    @Override
    public Token obtainToken() {
        AccessToken accessToken = umaClient.getAccessTokenWithClientCredentials(
                UMAConstants.RESOURCE_SERVER_IDENTITY.getUserId(),
                UMAConstants.RESOURCE_SERVER_SECRET,
                "uma_protection");
        OAuth2AccessToken token = new OAuth2AccessToken(Token.TokenType.PROTECTION_API_TOKEN,
                accessToken.getTokenId(),
                UMAConstants.RESOURCE_SERVER_IDENTITY, accessToken.getExpiresIn(), accessToken.getExpiresIn());
        return token;
    }

}
