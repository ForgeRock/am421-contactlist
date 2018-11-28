package com.forgerock.edu.contactlist.rest.security.filter;

import com.forgerock.edu.contactlist.rest.security.ContactListSecurityContext;
import com.forgerock.edu.contactlist.rest.exception.NotAuthorizedException;
import com.forgerock.edu.contactlist.rest.security.tokenstore.TokenManager;
import com.forgerock.edu.contactlist.uma.PermissionRequest;
import com.forgerock.edu.contactlist.uma.PermissionTicket;
import com.forgerock.edu.contactlist.uma.UMAClient;
import java.io.IOException;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.glassfish.jersey.server.model.AnnotatedMethod;

/**
 *
 * @author vrg
 */
public class UmaRolesAllowedDynamicFeature implements DynamicFeature {

    private static final UMAClient umaClient = new UMAClient();
    private static final TokenManager tokenManager = TokenManager.getInstane();

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext configuration) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        // DenyAll on the method take precedence over RolesAllowed and PermitAll
        if (am.isAnnotationPresent(DenyAll.class)) {
            configuration.register(new RolesAllowedRequestFilter());
            return;
        }

        // RolesAllowed on the method takes precedence over PermitAll
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new RolesAllowedRequestFilter(ra.value()));
            return;
        }

        // PermitAll takes precedence over RolesAllowed on the class
        if (am.isAnnotationPresent(PermitAll.class)) {
            // Do nothing.
            return;
        }

        // DenyAll can't be attached to classes
        // RolesAllowed on the class takes precedence over PermitAll
        ra = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new RolesAllowedRequestFilter(ra.value()));
        }
    }

    @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
    private static class RolesAllowedRequestFilter implements ContainerRequestFilter {

        private final boolean denyAll;
        private final String[] rolesAllowed;

        RolesAllowedRequestFilter() {
            this.denyAll = true;
            this.rolesAllowed = null;
        }

        RolesAllowedRequestFilter(final String[] rolesAllowed) {
            this.denyAll = false;
            this.rolesAllowed = (rolesAllowed != null) ? rolesAllowed : new String[]{};
        }

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            if (!denyAll) {
                SecurityContext sc = requestContext.getSecurityContext();
                if (rolesAllowed.length > 0 && !isAuthenticated(requestContext)) {
                    throw new NotAuthorizedException(Response.Status.UNAUTHORIZED, "Authenticated session is required.");
                }

                for (final String role : rolesAllowed) {
                    if (sc.isUserInRole(role)) {
                        return;
                    }
                }
                if (sc instanceof ContactListSecurityContext) {
                    ContactListSecurityContext clsc = (ContactListSecurityContext) sc;
                    String resourceSetId = clsc.getResourceSetId();
                    if (resourceSetId != null) {
                        for (String role : rolesAllowed) {
                            if (role.startsWith("uma_")) {
                                String umaScopeName = role.substring(4);//cutting down "uma_" prefix
                                PermissionRequest permRequest
                                        = PermissionRequest.builder()
                                        .resourceSetId(resourceSetId)
                                        .addScope(umaScopeName)
                                        .build();
                                PermissionTicket ticket = null;
                                try {
                                    ticket = umaClient.permissionRequest(
                                            // tokenManager.getProtectionApiToken().getTokenId(),
                                            clsc.getResourceOwnerPAToken(),
                                            permRequest);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                if (ticket != null) {
                                    throw new NotAuthorizedException(ticket, "UMA RPT is missing...");
                                }

                            }

                        }
                    }
                }
            }

            throw new NotAuthorizedException("The operation is not permitted for this user.");
        }

        private static boolean isAuthenticated(final ContainerRequestContext requestContext) {
            return requestContext.getSecurityContext().getUserPrincipal() != null;
        }
    }
}
