package org.sifu.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.sifu.dto.OrderDTO;
import org.sifu.dto.OrderItemDTO;
import org.sifu.dto.PageResponse;
import org.sifu.service.OrderQueryService;
import org.sifu.validators.PathParamValidator;

import java.util.List;

/**
 * JAX-RS resource for order query operations.
 * Provides REST endpoints for retrieving orders and order items with pagination support.
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderQueryResource {

    @Inject
    OrderQueryService orderQueryService;

    /**
     * Retrieves all orders with pagination.
     * @param page the page number (default 0)
     * @param size the page size (default 20)
     * @return paginated response of orders
     */
    @GET
    public PageResponse<OrderDTO> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return orderQueryService.findAll(page, size);
    }

    /**
     * Retrieves an order by ID.
     * @param id the order ID
     * @return the order with HTTP 200 status
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id) {
        OrderDTO order = orderQueryService.findById(PathParamValidator.validateUUID("id", id));
        return Response.ok(order).build();
    }

    /**
     * Retrieves the items of an order.
     * @param orderId the order ID
     * @return list of order items with HTTP 200 status
     */
    @GET
    @Path("/{orderId}/items")
    public Response getItems(@PathParam("orderId") String orderId) {
        List<OrderItemDTO> items = orderQueryService.getItems(PathParamValidator.validateUUID("orderId", orderId));
        return Response.ok(items).build();
    }
}