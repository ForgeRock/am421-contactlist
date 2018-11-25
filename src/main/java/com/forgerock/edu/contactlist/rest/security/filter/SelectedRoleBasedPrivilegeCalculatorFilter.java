package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.util.OpenAMClient;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

/**
 * This filter extracts the {@link ContactListSecurityContext} from the current
 * requestContext and replaces the privilege set based on the current session's
 * {@code selectedRole} property. This class assumes that the current tokenId is
 a valid OpenAM tokenId an it tries to read the session property named
 {@code selectedRole} from it. Based on the value of this property it
 * calculates the privilege set. See {@link #calculatePrivileges(java.lang.String)
 * }.
 *
 * @see ContactListSecurityContext
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION + 2)
public class SelectedRoleBasedPrivilegeCalculatorFilter implements ContainerRequestFilter {

    public final static Logger LOGGER = Logger.getLogger(SelectedRoleBasedPrivilegeCalculatorFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "SelectedRoleBasedPrivilegeCalculatorFilter started");
        if (requestContext.getProperty(FilterContstants.IS_NEW_SESSION_KEY) == null) {
            //If the session is coming from the cache, we are done.
            return;
        }
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext instanceof ContactListSecurityContext) {
            LOGGER.log(Level.FINE, "Found ContactListSecurityContext");
            ContactListSecurityContext sc = (ContactListSecurityContext) securityContext;
            String tokenId = sc.getTokenId();

            String selectedRole = getSelectedRole(tokenId);

            LOGGER.log(Level.INFO, "selectedRole is {0}", selectedRole);

            Set<String> privileges = calculatePrivileges(selectedRole);
            LOGGER.log(Level.INFO, "assigned privileges are {0}", privileges);

            User user = sc.getUser();
            user.setPrivileges(privileges);
        }
    }

    public String getSelectedRole(String tokenId) {
        //A caching mechnaism should be added here for the privilegedTokenId.
        try {
            String privilegedTokenId = OpenAMClient.authenticate("contactListBackend", "cangetinBackend");
            return OpenAMClient.getSessionProperty(tokenId, "selectedRole", privilegedTokenId);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE,"Could not read selectedRole Property",ex);
            return null;
        }
    }

    public Set<String> calculatePrivileges(final String selectedRole) {
        Set<String> privileges = new TreeSet<>();
        switch (selectedRole) {
            case "ContactReader":
                privileges.add("contact-groups/read");
                privileges.add("contacts/read");
                break;
            case "ContactAdmin":
                privileges.add("contact-groups/all");
                privileges.add("contact-groups/read");
                privileges.add("contact-groups/create");
                privileges.add("contact-groups/modify");
                privileges.add("contact-groups/delete");
                privileges.add("contacts/all");
                privileges.add("contacts/read");
                privileges.add("contacts/create");
                privileges.add("contacts/modify");
                privileges.add("contacts/delete");
                break;
            case "ProfileAdmin":
                privileges.add("users/all");
                privileges.add("users/read");
                privileges.add("users/create");
                privileges.add("users/modify");
                privileges.add("users/delete");
        }
        return privileges;
    }
}
