package com.forgerock.edu.contactlist.rest.security.tokenstore;

/**
 *
 * @author vrg
 */
@FunctionalInterface
public interface TokenStateListener {
    public void tokenInvalidated(Token token);
}
