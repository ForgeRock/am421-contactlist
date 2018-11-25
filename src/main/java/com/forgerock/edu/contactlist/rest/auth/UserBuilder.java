package com.forgerock.edu.contactlist.rest.auth;

import com.forgerock.edu.contactlist.entity.UserId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User builder class. Makes easier and more readable to create a user instance
 * in various ways. Follows builder pattern and fluent API.
 * @author vrg
 */
public class UserBuilder {
    private User user = new User(new UserId(null));

    public UserBuilder() {
        user.setActive(Boolean.TRUE);
    }
    
    public UserBuilder uid(String uid) {
        user.setUid(uid);
        return this;
    }
    public UserBuilder active(Boolean active) {
        user.setActive(active);
        return this;
    }
    public UserBuilder password(String password) {
        user.setPassword(password);
        return this;
    }
    
    public UserBuilder name(String name) {
        user.setName(name);
        return this;
    }
    public UserBuilder givenName(String givenName) {
        user.setGivenName(givenName);
        return this;
    }
    
    public UserBuilder familyName(String familyName) {
        user.setFamilyName(familyName);
        return this;
    }
    
    public UserBuilder email(String email) {
        user.setEmail(email);
        return this;
    }
    
    public UserBuilder privileges(Set<String> privileges) {
        user.setPrivileges(privileges);
        return this;
    }

    public UserBuilder privileges(String... privileges) {
        user.setPrivileges(new HashSet<>(Arrays.asList(privileges)));
        return this;
    }

    public User build() {
        return user;
    }
    
}
