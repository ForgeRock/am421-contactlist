package com.forgerock.edu.contactlist.rest;

/**
 * Error types sent in an error messages in REST responses.
 *
 * @see ErrorMessage
 * @author vrg
 */
public enum ErrorType {
    NOT_FOUND,
    MISSING_OR_INVALID_ACCESS_TOKEN,
    BAD_REQUEST,
    NOT_AUTHORIZED,
    CONSTRAINT_VIOLATION,
    OPTIMISTIC_LOCKING_VIOLATION,
    INTERNAL_ERROR,
    INCORRECT_PASSWORD
}
