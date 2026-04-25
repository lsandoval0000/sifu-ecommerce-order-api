package org.sifu.validators;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.sifu.exceptions.InvalidPathParameterException;

import java.util.UUID;

/**
 * Utility class for validating path parameters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathParamValidator {

    /**
     * Validates that a string is a valid UUID.
     * @param paramName the name of the parameter being validated
     * @param value the string value to validate
     * @return the parsed UUID
     * @throws InvalidPathParameterException if the value is null, blank, or not a valid UUID
     */
    public static UUID validateUUID(String paramName, String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPathParameterException(paramName, value, "UUID (e.g., 550e8400-e29b-41d4-a716-446655440000)");
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidPathParameterException(paramName, value, "UUID (e.g., 550e8400-e29b-41d4-a716-446655440000)");
        }
    }
}