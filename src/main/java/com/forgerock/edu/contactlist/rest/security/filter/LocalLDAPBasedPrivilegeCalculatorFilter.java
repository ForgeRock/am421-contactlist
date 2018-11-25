package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.dao.UserDAO;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import org.forgerock.opendj.ldap.ErrorResultException;

/**
 * This filter extracts the {@link ContactListSecurityContext} from the current
 * requestContext and replaces the privilege set based on the current session's
 * {@code selectedRole} property. This class assumes that the current tokenId is
 * a valid OpenAM tokenId an it tries to read the session property named
 * {@code selectedRole} from it. Based on the value of this property it
 * calculates the privilege set. See {@link #calculatePrivileges(java.lang.String)
 * }. .
 *
 * @see ContactListSecurityContext
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION + 2)
public class LocalLDAPBasedPrivilegeCalculatorFilter implements ContainerRequestFilter {

    public final static Logger LOGGER = Logger.getLogger(LocalLDAPBasedPrivilegeCalculatorFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "LocalLDAPBasedPrivilegeCalculatorFilter started");
        if (requestContext.getProperty(FilterContstants.IS_NEW_SESSION_KEY) == null) {
            return;
        }
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext instanceof ContactListSecurityContext) {
            LOGGER.log(Level.FINE, "Found ContactListSecurityContext");
            ContactListSecurityContext sc = (ContactListSecurityContext) securityContext;
            
            User user = sc.getUser();
            addPrivilegeSetBasedOnLocalUserStore(user);
        }
    }

    public void addPrivilegeSetBasedOnLocalUserStore(User user) {
        try {
            UserDAO userDAO = new UserDAO(LDAPConnectionFactoryImpl.INSTANCE);
            Set<String> privileges = userDAO.calculatePrivileges(user.getId());
            LOGGER.log(Level.INFO, "assigned privileges are {0}", privileges);
            user.setPrivileges(privileges);
        } catch (ErrorResultException ex) {
            LOGGER.log(Level.WARNING, ex,
                    () -> "Unable to read privileges of user "
                    + user.getId().getRDNAttributeValue() + " from LDAP");
        }

    }

}
