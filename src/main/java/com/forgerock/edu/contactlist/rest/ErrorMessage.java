package com.forgerock.edu.contactlist.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB Mapped entity to send as a response body in error scenarios.
 *
 * @see ErrorType
 * @author vrg
 */
@XmlRootElement
public class ErrorMessage {

    private String message;
    private ErrorType errorCode;

    public ErrorMessage() {
    }

    public ErrorMessage(String message, ErrorType errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorType getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorType errorCode) {
        this.errorCode = errorCode;
    }
    
    
    
}
