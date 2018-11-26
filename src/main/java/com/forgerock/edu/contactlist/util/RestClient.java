package com.forgerock.edu.contactlist.util;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

/**
 *
 * @author vrg
 */
public class RestClient {

    private final Logger logger;

    public RestClient(Logger logger) {
        this.logger = logger;
    }

    @FunctionalInterface
    public interface TargetCreator {

        public WebTarget createTarget(Client client);
    }

    @FunctionalInterface
    public interface InvocationBuilder {

        public Invocation.Builder buildInvocation(WebTarget target);
    }

    @FunctionalInterface
    public interface EntityBuilder<E> {

        public Entity<E> buildEntity();
    }

    @FunctionalInterface
    public interface ResponseHandler<E> {

        public E handle(Response response);
    }

    public <E> E sendRequest(String info, String httpMethod,
            TargetCreator targetCreator,
            InvocationBuilder builder,
            ResponseHandler<E> responseHandler) {
        WebTarget target = targetCreator.createTarget(ClientBuilder.newClient());
        logger.log(Level.INFO, "Sending {0} request to: {1}",
                new Object[]{httpMethod, target.getUri()});
        Response response = builder.buildInvocation(target)
                .method(httpMethod, Response.class);
        logger.log(Level.INFO, "{0} response is: {1}, headers: {2}",
                new Object[]{info, response.getStatusInfo(), response.getHeaders()});
        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            E successResponse = responseHandler.handle(response);
            logger.log(Level.INFO, "Successful response: {0}", successResponse);
            return successResponse;
        } else {
            throw new RestCallFailedException(response, JsonObject.class);
        }
    }

    public <E> E sendRequestWithBody(String info, String httpMethod,
            TargetCreator targetCreator,
            InvocationBuilder builder,
            EntityBuilder entityBuilder,
            ResponseHandler<E> responseHandler) {
        Entity entity = entityBuilder.buildEntity();
        Object payLoad = entity.getEntity();
        if (payLoad instanceof Form) {
            payLoad = ((Form) payLoad).asMap();
        }
        WebTarget target = targetCreator.createTarget(ClientBuilder.newClient());
        logger.log(Level.INFO, "Sending {0} request to: {1} with payload: {2}",
                new Object[]{httpMethod, target.getUri(), payLoad});
        Response response = builder.buildInvocation(target)
                .method(httpMethod, entity, Response.class);
        logger.log(Level.INFO, "{0} response is: {1}, headers: {2}",
                new Object[]{info, response.getStatusInfo(), response.getHeaders()});
        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            E successResponse = responseHandler.handle(response);
            logger.log(Level.INFO, "Successful response: {0}", successResponse);
            return successResponse;
        } else {
            throw new RestCallFailedException(response, JsonObject.class);
        }
    }

    public String calculateURL(TargetCreator targetCreator) {
        URI uri = targetCreator
                .createTarget(ClientBuilder.newClient())
                .getUri();
        logger.log(Level.INFO, "Calculated URI is {0}", uri);
        return uri.toString();
    }

}
