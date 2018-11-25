package com.forgerock.edu.contactlist.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB Mapped entity to send as a response body in successful scenarios.
 *
 * @see ErrorMessage
 * @author vrg
 */
@XmlRootElement
public class SuccessfulUpdate extends SuccessMessage {
    private String newRevision;

    public SuccessfulUpdate() {
    }

    public SuccessfulUpdate(String message, String newRevision) {
        super(message);
        this.newRevision = newRevision;
    }

    public String getNewRevision() {
        return newRevision;
    }

    public void setNewRevision(String newRevision) {
        this.newRevision = newRevision;
    }
}
