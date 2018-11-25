package com.forgerock.edu.contactlist.rest.security;

import com.forgerock.edu.contactlist.rest.security.UserStore;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.dao.UserDAO;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.auth.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.forgerock.opendj.ldap.ErrorResultException;

/**
 *
 * @author vrg
 */
public class LDAPUserStore implements UserStore {

    final LDAPConnectionFactory connectionFactory;
    private final UserDAO userDAO;

    public LDAPUserStore() {
        this(LDAPConnectionFactoryImpl.INSTANCE);
    }

    public LDAPUserStore(LDAPConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.userDAO = new UserDAO(connectionFactory);
    }

    @Override
    public User findByUid(String uid) {
        UserId userId = new UserId(uid);
        try {
            return userDAO.findByIdAndAddPrivileges(userId);
        } catch (ErrorResultException ex) {
            Logger.getLogger(LDAPUserStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean isPasswordMatching(String uid, String passwd) {
        return userDAO.isPasswordMatching(uid, passwd);
    }

}
