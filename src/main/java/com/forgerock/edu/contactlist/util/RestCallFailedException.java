package com.forgerock.edu.contactlist.util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author vrg
 */
public class RestCallFailedException extends RuntimeException {
    private final int status;
    private final MultivaluedMap<String, Object> headers;
    private final Object response;
    private Class<?> responseType; 
    
    public RestCallFailedException(Response response, Class<?>responseType) {
        status = response.getStatus();
        headers = response.getHeaders();
        this.responseType = responseType;
        this.response = response.readEntity(responseType);
    }

    public int getStatus() {
        return status;
    }

    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    public <R> R getResponse() {
        return (R)response;
    }

    public Class<?> getResponseType() {
        return responseType;
    }

    @Override
    public String getMessage() {
        return "Status: " + status + ", headers: " + headers + ", response: " + response;
    }
    
    
    
    
}
