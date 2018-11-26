package com.forgerock.edu.contactlist.rest.security.tokenstore;

import com.forgerock.edu.contactlist.uma.UMAClient;

/**
 *
 * @author vrg
 */
public class TokenManager {

    private final TokenCache tokenCache = new TokenCache();
    private final UMAClient umaClient;
    private final TokenFactory patFactory;

    public TokenManager(UMAClient umaClient) {
        this.umaClient = umaClient;
        patFactory = new PATObtainer(umaClient);
    }
    
    public Token getProtectionApiToken() {
        Token patToken = tokenCache.findByIdentityAndType(
                UMAConstants.RESOURCE_SERVER_IDENTITY, 
                Token.TokenType.PROTECTION_API_TOKEN);
        if (patToken == null) {
            patToken = patFactory.obtainToken();
            tokenCache.storeToken(patToken);
        }
        return patToken;
    }

    public Token getProtectionApiTokenForUser(Identity user) {
        return tokenCache.findByIdentityAndType(
                user,
                Token.TokenType.PROTECTION_API_TOKEN);
    }

    public Token getTokenById(String tokenId) {
        return tokenCache.findByTokenId(tokenId);
    }

    public UMAClient getUmaClient() {
        return umaClient;
    }
    
    private final static TokenManager INSTANCE = new TokenManager(new UMAClient());

    public static TokenManager getInstane() {
        return INSTANCE;
    }

    public void putProtectionApiTokenForUser(Token token) {
        tokenCache.storeToken(token);
    }
    
    
}
