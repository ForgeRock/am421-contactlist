package com.forgerock.edu.contactlist.ldap;

import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ErrorResultException;

/**
 *
 * @author vrg
 */
public enum LDAPConnectionFactoryImpl implements LDAPConnectionFactory {

    INSTANCE;

    private final LDAPSettings SETTINGS = LDAPSettings.getInstance();

    private final org.forgerock.opendj.ldap.LDAPConnectionFactory factory;
    private final ConnectionManager connectionManager;
    private final ConnectionManager anonymousConnectionManager;

    private LDAPConnectionFactoryImpl() {
        try {
            factory = new org.forgerock.opendj.ldap.LDAPConnectionFactory(SETTINGS.getLdapHost(), SETTINGS.getLdapPort());
            connectionManager = new ConnectionManager(factory, new ConnectionBinder(SETTINGS.getLdapBindDN(), SETTINGS.getLdapBindPassword()));
            anonymousConnectionManager = new ConnectionManager(factory, new ConnectionAnonymousBinder());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            Connection conn = connectionManager.getConnection();
            return conn;
        } catch (ErrorResultException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public Connection getAnonymousConnection() {
        try {
            Connection conn = anonymousConnectionManager.getConnection();
            return conn;
        } catch (ErrorResultException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void shutdown() {
        connectionManager.closeAll();
        anonymousConnectionManager.closeAll();
    }

}
