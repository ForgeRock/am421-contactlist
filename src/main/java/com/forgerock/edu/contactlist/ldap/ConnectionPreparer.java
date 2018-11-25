package com.forgerock.edu.contactlist.ldap;

import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ErrorResultException;

/**
 *
 * @author vrg
 */
public interface ConnectionPreparer {
    public void prepare(Connection connection) throws ErrorResultException;
}
