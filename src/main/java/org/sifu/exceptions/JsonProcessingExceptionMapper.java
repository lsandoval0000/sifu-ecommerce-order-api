package org.sifu.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.sifu.dto.ErrorResponse;

import java.util.List;

/**
 * JAX-RS exception mapper for JSON processing exceptions.
 * Converts JSON processing errors into HTTP 400 Bad Request responses.
 */
@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    @Override
    public Response toResponse(JsonProcessingException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.builder()
                        .error(ErrorMessages.INVALID_JSON)
                        .details(List.of(e.getMessage()))
                        .build())
                .build();
    }
}