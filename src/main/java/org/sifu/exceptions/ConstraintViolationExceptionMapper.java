package org.sifu.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.sifu.dto.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-RS exception mapper for constraint validation exceptions.
 * Converts validation failures into HTTP 400 Bad Request responses.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.builder()
                        .error("Validation failed")
                        .details(details)
                        .build())
                .build();
    }
}