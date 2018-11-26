package com.forgerock.edu.contactlist.rest.security.tokenstore;

import java.util.HashMap;
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
 * {@link #findByTokenId(java.lang.String) }. Token expiration is managed
 * automatically by a background thread which periodically purges expired tokens
 * by calling {@link #cleanup() } method. Any token can be invalidated with {@link #removeToken(java.lang.String)
 * }.
 *
 * @author vrg
 */
public class TokenCache implements TokenStateListener {

    private final static Logger LOGGER = Logger.getLogger("contactlist.auth.UMATokenStore");

    private final Map<String, Token> tokenById = new HashMap<>();
    private final Map<Identity, TokensForIdentity> tokensByIdentity = new HashMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public TokenCache() {
        Timer timer = new Timer("TokenCache", true);
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                cleanup();
            }
        }, 60000, 60000);
    }

    /**
     * Finds and returns with an Token by token id.
     *
     * @param tokenId
     * @return Token if the token is found AND it is valid. If the token is not
     * in the store or it is expired, but still not purged, then this method
     * returns with null (and removes the given expired token silently).
     */
    public <T extends Token> T findByTokenId(String tokenId) {
        try {
            readLock.lock();
            T token = (T) tokenById.get(tokenId);
            if (token == null) {
                return null;
            } else if (token.isValid()) {
                return token;
            } else {
                return null;
            }
        } finally {
            readLock.unlock();
        }
    }

    public <T extends Token> T findByIdentityAndType(Identity identity, Token.TokenType tokenType) {
        T token = (T) getTokensForIdentity(identity).getToken(tokenType);
        if (token == null) {
            return null;
        } else if (token.isValid()) {
            return token;
        } else {
            return null;
        }
    }

    /**
     * Stores auth token in store.
     *
     * @param token
     * @return
     */
    public <T extends Token> T storeToken(T token) {
        token.addTokenStateListener(this);
        try {
            writeLock.lock();
            tokenById.put(token.getTokenId(), token);
            Identity identity = token.getIdentity();
            getTokensForIdentity(identity).storeToken(token);
            return token;
        } finally {
            writeLock.unlock();
        }
    }

    private TokensForIdentity getTokensForIdentity(Identity identity) {
        TokensForIdentity tokensForIdentity = tokensByIdentity.get(identity);
        if (tokensForIdentity == null) {
            tokensForIdentity = new TokensForIdentity();
            tokensByIdentity.put(identity, tokensForIdentity);
        }
        return tokensForIdentity;
    }

    /**
     * Cleans up store. Removes all expired tokens.
     */
    public void cleanup() {
        try {
            writeLock.lock();
            tokenById.values().stream()
                    .forEach((token) -> token.invalidateIfExpired());
            LOGGER.log(Level.INFO, "Cleanup finished, tokens remained: {0}", tokenById.size());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes token from store.
     *
     * @param tokenId
     * @return The removed token.
     */
    public Token removeToken(String tokenId) {
        try {
            writeLock.lock();
            Token token = findByTokenId(tokenId);
            if (token != null) {
                getTokensForIdentity(token.getIdentity()).removeToken(token);
                return tokenById.remove(tokenId);
            } else {
                return null;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void tokenInvalidated(Token token) {
        removeToken(token.getTokenId());
    }
}
