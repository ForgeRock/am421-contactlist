package com.forgerock.edu.contactlist.rest.exception;

/**
 * This exception signals that the username-password combination is invalid. 
 *
 * @author martfer
 */
public class IncorrectPasswordException extends RuntimeException {

    public IncorrectPasswordException() {
    }

    public IncorrectPasswordException(String message) {
        super(message);
    }

}
