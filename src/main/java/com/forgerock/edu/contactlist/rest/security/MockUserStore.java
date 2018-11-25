package com.forgerock.edu.contactlist.rest.security;

import com.forgerock.edu.contactlist.rest.auth.User;
import com.forgerock.edu.contactlist.rest.security.UserStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample in-memory User store.
 *
 * @author vrg
 */
public class MockUserStore implements UserStore {

    private final Map<String, User> userByUid = new HashMap<>();

    public MockUserStore() {
        add(User.builder()
                .uid("peter")
                .password("cangetin")
                .givenName("Peter")
                .familyName("Demo")
                .email("peter@example.com")
                .name("Peter Demo")
                .privileges("phonebook://privileges/entries/read")
                .build());
        add(User.builder()
                .uid("vrg")
                .password("cangetin")
                .givenName("Peter")
                .familyName("Varga")
                .email("vrg@dpc.hu")
                .name("Peter Varga")
                .privileges("phonebook://privileges/entries/read")
                .build());
        add(User.builder()
                .uid("john")
                .password("cangetin")
                .givenName("John")
                .familyName("Demo")
                .email("john@example.com")
                .name("John Demo")
                .privileges("phonebook://privileges/entries/read",
                        "phonebook://privileges/entries/search")
                .build());
        add(User.builder()
                .uid("james")
                .password("cangetin")
                .givenName("James")
                .familyName("Demo")
                .email("james@example.com")
                .name("James Demo")
                .privileges("phonebook://privileges/entries/read",
                        "phonebook://privileges/entries/modify")
                .build());
    }

    private void add(User user) {
        userByUid.put(user.getUid(), user);
    }

    @Override
    public User findByUid(String uid) {
        return userByUid.get(uid);
    }

    @Override
    public boolean isPasswordMatching(String uid, String passwd) {
        return findByUid(uid).isPasswordMatching(passwd);
    }

}
