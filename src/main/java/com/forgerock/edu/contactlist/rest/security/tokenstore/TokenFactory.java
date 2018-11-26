package com.forgerock.edu.contactlist.rest.security.tokenstore;

/**
 *
 * @author vrg
 */
@FunctionalInterface
public interface TokenFactory {
    Token obtainToken();
}
