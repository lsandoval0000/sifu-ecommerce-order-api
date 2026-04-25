package org.sifu.clients;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.sifu.clients.filter.ForceJsonFilter;
import org.sifu.clients.model.ItfModel;

/**
 * REST client for the Financial API to retrieve ITF (Impuesto a las Transacciones Financieras) information.
 * Uses MicroProfile Rest Client for type-safe REST calls.
 */
@Path("itf")
@RegisterRestClient(configKey = "financial-api")
@RegisterProvider(ForceJsonFilter.class)
public interface FinancialApiRestClient {
    /**
     * Retrieves ITF information for a given amount.
     * @param amount the monetary amount to calculate ITF for
     * @return the ITF model containing the calculated ITF value
     */
    @GET
    @Path("{amount}.json")
    @Produces(MediaType.APPLICATION_JSON)
    ItfModel getItf(@PathParam("amount") Integer amount);
}
