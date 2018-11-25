package com.forgerock.edu.contactlist.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB Mapped entity to send as a response body in successful scenarios.
 *
 * @see ErrorMessage
 * @author vrg
 */
@XmlRootElement
public class SuccessMessage {
    private String message;

    public SuccessMessage() {
    }

    public SuccessMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
