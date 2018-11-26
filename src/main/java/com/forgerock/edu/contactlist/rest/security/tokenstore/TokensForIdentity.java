package com.forgerock.edu.contactlist.rest.security.tokenstore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author vrg
 */
public class TokensForIdentity implements TokenStateListener {

    private final Lock LOCK = new ReentrantLock();

    private Map<Token.TokenType, Token> tokenByType = new HashMap<>();

    public Token storeToken(Token token) {
        token.addTokenStateListener(this);
        try {
            LOCK.lock();
            return tokenByType.put(token.getType(), token);
        } finally {
            LOCK.unlock();
        }
    }

    public Token removeToken(Token token) {
        return removeToken(token.getType());
    }

    public Token removeToken(Token.TokenType type) {
        Token removed = null;
        try {
            LOCK.lock();
            removed = tokenByType.remove(type);
            return removed;
        } finally {
            LOCK.unlock();
            if (removed != null) {
                removed.removeTokenStateListener(this);
            }
        }
    }

    public Token getToken(Token.TokenType type) {
        try {
            LOCK.lock();
            return tokenByType.get(type);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void tokenInvalidated(Token token) {
        try {
            LOCK.lock();
            tokenByType.remove(token.getType());
        } finally {
            LOCK.unlock();
        }
    }
}
