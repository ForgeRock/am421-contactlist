package com.forgerock.edu.contactlist.util;

import com.forgerock.edu.contactlist.rest.exception.InvalidTokenIdException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * This class encapsulates the standard OpenID Connect communication. Its methods are using
 * the OpenAM's standard OpenID Connect defined REST endpoints to perform the actions.
 *
 * @author vrg
 */
public class OpenIDConnectClient {

    private final static Logger LOGGER = Logger.getLogger(OpenIDConnectClient.class.getName());
    private String userInfoEndpoint = null;
    private final JsonObject metaData;

    public final static String OPENID_DISCOVERY_ENDPOINT = 
            "http://login.example.com:18080/am/oauth2/.well-known/openid-configuration";

    public OpenIDConnectClient() {
        metaData = loadMetaData();
        userInfoEndpoint = metaData.getString("userinfo_endpoint");
    }
    
    private JsonObject loadMetaData() {
        try {
            LOGGER.log(Level.FINE, "Sending GET request to: {0}", OPENID_DISCOVERY_ENDPOINT);
            JsonObject discoveryResponse = ClientBuilder.newClient()
                    .target(OPENID_DISCOVERY_ENDPOINT)
                    .request(MediaType.APPLICATION_JSON)
                    .get(JsonObject.class);

            LOGGER.log(Level.INFO, "Response is: {0}", discoveryResponse);
            return discoveryResponse;
        } catch (BadRequestException bre) {
            // If the token is invalid, the rest call returns 400 Bad Request
            // and a JSON structure detailing the error condition 
            // retrive the detailed error message in JSON and log it
            JsonObject errorMsg = bre.getResponse().readEntity(JsonObject.class);
            LOGGER.log(Level.INFO, "OpenID Connect userinfo returned Bad Request: {0}", errorMsg);
            throw new RuntimeException("Unable to find OpenID server");
        }
    }       
    /**
     * Sends a GET request to OpenAM's {@code oauth2/userinfo} endpoint with
     * the given {@code accessToken} and returns with the JSON response. 
     * @param accessToken
     * @return JsonObject instance represents the raw response from OpenAM
     * @throws InvalidTokenIdException Thrown if the given access token is not valid.
     */
    public JsonObject getUserInfo(String accessToken) {

        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(userInfoEndpoint);
            LOGGER.log(Level.FINE, "Sending POST request to: {0}", target.getUri());

            JsonObject userinfoResponse = target
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .get(JsonObject.class);
            LOGGER.log(Level.INFO, "Userinfo response is: {0}", userinfoResponse);
            return userinfoResponse;
        } catch (BadRequestException bre) {
            // If the token is invalid, the rest call returns 400 Bad Request
            // and a JSON structure detailing the error condition 
            // retrive the detailed error message in JSON and log it
            JsonObject errorMsg = bre.getResponse().readEntity(JsonObject.class);
            LOGGER.log(Level.INFO, "OpenID Connect userinfo returned Bad Request: {0}", errorMsg);
            throw new InvalidTokenIdException("OAuth2 Access Token is not valid: " + errorMsg);
        }
    }    

}
