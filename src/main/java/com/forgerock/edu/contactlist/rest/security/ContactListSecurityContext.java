package com.forgerock.edu.contactlist.rest.security;

import com.forgerock.edu.contactlist.rest.auth.User;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.SecurityContext;

/**
 * Simple SecuirityContext implementation that wraps a
 * {@link ContactListPrincipal}.
 *
 * @author vrg
 */
public class ContactListSecurityContext implements SecurityContext {

    private final ContactListPrincipal userPrincipal;
    private final boolean secure;
    private boolean resourceOwner;
    private final String authenticationScheme;
    private Set<String> extraRoles = new HashSet<>();
    private String resourceSetId;

    public ContactListSecurityContext(ContactListPrincipal userPrincipal, boolean secure, String authenticationScheme) {
        this.userPrincipal = userPrincipal;
        this.secure = secure;
        this.authenticationScheme = authenticationScheme;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    /**
     * This method takes the wrapped {@link User} object's
     * {@link User#getPrivileges() privileges} set and checks whether the given
     * role is contained in this set or not. There is a special exception for
     * the role named {@code "resource-owner"}: in this case the response is the
     * value of {@link #resourceOwner} property.
     *
     * @see #setResourceOwner(boolean) 
     * @param role Role to check
     * @return true if the current user's privileges set contains the given
     * role.
     */
    @Override
    public boolean isUserInRole(String role) {
        return extraRoles.contains(role)
                || userPrincipal.getUser().getPrivileges().contains(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return authenticationScheme;
    }

    public void addExtraRole(String extraRole) {
        extraRoles.add(extraRole);
    }

    public boolean removeExtraRole(String extraRole) {
        return extraRoles.remove(extraRole);
    }
    
    public void removeExtraRoles() {
        extraRoles.clear();
    }

    public String getTokenId() {
        return userPrincipal.getTokenId();
    }

    public User getUser() {
        return userPrincipal.getUser();
    }

    public String getResourceSetId() {
        return resourceSetId;
    }

    public void setResourceSetId(String resourceSetId) {
        this.resourceSetId = resourceSetId;
    }

    public Set<String> getExtraRoles() {
        return Collections.unmodifiableSet(extraRoles);
    }
    
    
}
