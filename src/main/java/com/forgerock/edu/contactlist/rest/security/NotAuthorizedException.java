package com.forgerock.edu.contactlist.rest.security;

import javax.ws.rs.core.Response.Status;

/**
 * This exception signals that the current user does not have the privilege 
 * to execute the operation.
 * Non-privileged users are just allowed to read/modify their own data.
 *
 * @author martfer
 */
public class NotAuthorizedException extends RuntimeException {

    private final Status status;
    
    public NotAuthorizedException() {
        this.status = Status.FORBIDDEN;
    }

    public NotAuthorizedException(String message) {
        super(message);
        status = Status.FORBIDDEN;
    }
    
    public NotAuthorizedException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
    
    

}
