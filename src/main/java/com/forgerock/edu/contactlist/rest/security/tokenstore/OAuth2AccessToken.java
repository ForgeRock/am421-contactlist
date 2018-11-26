package com.forgerock.edu.contactlist.rest.security.tokenstore;

/**
 *
 * @author vrg
 */
public class OAuth2AccessToken extends GeneralToken {

    public OAuth2AccessToken(TokenType type, String tokenId, Identity identity, int maxInactiveIntervalInSeconds, int maxTTLinSeconds) {
        super(type, tokenId, identity, maxInactiveIntervalInSeconds, maxTTLinSeconds);
    }

    @Override
    public String toString() {
        return "OAuth2AccessToken" + super.toString(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
