package com.forgerock.edu.contactlist.rest.security;

import com.forgerock.edu.contactlist.rest.auth.User;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread safe auth token store. Used to manage user sessions after successful
 * authentication. New token can be created with 
 * {@link #createTokenForUser(com.forgerock.edu.phonebook.rest.auth.User, int) },
 * then token can be found based on token id string using 
 * {@link #findByTokenId(java.lang.String) }. Token expiration is managed automatically
 * by a background thread which periodically purges expired tokens by calling {@link #cleanup() } method.
 * Any token can be invalidated with {@link #removeToken(java.lang.String) }.
 *
 * @author vrg
 */
public class AuthTokenStore {
    
    private final static Logger LOGGER = Logger.getLogger("contactlist.auth.AuthTokenStore");

    private final Map<String, AuthToken> tokenById = new HashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public AuthTokenStore() {
        Timer timer = new Timer("AuthTokenStoreCleanup", true);
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                cleanup();
            }
        }, 60000, 60000);
    }
    
    /**
     * Finds and returns with an AuthToken by token id.
     * @param tokenId
     * @return AuthToken if the token is found AND it is valid. If the token is
     * not in the store or it is expired, but still not purged, then this method
     * returns with null (and removes the given expired token silently). 
     */
    public AuthToken findByTokenId(String tokenId) {
        try {
            readLock.lock();
            AuthToken token = tokenById.get(tokenId);
            if (token == null) {
                return null;
            } else if (!token.isValid()) {
                tokenById.remove(token.getTokenId());
                return null;
            } else {
                return token;
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Creates and stores AuthToken.
     * @param user
     * @param expirationTime Token expiration time in seconds.
     * @return 
     */
    public AuthToken createTokenForUser(User user, int expirationTime) {
        return storeToken(new AuthToken(user, expirationTime));
    }

    /**
     * Stores auth token in store.
     * @param token
     * @return 
     */
    public AuthToken storeToken(AuthToken token) {
        try {
            writeLock.lock();
            tokenById.put(token.getTokenId(), token);
            return token;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Cleans up store. Removes all expired tokens.
     */
    public void cleanup() {
        try {
            writeLock.lock();
            for (Iterator<Map.Entry<String, AuthToken>> iterator = tokenById.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, AuthToken> entry = iterator.next();
                if (!entry.getValue().isValid()) {
                    iterator.remove();
                }
            }
            LOGGER.log(Level.INFO, "Cleanup finished, tokens remained: {0}", tokenById.size());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes token from store.
     * @param tokenId
     * @return The removed token.
     */
    public AuthToken removeToken(String tokenId) {
        try {
            writeLock.lock();
            AuthToken token = findByTokenId(tokenId);
            if (token != null) {
                return tokenById.remove(tokenId);
            } else {
                return null;
            }
        } finally {
            writeLock.unlock();
        }
    }
}
