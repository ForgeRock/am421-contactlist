package com.forgerock.edu.contactlist.rest.exception;

/**
 *
 * @author vrg
 */
public class OptimisticLockException extends RuntimeException {

    private final String currentRevision;
    private final String sentRevision;

    public OptimisticLockException(String currentRevision, String sentRevision, String message) {
        super(message);
        this.currentRevision = currentRevision;
        this.sentRevision = sentRevision;
    }
    

    public String getCurrentRevision() {
        return currentRevision;
    }

    public String getSentRevision() {
        return sentRevision;
    }

}
