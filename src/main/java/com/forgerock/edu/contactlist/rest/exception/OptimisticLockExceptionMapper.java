package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.rest.ErrorType;
import com.forgerock.edu.contactlist.util.NullSafeJsonObjectBuilder;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps OptimisticLockException to a REST response:
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
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

    @Override
    public Response toResponse(OptimisticLockException exception) {
        JsonObject payload = new NullSafeJsonObjectBuilder()
                .withNulls()
                .add("message", exception.getMessage())
                .add("errorCode", ErrorType.OPTIMISTIC_LOCKING_VIOLATION.toString())
                .add("currentRevision", exception.getCurrentRevision())
                .add("sentRevision", exception.getSentRevision())
                .build();
        
        return Response.status(Response.Status.PRECONDITION_FAILED)
                .type(MediaType.APPLICATION_JSON)
                .entity(payload)
                .build();
    }

}
