package com.forgerock.edu.contactlist.rest.exception;

import com.forgerock.edu.contactlist.rest.ErrorMessage;
import com.forgerock.edu.contactlist.rest.ErrorType;
import com.forgerock.edu.contactlist.uma.PermissionTicket;
import java.math.BigDecimal;
import javax.json.Json;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps InvalidTokenIdException to a REST response:
 * <p>
 * <b>status: </b> {@code 401 Unauthorized}</p>
 * <p>
 * <b>ErrorType: </b> {@code NOT_AUTHORIZED}</p>
 * <p>
 * <b>message: </b> {@code exception.getMessage()}</p>
 *
 * @author vrg
 */
@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        PermissionTicket permissionTicket = exception.getPermissionTicket();
        if (permissionTicket != null) {
            return Response.status(exception.getStatus())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Json.createObjectBuilder()
                            .add("message", exception.getMessage())
                            .add("ticket", permissionTicket.getTicket())
                            .build())
                    .build();
        }

        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorMessage(
                        exception.getMessage(),
                        ErrorType.NOT_AUTHORIZED))
                .build();
    }

}
