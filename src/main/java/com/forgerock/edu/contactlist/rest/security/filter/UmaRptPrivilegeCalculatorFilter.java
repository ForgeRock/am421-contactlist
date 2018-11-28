package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.rest.security.tokenstore.TokenManager;
import com.forgerock.edu.contactlist.uma.UMAClient;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

//TODO Ch6L1Ex2: Read the javadoc and investigate the code
/**
 * This filter extracts the {@link ContactListSecurityContext} from the current
 * requestContext and adds extra roles to it based on the RPT token's content.
 * This class assumes that the current tokenId is an RPT token, and it is
 * inspected by using the UMA authorization server's token introspection
 * endpoint. The result is filtered by the current resource set id, and if there
 * are one or more scopes assigned to the current resource set id, then these
 * scopes are added to the current security context with a constant {@code uma_}
 * prefix. These roles can be referred to in the {@code @RolesAllowed}
 * annotations, or even in a
 * {@link SecurityContext#isUserInRole(java.lang.String) programmatic way}.
 *
 * @see ContactListSecurityContext
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHENTICATION + 12)
public class UmaRptPrivilegeCalculatorFilter implements ContainerRequestFilter {

    TokenManager tokenManager = TokenManager.getInstane();
    UMAClient umaClient = tokenManager.getUmaClient();

    public final static Logger LOGGER = Logger.getLogger(UmaRptPrivilegeCalculatorFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "SelectedRoleBasedPrivilegeCalculatorFilter started");
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext instanceof ContactListSecurityContext) {
            LOGGER.log(Level.FINE, "Found ContactListSecurityContext");
            ContactListSecurityContext sc = (ContactListSecurityContext) securityContext;
            String resourceSetId = sc.getResourceSetId();

            //if the current resource is shared
            if (resourceSetId != null) {
                String tokenId = sc.getTokenId();

                try {
                    JsonObject result = umaClient.introspectToken(sc.getResourceOwnerPAToken(), tokenId);
                    LOGGER.log(Level.INFO, "introspection result {0}", result);

                    if (result.getBoolean("active")) {
                        JsonArray jsonArray = result.getJsonArray("permissions");
                        jsonArray.stream()
                                .map((v) -> (JsonObject) v)
                                .filter((o) -> resourceSetId.equals(o.getString("resource_id")))
                                .flatMap((o) -> o.getJsonArray("resource_scopes").stream())
                                .map((v) -> ((JsonString) v).getString())
                                .forEach((scope) -> sc.addExtraRole("uma_" + scope));
                        LOGGER.log(Level.INFO, "Now the extra roles are these: {0}", sc.getExtraRoles());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
