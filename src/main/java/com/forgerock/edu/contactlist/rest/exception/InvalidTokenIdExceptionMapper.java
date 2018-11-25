package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps InvalidTokenIdException to a REST response: 
 * <p><b>status: </b> {@code 404 NOT_FOUND}</p>
 * <p><b>ErrorType: </b> {@code NOT_FOUND}</p>
 * <p><b>message: </b> Invalid Token Id</p>
 * 
 * @author vrg
 */
public class InvalidTokenIdExceptionMapper implements ExceptionMapper<InvalidTokenIdException>{

    @Override
    public Response toResponse(InvalidTokenIdException exception) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new ErrorMessage("Invalid Token Id", 
                        ErrorType.NOT_FOUND)).build();
    }
    
}
