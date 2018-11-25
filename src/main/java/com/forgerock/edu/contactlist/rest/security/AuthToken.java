package com.forgerock.edu.contactlist.rest.security;

import com.forgerock.edu.contactlist.rest.LocalDateTimeXmlAdapter;
import com.forgerock.edu.contactlist.rest.auth.User;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Auth token. Represents an authenticated user session. The session contains a
 * reference to the authenticated user and an expiration time. Has a unique id,
 * which is created during construction of AuthToken. This token also has an
 * expiration mechanism which is based on two factors: the finalExpiration is a
 * fixed time which is calculated during the construction of this token and the
 * maxInactiveIntervalInSeconds which is also passes to the constructor. Calling
 * the {@link #isValid() } method not just checks the validity of the token, but
 * also extends the lifetime of the token (maximum until the final expiration).
 *
 *
 * @author vrg
 */
@XmlRootElement
public class AuthToken {

    private final int maxInactiveIntervalInSeconds;

    private final LocalDateTime finalExpiration;

    private LocalDateTime expiresOn;

    private final User user;

    private final String tokenId;

    private static final TokenIdGenerator tokenIdGenerator = new TokenIdGenerator();

    /**
     * Creates a new token.
     *
     * @param tokenId Unique id for this token.
     * @param user authenticated user. Stored in token.
     * @param maxInactiveIntervalInSeconds in seconds
     * @param maxTTLinSeconds the max TTL in seconds. 0 means there is no final expiration.
     */
    public AuthToken(String tokenId, User user, int maxInactiveIntervalInSeconds, int maxTTLinSeconds) {
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
        this.tokenId = tokenId;
        this.user = user;
        this.finalExpiration = maxTTLinSeconds == 0 ? null : LocalDateTime.now().plusSeconds(maxTTLinSeconds);
        calculateNextExpiration();
    }

    /**
     * Creates a new token. Uses {@link TokenIdGenerator} to generate a unique
     * id for the created token.
     *
     * @param user authenticated user. Stored in token.
     * @param expirationTime in seconds
     */
    public AuthToken(User user, int expirationTime) {
        this(tokenIdGenerator.nextTokenId(), user, expirationTime, 0);
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

    /**
     * Checks whether the token is still valid. Calling this method also extends
     * the lifetime of the token with {@link #maxInactiveIntervalInSeconds}
     * seconds. The latest expiration time will never exceed the
     * {@link #finalExpiration}.
     *
     * @return true if the token is still valid.
     */
    public boolean isValid() {
        final LocalDateTime now = LocalDateTime.now();
        boolean valid = expiresOn.isAfter(now);
        if (valid) {
            calculateNextExpiration();
            valid = expiresOn.isAfter(now);
        }
        return valid;
    }

    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
    public LocalDateTime getExpiresOn() {
        return expiresOn;
    }

    public long getExpiresIn() {
        return Duration.between(LocalDateTime.now(), expiresOn).getSeconds();
    }

    public User getUser() {
        return user;
    }

    public String getTokenId() {
        return tokenId;
    }

}
