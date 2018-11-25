package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.forgerock.opendj.ldap.EntryNotFoundException;

/**
 * Maps InvalidTokenIdException to a REST response: 
 * <p><b>status: </b> {@code 401 Unauthorized}</p>
 * <p><b>ErrorType: </b> {@code NOT_AUTHORIZED}</p>
 * <p><b>message: </b> {@code exception.getMessage()}</p>
 * 
 * @author vrg
 */
@Provider
public class EntryNotFoundExceptionMapper implements ExceptionMapper<EntryNotFoundException>{
    
    @Override
    public Response toResponse(EntryNotFoundException exception) {
        
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorMessage(
                        exception.getMessage(), 
                        ErrorType.NOT_FOUND))
                .build();
    }
    
}
