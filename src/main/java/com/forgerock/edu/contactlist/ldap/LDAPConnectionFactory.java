package com.forgerock.edu.contactlist.ldap;

import org.forgerock.opendj.ldap.Connection;

/**
 *
 * @author vrg
 */
public interface LDAPConnectionFactory {

    Connection getAnonymousConnection();

    Connection getConnection();

    void shutdown();
    
}
