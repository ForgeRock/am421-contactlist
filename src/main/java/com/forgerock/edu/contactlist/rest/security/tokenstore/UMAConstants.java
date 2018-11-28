package com.forgerock.edu.contactlist.rest.security.tokenstore;

/**
 *
 * @author vrg
 */
public class UMAConstants {

    public final static Identity RESOURCE_SERVER_IDENTITY
            = Identity.builder()
            .oauth2Client()
            .realm("/")
            .userId("ContactListBackend")
            .build();

    public final static String RESOURCE_SERVER_SECRET = "cangetinBackend";
    public final static String RESOURCE_SERVER_REDIRECT_URI = "http://app.test:8080/contactlist/rest/oauth2/responseHandler";
}
