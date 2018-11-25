package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.forgerock.opendj.ldap.EntryNotFoundException;
import org.forgerock.opendj.ldap.ErrorResultIOException;

/**
 * Maps ErrorResultIOException to a REST response:
 * <p>
 * <b>status: </b> {@code 409 Conflict}</p>
 * <p>
 * <b>ErrorType: </b> {@code CONSTRAINT_VIOLATION}</p>
 * <p>
 * <b>message: </b> {@code exception.getMessage()}</p>
 *
 * @author vrg
 */
@Provider
public class ErrorResultIOExceptionMapper implements ExceptionMapper<ErrorResultIOException> {

    @Override
    public Response toResponse(ErrorResultIOException exception) {
        if (exception.getCause() instanceof EntryNotFoundException) {
            EntryNotFoundException notFoundEx = (EntryNotFoundException) exception.getCause();
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMessage(
                            notFoundEx.getMessage(),
                            ErrorType.NOT_FOUND))
                    .build();

        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMessage(
                            exception.getMessage(),
                            ErrorType.INTERNAL_ERROR))
                    .build();
            
        }
    }

}
