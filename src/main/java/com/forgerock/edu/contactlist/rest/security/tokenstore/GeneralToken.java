package com.forgerock.edu.contactlist.rest.security.tokenstore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author vrg
 */
public class GeneralToken implements Token {

    private final Lock LOCK = new ReentrantLock();
    
    private final TokenType type;

    private final int maxInactiveIntervalInSeconds;

    private final LocalDateTime finalExpiration;

    private LocalDateTime expiresOn;

    private final String tokenId;

    private final Identity identity;

    private boolean invalidated = false;

    private final List<TokenStateListener> tokenStateListeners = new ArrayList<>();

    public GeneralToken(TokenType type, String tokenId, Identity identity, int maxInactiveIntervalInSeconds, int maxTTLinSeconds) {
        this.type = type;
        this.tokenId = tokenId;
        this.identity = identity;
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
        this.finalExpiration = maxTTLinSeconds == 0 ? null : LocalDateTime.now().plusSeconds(maxTTLinSeconds);
        calculateNextExpiration();
    }

    /**
     * Calculates the next expiration time. Two factors to calculate this: the
     * finalExpiration time and the maxInactiveIntervalInSeconds. The expiration
     * time is calculated like this:
     * {@code min(finalExpiration, now() + maxInactiveIntervalInSeconds)}
     *
     * @return {@code false} if the final expiration time elapsed, {@code true}
     * if the token's lifetime renewed normally.
     */
    private void calculateNextExpiration() {
        final LocalDateTime now = LocalDateTime.now();
        LocalDateTime inactiveIntervalExpiration = now.plusSeconds(maxInactiveIntervalInSeconds);
        if (finalExpiration == null) {
            expiresOn = inactiveIntervalExpiration;
        } else {
            this.expiresOn
                    = inactiveIntervalExpiration.isBefore(finalExpiration)
                    ? inactiveIntervalExpiration
                    : finalExpiration;
        }
    }

    @Override
    public TokenType getType() {
        return type;
    }
    
    @Override
    public String getTokenId() {
        return tokenId;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    /**
     * Checks whether the token is still valid. Calling this method also extends
     * the lifetime of the token with {@link #maxInactiveIntervalInSeconds}
     * seconds. The latest expiration time will never exceed the
     * {@link #finalExpiration}.
     *
     * @return true if the token is still valid.
     */
    @Override
    public boolean isValid() {
        try {
            LOCK.lock();
            invalidateIfExpired();
            if (!invalidated) {
                calculateNextExpiration();
            }
            return !invalidated;
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void invalidateIfExpired() {
        try {
            LOCK.lock();
            final LocalDateTime now = LocalDateTime.now();
            if (!expiresOn.isAfter(now)) {
                invalidate();
            }
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public LocalDateTime getExpiresOn() {
        return expiresOn;
    }

    @Override
    public long getExpiresIn() {
        return Duration.between(LocalDateTime.now(), expiresOn).getSeconds();
    }

    @Override
    public void addTokenStateListener(TokenStateListener tokenStateListener) {
        try {
            LOCK.lock();
            tokenStateListeners.add(tokenStateListener);
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void removeTokenStateListener(TokenStateListener tokenStateListener) {
        try {
            LOCK.lock();
            tokenStateListeners.remove(tokenStateListener);
        } finally {
            LOCK.unlock();
        }
    }

    private void fireTokenInvalidatedEvent(Token token) {
        tokenStateListeners.stream().forEach((tokenStateListener) -> {
            tokenStateListener.tokenInvalidated(token);
        });
    }

    @Override
    public void invalidate() {
        try {
            LOCK.lock();
            invalidated = true;
            fireTokenInvalidatedEvent(this);
            tokenStateListeners.clear();
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public String toString() {
        return "{" + "type=" + type + ", maxInactiveIntervalInSeconds=" + maxInactiveIntervalInSeconds + ", finalExpiration=" + finalExpiration + ", expiresOn=" + expiresOn + ", tokenId=" + tokenId + ", identity=" + identity + ", invalidated=" + invalidated + '}';
    }

}
