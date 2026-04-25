package org.sifu.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.sifu.dto.CreateOrderRequest;
import org.sifu.dto.OrderDTO;
import org.sifu.messaging.OrderNotificationKafkaService;
import org.sifu.service.OrderCommandService;
import org.sifu.validators.PathParamValidator;

/**
 * JAX-RS resource for order command operations.
 * Provides REST endpoints for creating, deleting, confirming, canceling, and changing order status.
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderCommandResource {

    @Inject
    OrderCommandService orderCommandService;

    @Inject
    OrderNotificationKafkaService orderNotificationKafkaService;

    /**
     * Creates a new order.
     * @param request the order creation request
     * @return the created order with HTTP 201 status
     */
    @POST
    public Response create(@Valid CreateOrderRequest request) {
        OrderDTO order = orderCommandService.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(order)
                .build();
    }

    /**
     * Deletes an order by ID.
     * @param id the order ID
     * @return HTTP 204 no content response
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        orderCommandService.delete(PathParamValidator.validateUUID("id", id));
        return Response.noContent().build();
    }

    /**
     * Confirms an order by ID.
     * Reserves stock and sends a notification to Kafka.
     * @param id the order ID
     * @return the confirmed order
     */
    @GET
    @Path("/{id}/confirm")
    public Response confirm(@PathParam("id") String id) {
        OrderDTO order = orderCommandService.confirm(PathParamValidator.validateUUID("id", id));
        orderNotificationKafkaService.sendNotificationOnOrderConfirmation(order);
        return Response.ok(order).build();
    }

    /**
     * Cancels an order by ID.
     * Releases stock if the order was previously confirmed.
     * @param id the order ID
     * @return the cancelled order
     */
    @GET
    @Path("/{id}/cancel")
    public Response cancel(@PathParam("id") String id) {
        OrderDTO order = orderCommandService.cancel(PathParamValidator.validateUUID("id", id));
        return Response.ok(order).build();
    }
}