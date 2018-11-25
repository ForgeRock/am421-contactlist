package com.forgerock.edu.contactlist.rest;

import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB Mapped entity to send as a response body in successful scenarios.
 *
 * @see ErrorMessage
 * @author vrg
 */
@XmlRootElement
public class CreatedMessage {
    private String message;
    private String createdURI;

    public CreatedMessage() {
    }

    public CreatedMessage(String message) {
        this.message = message;
    }

    public CreatedMessage(String message, URI createdURI) {
        this.message = message;
        this.createdURI = createdURI.toString();
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedURI() {
        return createdURI;
    }

    public void setCreatedURI(String createdURI) {
        this.createdURI = createdURI;
    }
}
