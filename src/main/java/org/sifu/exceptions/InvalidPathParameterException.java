package org.sifu.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a path parameter has an invalid format.
 */
@Getter
public class InvalidPathParameterException extends RuntimeException {

    private final String parameter;
    private final String value;
    private final String expectedFormat;

    public InvalidPathParameterException(String parameter, String value, String expectedFormat) {
        super(String.format(ErrorMessages.INVALID_PATH_PARAM, parameter, value, expectedFormat));
        this.parameter = parameter;
        this.value = value;
        this.expectedFormat = expectedFormat;
    }
}