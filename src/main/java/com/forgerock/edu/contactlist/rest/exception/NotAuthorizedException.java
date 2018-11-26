package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.uma.PermissionTicket;
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
    private final PermissionTicket permissionTicket;
    
    public NotAuthorizedException() {
        this.status = Status.FORBIDDEN;
        this.permissionTicket = null;
    }
    public NotAuthorizedException(PermissionTicket permissionTicket, String message) {
        super(message);
        status = Status.FORBIDDEN;
        this.permissionTicket = permissionTicket;
    }

    public NotAuthorizedException(String message) {
        super(message);
        status = Status.FORBIDDEN;
        this.permissionTicket = null;
    }
    
    public NotAuthorizedException(Status status, String message) {
        super(message);
        this.status = status;
        this.permissionTicket = null;
    }

    public Status getStatus() {
        return status;
    }

    public PermissionTicket getPermissionTicket() {
        return permissionTicket;
    }

}
