package org.sifu.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.quarkus.arc.ArcUndeclaredThrowableException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.sifu.dto.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

import static org.sifu.exceptions.ErrorMessages.*;

/**
 * Global JAX-RS exception mapper for handling all exceptions.
 * Provides centralized error handling and converts exceptions to appropriate HTTP responses.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof JsonMappingException) {
            JsonMappingException jme = (JsonMappingException) exception;
            String message = jme.getMessage();
            if (jme instanceof MismatchedInputException) {
                MismatchedInputException mie = 
                    (MismatchedInputException) jme;
                if (mie.getTargetType() != null) {
                    message = String.format(ErrorMessages.MISSING_REQUIRED_FIELD, mie.getTargetType().getSimpleName());
                }
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .error(ErrorMessages.INVALID_JSON)
                            .details(List.of(message))
                            .build())
                    .build();
        }

        if (exception instanceof JsonProcessingException) {
            JsonProcessingException jpe = (JsonProcessingException) exception;
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .error(ErrorMessages.INVALID_JSON)
                            .details(List.of(jpe.getMessage()))
                            .build())
                    .build();
        }

        if (exception instanceof OrderNotFoundException ||
            exception instanceof ProductNotFoundException ||
            exception instanceof OrderItemNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.builder()
                            .error(exception.getMessage())
                            .build())
                    .build();
        }

        if (exception instanceof InsufficientStockException) {
            InsufficientStockException ise = (InsufficientStockException) exception;
            return Response.status(Response.Status.CONFLICT)
                    .entity(ErrorResponse.builder()
                            .error(ErrorMessages.INSUFFICIENT_STOCK)
                            .details(ise.getDetails())
                            .build())
                    .build();
        }

        if (exception instanceof InvalidStatusTransitionException ||
            exception instanceof BusinessRuleViolationException ||
            exception instanceof InvalidPathParameterException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .error(exception.getMessage())
                            .build())
                    .build();
        }
        
        if (exception.getClass().getName().contains("ConstraintViolation")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.builder()
                            .error("Validation failed")
                            .details(List.of(exception.getMessage()))
                            .build())
                    .build();
        }
        
        if (exception instanceof ArcUndeclaredThrowableException) {
            Throwable cause = exception.getCause();
            if (cause != null) {
                if (cause instanceof JsonMappingException) {
                    JsonMappingException jme = (JsonMappingException) cause;
                    String message = jme.getMessage();
                    if (jme instanceof MismatchedInputException) {
                        MismatchedInputException mie = 
                            (MismatchedInputException) jme;
                        if (mie.getTargetType() != null) {
                            message = String.format(ErrorMessages.MISSING_REQUIRED_FIELD, mie.getTargetType().getSimpleName());
                        }
                    }
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.builder()
                                    .error(ErrorMessages.INVALID_JSON)
                                    .details(List.of(message))
                                    .build())
                            .build();
                }

                if (cause instanceof JsonProcessingException) {
                    JsonProcessingException jpe = (JsonProcessingException) cause;
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.builder()
                                    .error(ErrorMessages.INVALID_JSON)
                                    .details(List.of(jpe.getMessage()))
                                    .build())
                            .build();
                }
                
                Throwable currentCause = cause;
                while (currentCause != null) {
                    if (currentCause instanceof JsonMappingException || currentCause instanceof JsonProcessingException) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(ErrorResponse.builder()
                                        .error(ErrorMessages.INVALID_JSON)
                                        .details(List.of(currentCause.getMessage()))
                                        .build())
                                .build();
                    }
                    currentCause = currentCause.getCause();
                }
                
                if (cause instanceof ConstraintViolationException) {
                    ConstraintViolationException cve = (ConstraintViolationException) cause;
                    List<String> details = cve.getConstraintViolations().stream()
                            .map(jakarta.validation.ConstraintViolation::getMessage)
                            .collect(Collectors.toList());
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.builder()
                                    .error(VALIDATION_FAILED)
                                    .details(details)
                                    .build())
                            .build();
                }

                if (cause.getMessage() != null && cause.getMessage().contains("null value")) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.builder()
                                    .error(INVALID_DATA)
                                    .details(List.of(cause.getMessage()))
                                    .build())
                            .build();
                }
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.builder()
                                .error(cause.getMessage())
                                .build())
                        .build();
            }
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse.builder()
                        .error(ErrorMessages.INTERNAL_SERVER_ERROR)
                        .details(List.of(UNEXPECTED_ERROR + exception.getClass().getName() + " - " + exception.getMessage()))
                        .build())
                .build();
    }
}