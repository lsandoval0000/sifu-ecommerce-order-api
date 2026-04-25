package org.sifu.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.sifu.dto.AddItemRequest;
import org.sifu.dto.OrderItemDTO;
import org.sifu.service.CartService;
import org.sifu.validators.PathParamValidator;

/**
 * JAX-RS resource for order item (cart) operations.
 * Provides REST endpoints for adding and removing items from an order.
 */
@Path("/api/orders/{orderId}/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderItemResource {

    @Inject
    CartService cartService;

    /**
     * Adds an item to an order (shopping cart).
     * @param orderId the order ID
     * @param request the add item request
     * @return the added order item with HTTP 201 status
     */
    @POST
    public Response addItem(@PathParam("orderId") String orderId, @Valid AddItemRequest request) {
        OrderItemDTO item = cartService.addItem(PathParamValidator.validateUUID("orderId", orderId), request);
        return Response.status(Response.Status.CREATED)
                .entity(item)
                .build();
    }

    /**
     * Removes an item from an order (shopping cart).
     * @param orderId the order ID
     * @param itemId the item ID to remove
     * @return HTTP 204 no content response
     */
    @DELETE
    @Path("/{itemId}")
    public Response removeItem(@PathParam("orderId") String orderId, @PathParam("itemId") String itemId) {
        cartService.removeItem(
                PathParamValidator.validateUUID("orderId", orderId),
                PathParamValidator.validateUUID("itemId", itemId));
        return Response.noContent().build();
    }
}