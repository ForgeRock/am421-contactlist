package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.rest.security.NotAuthorizedException;
import com.forgerock.edu.contactlist.util.OpenAMClient;
import com.forgerock.edu.contactlist.util.OpenAMJsonObject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

/**
 * This filter evaluates policies with OpenAM and based on the response allows
 or denies the current request. This filter extracts the {@code tokenId} from
 * the current {@link ContactListSecurityContext}, and then calculates the
 * resource name from the current request path (adds the prefix {@code rest://}
 to it). Then sends a policy evaluation request to OpenAM (see {@link OpenAMClient#evaluatePolicies(java.lang.String, java.lang.String, java.lang.String)
 * OpenAMClient.evaluatePolicies}). If it turns out that the current request is not
 explicitly allowed by OpenAM, then throws a {@link NotAuthorizedException}
 * which will prevent the execution of the real REST resource: an HTTP 403 will
 * be the result immediately, where the exception's message will be passed to
 * the client.
 *
 * @see
 * @author vrg
 */
@Protected
@Priority(Priorities.AUTHORIZATION)
public class OpenAMPolicyEnforcementFilter implements ContainerRequestFilter {

    public final static Logger LOGGER = Logger.getLogger(OpenAMPolicyEnforcementFilter.class.getName());

    public void authorize(String tokenId, String path, String method) throws NotAuthorizedException {
        String resource = "rest://" + path;
        try {
            JsonArray evaluationResponse
                    = OpenAMClient.evaluatePolicies("ContactListREST", resource, tokenId);
            int entitlements = evaluationResponse.size();
            for (int i = 0; i < entitlements; i++) {
                JsonObject entitlement = evaluationResponse.getJsonObject(i);
                if (resource.equals(entitlement.getString("resource"))) {
                    JsonObject actionValues = entitlement.getJsonObject("actions");
                    
                    //reading the action value for the resource. If the action
                    //value is missing: it means an implicit deny decision.
                    boolean allowed = actionValues.getBoolean(method, false);
                    if (!allowed) {
                        JsonObject attributes = entitlement.getJsonObject("attributes");
                        // TODO Ch4L4Ex2: check if the deny decision is caused by maintenance mode
                        checkMaintenanceMode(new OpenAMJsonObject(attributes));
                        throw new NotAuthorizedException("Not authorized: " + attributes);
                    } else {
                        return;
                    }
                }
            }
            LOGGER.log(Level.WARNING, "No matching policies for: ", resource);

            throw new NotAuthorizedException("Not Authorized");

        } catch (BadRequestException bre) {
            JsonObject errorMsg = bre.getResponse().readEntity(JsonObject.class);
            LOGGER.log(Level.INFO, "Policy evaluation failed: {0}", errorMsg);
            throw new NotAuthorizedException("Not Authorized because policy evaluation is failed: " + errorMsg);
        }

    }

    /**
     * Checks whether the deny decision is because of a server maintenance mode.
     * The policies will expose a response attribute named
     * {@code maintenanceMode} with the value @{code "true"} if the deny
     * decision is because the server is in a maintenance mode. In this case
     * this method will throw a special NotAuthorizdException with a custom
     * message which contains the {@code maintenanceMessage}. This message is
     * also exposed by the policy engine in a response attribute).
     *
     * @param responseAttributes
     * @throws NotAuthorizedException
     */
    void checkMaintenanceMode(OpenAMJsonObject responseAttributes) throws NotAuthorizedException {

        boolean maintenanceMode = "true".equals(
                responseAttributes.getString("MaintenanceMode"));
        if (maintenanceMode) {
            //TODO Ch4L4Ex2: Read the response attribute named "maintenanceMessage" and throw a NotAuthorizedException that contains this message.
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.log(Level.FINE, "ResourceOwnerCalculatorFilter started");
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext instanceof ContactListSecurityContext) {
            LOGGER.log(Level.FINE, "Found ContactListSecurityContext");
            ContactListSecurityContext sc = (ContactListSecurityContext) securityContext;

            String tokenId = sc.getTokenId();
            String method = requestContext.getRequest().getMethod();
            String path = requestContext.getUriInfo().getPath();

            authorize(tokenId, path, method);
        } else {
            throw new NotAuthorizedException(Status.UNAUTHORIZED, 
                    "You need an authenticated session to use this functionality.");
        }
    }
}
