package com.forgerock.edu.contactlist.rest.security;

import com.forgerock.edu.contactlist.rest.auth.User;
import java.security.Principal;

/**
 * Simple {@link Principal} implementation which wraps a {@link User} object and
 * the current {@code tokenId}.
 *
 * @author vrg
 */
public class ContactListPrincipal implements Principal {

    private final User user;

    private final String tokenId;

    public ContactListPrincipal(User user, String tokenId) {
        this.user = user;
        this.tokenId = tokenId;
    }

    /**
     * Returns with the wrapped user's {@link User#getUid() } method.
     *
     * @return
     */
    @Override
    public String getName() {
        return user.getUid();
    }

    public User getUser() {
        return user;
    }

    public String getTokenId() {
        return tokenId;
    }
}
