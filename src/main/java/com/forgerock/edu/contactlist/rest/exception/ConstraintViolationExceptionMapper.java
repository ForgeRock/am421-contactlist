package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.forgerock.opendj.ldap.ConstraintViolationException;

/**
 * Maps InvalidTokenIdException to a REST response: 
 * <p><b>status: </b> {@code 409 Conflict}</p>
 * <p><b>ErrorType: </b> {@code CONSTRAINT_VIOLATION}</p>
 * <p><b>message: </b> {@code exception.getMessage()}</p>
 * 
 * @author vrg
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>{
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorMessage(
                        exception.getMessage(), 
                        ErrorType.CONSTRAINT_VIOLATION))
                .build();
    }
    
}
