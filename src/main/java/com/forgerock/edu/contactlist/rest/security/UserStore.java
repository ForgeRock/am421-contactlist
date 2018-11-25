package com.forgerock.edu.contactlist.rest.security;

import com.forgerock.edu.contactlist.rest.auth.User;

/**
 *
 * @author vrg
 */
public interface UserStore {

    User findByUid(String uid);
    boolean isPasswordMatching(String uid, String passwd);
    
}
