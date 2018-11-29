package com.forgerock.edu.contactlist.util;

import com.forgerock.edu.contactlist.rest.auth.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * This class encapsulates the communication with OpenAM. Its methods are using
 * the OpenAM REST API to perform the actions.
 *
 * @author vrg
 */
public class OpenAMClient {

    private final static Logger LOGGER = Logger.getLogger(OpenAMClient.class.getName());

    public final static String OPENAM_URI = "http://login.example.com:18080/am";
    public final static String AUTHENTICATE_REST_ENDPOINT = OPENAM_URI + "/json/authenticate";
    public final static String POLICIES_REST_ENDPOINT = OPENAM_URI + "/json/policies";
    public final static String SESSIONS_REST_ENDPOINT = OPENAM_URI + "/json/sessions";
    public final static String USERS_REST_ENDPOINT = OPENAM_URI + "/json/users";

    /**
     * Validates tokenId against OpenAM and returns with the uid if it is
     * valid.
     *
     * @param tokenId
     * @return If the token is valid, returns with the {@code uid} assigned to
     * the session otherwise the return value is {@code null}.
     */
    public static String validateSessionAndExtractUserId(String tokenId) {
        Client client = ClientBuilder.newClient();
        Entity<JsonObject> entity = Entity.entity(
                Json.createObjectBuilder().build(), MediaType.APPLICATION_JSON);

        WebTarget target = client.target(SESSIONS_REST_ENDPOINT)
                .queryParam("_action", "validate");

        JsonObject validationResult = target
                .request(MediaType.APPLICATION_JSON)
                .header("iPlanetDirectoryPro", tokenId)
                .header("Accept-API-Version", "protocol=1.0,resource=2.0")
                .post(entity, JsonObject.class);

        LOGGER.log(Level.FINE, "session validation result: {0}", validationResult);

        boolean valid = validationResult.getBoolean("valid");

        if (valid) {
            return validationResult.getString("uid");
        } else {
            return null;
        }
    }

    /**
     * Gets some of the user profile attributes as a {@link User} object.
     * {@code cn}, {@code givenName}, {@code sn} and {@code mail} attributes are
     * queried from OpenAM.
     *
     * @param uid the users id to be queried
     * @param tokenId the sso tokenId that has the privilege to query the given
     * user's profile.
     * @return The user's attributes encapsulated into a {@link User} object.
     */
    public static User getUserProfile(String uid, String tokenId) {
        Client client = ClientBuilder.newClient();

        WebTarget target = client
                .target(USERS_REST_ENDPOINT)
                .path(uid);

        JsonObject userJSON = target
                .request(MediaType.APPLICATION_JSON)
                .header("iPlanetDirectoryPro", tokenId)
                .get(JsonObject.class);

        LOGGER.log(Level.FINE, "Get user profile result: {0}", userJSON);

        OpenAMJsonObject user = new OpenAMJsonObject(userJSON);

        String fullName = user.getString("cn");
        String givenName = user.getString("givenName");
        String familyName = user.getString("sn");
        String email = user.getString("mail");

        return User.builder()
                .uid(uid)
                .givenName(givenName)
                .familyName(familyName)
                .name(fullName)
                .email(email)
                .active(Boolean.TRUE)
                .build();
    }

    /**
     * Sends in a policy evaluation request to OpenAM with a single
     * resource.
     *
     * @param policySet The policy set's name. (formerly - policy application)
     * @param resource The resource's name.
     * @param tokenId The tokenId, which is the subject in this case and also
     * the policy evaluator. Note that the user assigned to this tokenId needs
     * to have the privilege to evaluate policies.
     * @return The raw response from OpenAM as a {@link JsonArray}.
     */
    public static JsonArray evaluatePolicies(String policySet, String resource, String tokenId) {
        Client client = ClientBuilder.newClient();
        JsonObject requestBody = Json.createObjectBuilder()
                .add("application", policySet)
                .add("resources", Json.createArrayBuilder()
                        .add(resource)).build();
        Entity<JsonObject> entity = Entity.entity(requestBody, MediaType.APPLICATION_JSON);

        WebTarget target = client.target(POLICIES_REST_ENDPOINT)
                .queryParam("_action", "evaluate");

        JsonArray evaluationResponse = target
                .request(MediaType.APPLICATION_JSON)
                .header("Accept-API-Version", "protocol=1.0,resource=2.0")
                .header("iPlanetDirectoryPro", tokenId)
                .post(entity, JsonArray.class);

        LOGGER.log(Level.INFO, "Policy evaluation response is: {0}", evaluationResponse);
        return evaluationResponse;
    }

    /**
     * Authenticates against OpenAM with the default authentication method
     * in the top level realm and passes in the given username and password
     * combination.
     *
     * @param username
     * @param password
     * @return The tokenId that is created - if the authentication was a
     * success.
     * @throws WebApplicationException If there was any kind of failure.
     */
    public static String authenticate(String username, String password) throws WebApplicationException {
        LOGGER.log(Level.FINE, "Trying to authenticate user {0}...", username);

        Client client = ClientBuilder.newClient();

        Entity<JsonObject> entity = Entity.entity(
                Json.createObjectBuilder().build(), MediaType.APPLICATION_JSON);

        WebTarget target = client.target(AUTHENTICATE_REST_ENDPOINT);

        JsonObject evaluationResponse = target
                .request(MediaType.APPLICATION_JSON)
                .header("X-OpenAM-Username", username)
                .header("X-OpenAM-Password", password)
                .header("Accept-API-Version", "resource=2.0, protocol=1.0")
                .post(entity, JsonObject.class);

        LOGGER.log(Level.FINE, "Authentication response is: {0}", evaluationResponse);
        String tokenId = evaluationResponse.getString("tokenId");
        LOGGER.log(Level.INFO, "Authenticated tokenId for user {0} is: {1}", new Object[]{username, tokenId});
        return tokenId;
    }

    /**
     * Gets the session property of a session represented by the given tokenId.
     *
     * @param tokenId The session that is queried for the given session
     * property.
     * @param propertyName The name of the session property.
     * @param privilegedTokenId The token, which is assigned to a user which has
     * the privilege to read (and write) the session properties.
     * @return Returns with the property value.
     */
    public static String getSessionProperty(String tokenId, String propertyName, String privilegedTokenId) {
        Client client = ClientBuilder.newClient();
        Entity<JsonObject> entity = Entity.entity(
                Json.createObjectBuilder()
                .add("properties",
                        Json.createArrayBuilder().add(propertyName))
                .build(), MediaType.APPLICATION_JSON);

        WebTarget target = client.target(SESSIONS_REST_ENDPOINT)
                .queryParam("_action", "getProperty")
                .queryParam("tokenId", tokenId);

        try {
            JsonObject result = target
                    .request(MediaType.APPLICATION_JSON)
                    .header("iPlanetDirectoryPro", privilegedTokenId)
                    .post(entity, JsonObject.class);

            LOGGER.log(Level.FINE, "GetSessionProperty raw result: {0}", result);

            return result.getString(propertyName);
        } catch (RuntimeException ex) {
            LOGGER.log(Level.FINE, "GetSessionProperty error", ex);
            return null;
        }
    }
}
