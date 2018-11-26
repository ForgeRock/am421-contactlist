package com.forgerock.edu.contactlist.rest.security.tokenstore;

import java.time.LocalDateTime;

/**
 *
 * @author vrg
 */
public interface Token {
    public enum TokenType {
        AUTHORIZATION_API_TOKEN,
        PROTECTION_API_TOKEN,
        UMA_TICKET,
        UMA_RPT,
        OPENAM_SSO_TOKEN,
        LOCAL_TOKEN
    }
    public TokenType getType();
    public String getTokenId();
    public Identity getIdentity();
    public boolean isValid();
    public LocalDateTime getExpiresOn();
    public long getExpiresIn();
    public void addTokenStateListener(TokenStateListener tokenStateListener);
    public void removeTokenStateListener(TokenStateListener tokenStateListener);
    public void invalidate();
    public void invalidateIfExpired();
}
