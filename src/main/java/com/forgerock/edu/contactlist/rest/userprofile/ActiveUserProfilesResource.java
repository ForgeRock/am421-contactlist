package com.forgerock.edu.contactlist.rest.userprofile;

import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactoryImpl;
import com.forgerock.edu.contactlist.dao.UserDAO;
import com.forgerock.edu.contactlist.ldap.LDAPConnectionFactory;
import com.forgerock.edu.contactlist.rest.security.filter.Protected;
import com.forgerock.edu.contactlist.util.JsonUtil;
import javax.annotation.security.RolesAllowed;
import javax.json.JsonArray;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.ErrorResultIOException;
import org.forgerock.opendj.ldap.SearchResultReferenceIOException;

/**
 *
 * @author vrg
 */
@Path("activeProfiles")
@Protected
public class ActiveUserProfilesResource {

    private final LDAPConnectionFactory connectionFactory;

    public ActiveUserProfilesResource() {
        this(LDAPConnectionFactoryImpl.INSTANCE);
    }

    public ActiveUserProfilesResource(LDAPConnectionFactory connection) {
        this.connectionFactory = connection;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"contact-groups/read"})
    public JsonArray getUserList() throws ErrorResultException, ErrorResultIOException, SearchResultReferenceIOException {
        UserDAO userDAO = new UserDAO(connectionFactory);
        return userDAO
                .findAll()
                .filter(user -> user.getActive())
                .map(group -> group.getMinimalJsonObject())
                .collect(JsonUtil.toJsonArray());
    }
}
