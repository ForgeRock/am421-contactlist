package com.forgerock.edu.contactlist.ldap;

import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.requests.BindRequest;
import org.forgerock.opendj.ldap.requests.Requests;

/**
 *
 * @author vrg
 */
public class ConnectionAnonymousBinder implements ConnectionPreparer {
    
    private final BindRequest request;

    public ConnectionAnonymousBinder() {
        request = Requests.newSimpleBindRequest();
    }
            
    @Override
    public void prepare(Connection connection) throws ErrorResultException {
        connection.bind(request);
    }
}
