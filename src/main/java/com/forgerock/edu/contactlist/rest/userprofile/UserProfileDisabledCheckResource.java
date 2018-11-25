package com.forgerock.edu.contactlist.rest.userprofile;

import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.dao.UserDAO;
import com.forgerock.edu.contactlist.entity.UserId;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.auth.User;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.forgerock.opendj.ldap.EntryNotFoundException;
import org.forgerock.opendj.ldap.ErrorResultException;

/**
 *
 * @author vrg
 */
@Path("isProfileDisabled/{uid}")
public class UserProfileDisabledCheckResource {

    private final LDAPConnectionFactory connectionFactory;

    public UserProfileDisabledCheckResource() {
        this(LDAPConnectionFactoryImpl.INSTANCE);
    }

    public UserProfileDisabledCheckResource(LDAPConnectionFactory connection) {
        this.connectionFactory = connection;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isProfileDisabled(@PathParam("uid") String userId) throws ErrorResultException {
        final UserDAO userDAO = new UserDAO(connectionFactory);
        try {
            User user = userDAO
                    .findById(new UserId(userId));
            if (user.getActive() == null || user.getActive()) {
                return false;
            } else {
                return true;
            }
        } catch (EntryNotFoundException ex) {
            userDAO.createUserByUID(userId);
            return false;
        }
    }
}
