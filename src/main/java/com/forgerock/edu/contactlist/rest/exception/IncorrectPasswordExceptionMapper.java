package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps IncorrectPasswordException to a REST response: 
 * <p><b>status: </b> {@code 406 NOT_ACCEPTABLE}</p>
 * <p><b>ErrorType: </b> {@code INCORRECT_PASSWORD}</p>
 * <p><b>message: </b> Incorrect password</p>
 *
 * @author vrg
 */
public class IncorrectPasswordExceptionMapper implements ExceptionMapper<IncorrectPasswordException>{

    @Override
    public Response toResponse(IncorrectPasswordException exception) {
        return Response
                .status(Response.Status.NOT_ACCEPTABLE)
                .entity(new ErrorMessage("Incorrect password", 
                        ErrorType.INCORRECT_PASSWORD)).build();
    }
    
}
