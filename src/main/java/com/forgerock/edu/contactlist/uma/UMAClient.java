package com.forgerock.edu.contactlist.uma;

import com.forgerock.edu.contactlist.rest.security.tokenstore.Identity;
import com.forgerock.edu.contactlist.rest.security.tokenstore.Token;
import com.forgerock.edu.contactlist.rest.security.tokenstore.TokenManager;
import com.forgerock.edu.contactlist.rest.security.tokenstore.UMAConstants;
import com.forgerock.edu.contactlist.util.RestClient;
import com.forgerock.edu.contactlist.util.JsonUtil;
import java.io.StringReader;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class encapsulates the standard OpenID Connect communication. Its
 * methods are using the OpenAM's standard OpenID Connect defined REST endpoints
 * to perform the actions.
 *
 * @author vrg
 */
public class UMAClient {

    private final static Logger LOGGER = Logger.getLogger(UMAClient.class.getName());
    private String authorizationEndpoint = null;
    private String introspectionEndpoint = null;
    private String tokenEndpoint = null;
    private String resourceSetRegistrationEndpoint = null;
    private String permissionRegistrationEndpoint = null;
    private String rptEndpoint = null;
    private final JsonObject metaData;
    private final RestClient httpClient;

    public final static String UMA_DISCOVERY_ENDPOINT
            = "http://login.example.com:18080/openam/uma/.well-known/uma-configuration";

    public UMAClient() {
        httpClient = new RestClient(LOGGER);
        metaData = loadMetaData();
        authorizationEndpoint = metaData.getString("authorization_endpoint");
        introspectionEndpoint = metaData.getString("introspection_endpoint");
        tokenEndpoint = metaData.getString("token_endpoint");
        resourceSetRegistrationEndpoint = metaData.getString("resource_set_registration_endpoint");
        permissionRegistrationEndpoint = metaData.getString("permission_registration_endpoint");
        introspectionEndpoint = metaData.getString("introspection_endpoint");
        rptEndpoint = metaData.getString("rpt_endpoint");
    }

    private RuntimeException handleException(BadRequestException ex, String message) {
        JsonObject errorDetails = ex.getResponse().readEntity(JsonObject.class);
        LOGGER.log(Level.SEVERE, message, ex);
        LOGGER.log(Level.INFO, "Server returned with {0}", errorDetails);
        return new RuntimeException(message, ex);
    }

    private JsonObject loadMetaData() {
        return httpClient.sendRequest(
                "loadMetaData",
                "GET",
                // Defining headers
                (client) -> client.target(UMA_DISCOVERY_ENDPOINT),
                (builder) -> builder.request(MediaType.APPLICATION_JSON),
                (rawResponse) -> rawResponse.readEntity(JsonObject.class)
        );
    }

