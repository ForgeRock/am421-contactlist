package com.forgerock.edu.contactlist.rest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

/**
 * Simple CORS filter that simply allows all origins to access this REST
 * interface
 *
 * @author vrg
 */
@Provider
@PreMatching
public class CORSResponseFilter implements ContainerResponseFilter {

    public final static Logger LOGGER = Logger.getLogger(CORSResponseFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
        LOGGER.log(Level.FINE, "Request: {0} {1}",
                new Object[]{requestCtx.getMethod(), requestCtx.getUriInfo().getRequestUri()});
        responseCtx.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseCtx.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseCtx.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        responseCtx.getHeaders().add("Access-Control-Allow-Headers", "Authorization, Content-Type, If-Match, If-None-Match, Access-Control-Allow-Headers");
        responseCtx.getHeaders().add("Access-Control-Expose-Headers", "WWW-Authenticate, Server-Authorization");
    }
}
