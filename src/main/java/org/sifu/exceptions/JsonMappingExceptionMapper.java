package org.sifu.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.sifu.dto.ErrorResponse;

import java.util.List;

import static org.sifu.exceptions.ErrorMessages.INVALID_VALUE_FOR_FIELD;

/**
 * JAX-RS exception mapper for JSON mapping exceptions.
 * Converts JSON mapping errors into HTTP 400 Bad Request responses.
 */
@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    @Override
    public Response toResponse(JsonMappingException e) {
        String message = e.getMessage();
        
        if (e instanceof MismatchedInputException) {
            MismatchedInputException mie = (MismatchedInputException) e;
            if (mie.getTargetType() != null) {
                message = String.format(ErrorMessages.MISSING_REQUIRED_FIELD, mie.getTargetType().getSimpleName());
            }
        } else if (e instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) e;
            message = String.format(INVALID_VALUE_FOR_FIELD, ife.getValue(), ife.getPathReference());
        }
        
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse.builder()
                        .error(ErrorMessages.INVALID_JSON)
                        .details(List.of(message))
                        .build())
                .build();
    }
}