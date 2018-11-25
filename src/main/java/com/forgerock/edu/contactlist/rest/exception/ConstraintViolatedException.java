package com.forgerock.edu.contactlist.rest.exception;

/**
 *
 * @author vrg
 */
public class ConstraintViolatedException extends RuntimeException {

    public ConstraintViolatedException() {
    }

    public ConstraintViolatedException(String message) {
        super(message);
    }

    public ConstraintViolatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstraintViolatedException(Throwable cause) {
        super(cause);
    }

    public ConstraintViolatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
