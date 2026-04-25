package org.sifu.clients.filter;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.MediaType;

/**
 * JAX-RS client response filter that forces the Content-Type header to be application/json.
 * This is used to ensure that REST client responses are treated as JSON regardless of the server's Content-Type header.
 */
public class ForceJsonFilter implements ClientResponseFilter {
    /**
     * Filters the client response by setting the Content-Type header to application/json.
     * @param requestContext the client request context
     * @param responseContext the client response context
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        responseContext.getHeaders().putSingle("Content-Type", MediaType.APPLICATION_JSON);
    }
}
