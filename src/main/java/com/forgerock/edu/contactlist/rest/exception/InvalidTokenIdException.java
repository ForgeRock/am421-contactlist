package com.forgerock.edu.contactlist.rest.exception;

/**
 * Signals that the token id does not exist or not valid anymore (e.g. timed out).
 *
 * @author martfer
 */
public class InvalidTokenIdException extends RuntimeException {

    public InvalidTokenIdException() {
    }

    public InvalidTokenIdException(String message) {
        super(message);
    }

}
