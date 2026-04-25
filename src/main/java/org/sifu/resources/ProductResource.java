package org.sifu.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.sifu.dto.CreateProductRequest;
import org.sifu.dto.PageResponse;
import org.sifu.dto.ProductDTO;
import org.sifu.dto.UpdateProductRequest;
import org.sifu.service.ProductService;
import org.sifu.validators.PathParamValidator;

/**
 * JAX-RS resource for product operations.
 * Provides REST endpoints for creating, retrieving, updating, and deleting products with pagination support.
 */
@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService productService;

    /**
     * Retrieves all products with pagination.
     * @param page the page number (default 0)
     * @param size the page size (default 20)
     * @return paginated response of products
     */
    @GET
    public PageResponse<ProductDTO> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return productService.findAll(page, size);
    }

    /**
     * Retrieves a product by ID.
     * @param id the product ID
     * @return the product with HTTP 200 status
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id) {
        ProductDTO product = productService.findById(PathParamValidator.validateUUID("id", id));
        return Response.ok(product).build();
    }

    /**
     * Creates a new product.
     * @param request the product creation request
     * @return the created product with HTTP 201 status
     */
    @POST
    public Response create(@Valid CreateProductRequest request) {
        ProductDTO product = productService.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(product)
                .build();
    }

    /**
     * Updates a product by ID.
     * @param id the product ID
     * @param request the product update request
     * @return the updated product with HTTP 200 status
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, @Valid UpdateProductRequest request) {
        ProductDTO product = productService.update(PathParamValidator.validateUUID("id", id), request);
        return Response.ok(product).build();
    }

    /**
     * Deletes (disables) a product by ID.
     * @param id the product ID
     * @return HTTP 204 no content response
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        productService.delete(PathParamValidator.validateUUID("id", id));
        return Response.noContent().build();
    }
}