    public UMAResourceSet registerResourceSet(String protectionApiToken,
            UMAResourceSet resourceSet) {
        return httpClient.sendRequestWithBody(
                "registerResourceSet",
                "POST",
                // Defining target URL
                (client) -> client.target(resourceSetRegistrationEndpoint),
                // Defining headers
                (builder) -> {
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + protectionApiToken);
                },
                () -> { // Defining request body
                    return Entity.entity(resourceSet.getJsonObject(),
                            MediaType.APPLICATION_JSON);
                },
                (rawResponse) -> {
                    return UMAResourceSet.builder()
                    .resourceSet(resourceSet)
                    .addDataFromCreatedResponse(rawResponse.readEntity(JsonObject.class))
                    .build();
                }
        );
    }

    public PermissionTicket permissionRequest(String protectionApiToken,
            PermissionRequest permissionRequest) {
        return httpClient.sendRequestWithBody(
                "permissionRequest",
                "POST",
                // Defining target URL
                (client) -> client.target(permissionRegistrationEndpoint),
                // Defining headers
                (builder) -> {
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + protectionApiToken);
                },
                () -> { // Defining request body
                    return Entity.entity(permissionRequest.getJsonObject(),
                            MediaType.APPLICATION_JSON);
                },
                (rawResponse) -> { // Handling successful response
                    return PermissionTicket.builder()
                    .jsonState(rawResponse.readEntity(JsonObject.class))
                    .build();
                }
        );
    }

    public RequestingPartyToken authorizeRequest(String authorizationApiToken, PermissionTicket ticket) {
        return httpClient.sendRequestWithBody(
                "authorizeRequest",
                "POST",
                // Defining target URL
                (client) -> client.target(rptEndpoint),
                // Defining headers
                (builder) -> {
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + authorizationApiToken);
                },
                () -> { // Defining request body
                    return Entity.entity(ticket.getJsonObject(),
                            MediaType.APPLICATION_JSON);
                },
                (rawResponse) -> { // Handling successful response
                    return RequestingPartyToken.builder()
                    .jsonState(rawResponse.readEntity(JsonObject.class))
                    .build();
                }
        );
    }

    public List<String> getResourceSetIds(String protectionApiToken) {
        return httpClient.sendRequest(
                "getResourceSetIds",
                "GET",
                // Defining target URL
                (client) -> client.target(resourceSetRegistrationEndpoint), // Target URL
                (builder) -> { // Defining headers
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + protectionApiToken);
                },
                (rawResponse) -> { // Handling successful response
                    return rawResponse
                    .readEntity(JsonArray.class).stream()
                    .map(JsonUtil.jsonValueAsString())
                    .collect(Collectors.toList());
                }
        );

    }

    public UMAResourceSet getResourceSet(String protectionApiToken, String resourceSetId) {
        return httpClient.sendRequest(
                "getResourceSet",
                "GET",
                (client) -> { // Defining target URL
                    return client.target(resourceSetRegistrationEndpoint)
                    .path(resourceSetId);
                },
                (builder) -> { // Defining headers
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + protectionApiToken);
                },
                (rawResponse) -> { // Handling successful response
                    return UMAResourceSet.builder()
                    .revision(rawResponse.getHeaderString("ETag"))
                    .jsonState(rawResponse.readEntity(JsonObject.class))
                    .build();
                }
        );
    }

    public JsonObject introspectToken(String token) {
        return introspectToken(UMAConstants.RESOURCE_SERVER_IDENTITY.getUserId(), UMAConstants.RESOURCE_SERVER_SECRET, token);
    }
    
    public JsonObject introspectToken(String clientId, String clientSecret, String token) {
        String basicToken = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        return httpClient.sendRequest(
                "introspectToken",
                "GET",
                // Defining target URL
                (client) -> client.target(introspectionEndpoint)
                .queryParam("token", token),
                (builder) -> { // Defining headers
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Basic " + basicToken);
                },
                // Handling successful response
                (rawResponse) -> rawResponse.readEntity(JsonObject.class)
        );

    }

    public boolean unregisterResourceSet(String protectionApiToken, String resourceSetId, String eTag) {
        return httpClient.sendRequest(
                "unregisterResourceSet",
                "DELETE",
                (client) -> { // Defining target URL
                    return client.target(resourceSetRegistrationEndpoint)
                    .path(resourceSetId);
                },
                (builder) -> { // Defining headers
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + protectionApiToken)
                    .header("If-Match", eTag);
                },
                // Handling successful response
                (rawResponse) -> {
                    if (rawResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                        JsonObject responseJSON = rawResponse.readEntity(JsonObject.class);
                        LOGGER.log(Level.INFO, "unregisterResourceSet JSON response : {0}", responseJSON);
                    }
                    return true;
                }
        );
    }

    public AccessToken getAccessTokenWithResourceOwnerCredentials(
            String clientId, String clientSecret, String scope,
            String username, String password) {
        String basicToken = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());
        return httpClient.sendRequestWithBody(
                "getAccessTokenWithResourceOwnerCredentials",
                "POST",
                // Defining target URL
                (client) -> client.target(tokenEndpoint),
                // Defining headers
                (builder) -> {
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Basic " + basicToken);
                },
                () -> { // Defining request body
                    return Entity.form(new Form()
                            .param("grant_type", "password")
                            .param("username", username)
                            .param("password", password)
                            .param("scope", scope));
                },
                (rawResponse) -> { // Handling successful response
                    return AccessToken.builder()
                    .jsonState(rawResponse.readEntity(JsonObject.class))
                    .build();
                }
        );
    }

    public String getURLForObtainingUserPAT(String state) {
        String clientId = UMAConstants.RESOURCE_SERVER_IDENTITY.getUserId();
        String rediretURI = UMAConstants.RESOURCE_SERVER_REDIRECT_URI;
        return getURLForObtainingUserPAT(clientId, rediretURI, state);
    }

    public String getURLForObtainingUserPAT(String clientId, String redirectURI, String state) {
        return httpClient.calculateURL((client) -> client
                .target(authorizationEndpoint)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("nonce", "abc1234")
                .queryParam("state", state)
                .queryParam("scope", "uma_protection openid")
                .queryParam("redirect_uri", redirectURI));
    }

    public AccessToken getUserPATforUserWithAuthCode(String authCode) {
        String clientId = UMAConstants.RESOURCE_SERVER_IDENTITY.getUserId();
        String clientSecret = UMAConstants.RESOURCE_SERVER_SECRET;
        String rediretURI = UMAConstants.RESOURCE_SERVER_REDIRECT_URI;
        return getAccessTokenWithAuthCode(clientId, clientSecret, rediretURI, authCode);
    }

    public AccessToken getAccessTokenWithAuthCode(
            String clientId, String clientSecret, String redirectURI,
            String code) {
        String basicToken = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());
        return httpClient.sendRequestWithBody(
                "getAccessTokenWithAuthCode",
                "POST",
                // Defining target URL
                (client) -> client.target(tokenEndpoint),
                // Defining headers
                (builder) -> {
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Basic " + basicToken);
                },
                () -> { // Defining request body
                    return Entity.form(new Form()
                            .param("grant_type", "authorization_code")
                            .param("response_type", "id_token")
                            .param("code", code)
                            .param("redirect_uri", redirectURI));
                },
                (rawResponse) -> { // Handling successful response
                    return AccessToken.builder()
                    .jsonState(rawResponse.readEntity(JsonObject.class))
                    .build();
                }
        );
    }

    public AccessToken getAccessTokenWithClientCredentials(
            String clientId, String clientSecret, String scope) {
        String basicToken = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());
        return httpClient.sendRequestWithBody(
                "getClientCredentialsAccessToken",
                "POST",
                // Defining target URL
                (client) -> client.target(tokenEndpoint),
                // Defining headers
                (builder) -> {
                    return builder
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Basic " + basicToken);
                },
                () -> { // Defining request body
                    return Entity.form(new Form()
                            .param("grant_type", "client_credentials")
                            .param("scope", scope));
                },
                (rawResponse) -> { // Handling successful response
                    return AccessToken.builder()
                    .jsonState(rawResponse.readEntity(JsonObject.class))
                    .build();
                }
        );
    }

    public static void main(String[] args) {
        UMAClient umaClient = new UMAClient();
        AccessToken aatSimpleUser = umaClient
                .getAccessTokenWithResourceOwnerCredentials(
                        "ContactList", "cangetinFrontend", "uma_authorization", "simpleuser", "cangetin"
                );
//        AccessToken patSimpleUser = umaClient
//                .getAccessTokenWithResourceOwnerCredentials(
//                        "ContactListBackend", "cangetinBackend", "uma_protection", "simpleuser", "cangetin"
//                );
        AccessToken patSuperAdmin = umaClient
                .getAccessTokenWithResourceOwnerCredentials(
                        "ContactListBackend", "cangetinBackend", "uma_protection", "superadmin", "cangetin"
                );
//        AccessToken patSuperAdminCCB = umaClient
//                .getAccessTokenWithResourceOwnerCredentials(
//                        "ccb", "cangetinBackend", "uma_protection", "superadmin", "cangetin"
//                );
        System.out.println("\n\nAAT simpleuser:  " + aatSimpleUser + "\n\n");
        TokenManager tokenManager = new TokenManager(umaClient);
        
        Token pat = tokenManager.getProtectionApiToken();
//        JsonObject intro = umaClient.introspectToken(aatSimpleUser.getTokenId());
//        System.out.println("\n\n\n\n$$$$$$\n\n" + intro  + "\n\n");
       
//        AccessToken patToken = umaClient
//                .getAccessTokenWithClientCredentials(
//                        "ContactListBackend", "cangetinBackend", "uma_protection");
//        System.out.println("patToken: " + patToken);
//
        umaClient.getResourceSet(patSuperAdmin.getTokenId(), "e8bd85f7-25da-41dd-97d1-f578e9905f434");
        
        PermissionRequest pr = PermissionRequest.builder()
                .resourceSetId("e8bd85f7-25da-41dd-97d1-f578e9905f434")
                .addScope("manage")
                .build();
//
        PermissionTicket ticket = umaClient.permissionRequest(pat.getTokenId(), pr);
        System.out.println("\n\nAAT ticket:  " + ticket + "\n\n");
        
        umaClient.introspectToken("447d24e1-c85c-44dc-ba29-66d0fa79c5812");
        
//        RequestingPartyToken rpt = umaClient.authorizeRequest(aatSimpleUser.getTokenId(), ticket);
//        //List<String> resourceSetIds = umaClient.getResourceSetIds(patTokenForDemo.getTokenId());
//        JsonObject response = umaClient.introspectToken("ContactListBackend", "cangetinContactList",rpt.getRpt());
//        System.out.println("*****************************");
//        System.out.println("rpt: " + response);
//        response = umaClient.introspectToken("ContactListBackend", "cangetinContactList",aatSimpleUser.getTokenId());
//        System.out.println("*****************************");
//        System.out.println("aatSimpleUser: " + response);

//        List<String> resourceSetIds = umaClient.getResourceSetIds(patSuperAdmin.getTokenId());
//        resourceSetIds.stream()
//                .forEach((String resourceSetId)
//                        -> {
//                    UMAResourceSet resourceSet = umaClient.getResourceSet(patSuperAdmin.getTokenId(), resourceSetId);
////                    PermissionRequest permissionRequest
////                            = PermissionRequest.builder().resourceSet(resourceSet).build();
//                    try {
////                    System.out.println(" deleting resourceSet: " + resourceSet.getJsonObject());
////                        umaClient.unregisterResourceSet(patSuperAdminCCB.getTokenId(), resourceSetId, resourceSet.getRevision());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                });
//        UMAResourceSet resourceSet = umaClient.registerResourceSet(patSuperAdmin.getTokenId(),
//                UMAResourceSet.builder()
//                .type("ContactGroup")
//                .name("My Group")
//                .iconURI("http://app.test:18080/contactlist/images/contactgroup.png")
//                .scopes("view", "full-access")
//                .build());
//        UMAResourceSet retrievedRS = umaClient.getResourceSet(patSuperAdmin.getTokenId(), resourceSet.getId());

        //umaClient.unregisterResourceSet(patSuperAdmin.getTokenId(), resourceSet.getId(), retrievedRS.getRevision());
    }

}
