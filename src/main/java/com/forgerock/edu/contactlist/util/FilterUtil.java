package com.forgerock.edu.contactlist.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;

/**
 *
 * @author vrg
 */
public class FilterUtil {
    public final static String AUTHORIZATION_HEADER = "Authorization";
    
    private final static Logger LOGGER = Logger.getLogger(FilterUtil.class.getName());
    
    /**
     * Extracts bearer token from current request. This method tries to find
     * the bearer token in the {@code Authorization} header. The value must
     * be prefixed with the word {@code Bearer} and there must be whitespace
     * between Bearer and the token value. For example
     * {@code Authorization: Bearer abcdefg12345678}.
     * 
     * @param requestContext
     * @return The extracted token string or null, if it is missing.
     */
    public static String extractBearerToken(ContainerRequestContext requestContext) {

        String authorizationHeaderValue = requestContext.getHeaderString(AUTHORIZATION_HEADER);
        if (authorizationHeaderValue == null) {
            LOGGER.fine("Missing header: " + AUTHORIZATION_HEADER);
            return null;
        }
        LOGGER.log(Level.INFO, "Authorization header found: {0}", authorizationHeaderValue);

        // the Authorization token must follow the following syntax
        // Authorization: Bearer EEwJ6tF9x5...4599F
        String[] splitResult = authorizationHeaderValue.split("\\s+");
        if (splitResult.length != 2 || !"bearer".equalsIgnoreCase(splitResult[0])) {
            LOGGER.warning("Authorization header is not valid, does not contain the bearer token.");
            return null;
        }

        String tokenId = splitResult[1];

        return tokenId;
    }    
}
