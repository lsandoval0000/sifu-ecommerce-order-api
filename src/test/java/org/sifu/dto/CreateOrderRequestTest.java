package org.sifu.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateOrderRequest DTO
 */
class CreateOrderRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid request passes validation")
        void validRequest_passesValidation() {
            CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@example.com");
            var violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Blank customer name fails validation")
        void blankName_failsValidation() {
            CreateOrderRequest request = new CreateOrderRequest("", "john@example.com");
            var violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")));
        }

        @Test
        @DisplayName("Null customer name fails validation")
        void nullName_failsValidation() {
            CreateOrderRequest request = new CreateOrderRequest(null, "john@example.com");
            var violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")));
        }

        @Test
        @DisplayName("Blank customer email fails validation")
        void blankEmail_failsValidation() {
            CreateOrderRequest request = new CreateOrderRequest("John Doe", "");
            var violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail")));
        }

        @Test
        @DisplayName("Null customer email fails validation")
        void nullEmail_failsValidation() {
            CreateOrderRequest request = new CreateOrderRequest("John Doe", null);
            var violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail")));
        }

        @Test
        @DisplayName("Invalid email format fails validation")
        void invalidEmailFormat_failsValidation() {
            CreateOrderRequest request = new CreateOrderRequest("John Doe", "not-an-email");
            var violations = validator.validate(request);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail")));
        }

        @Test
        @DisplayName("Valid email formats pass validation")
        void validEmailFormats_passValidation() {
            String[] validEmails = {
                    "test@example.com",
                    "user.name@domain.org",
                    "user+tag@domain.co.uk"
            };

            for (String email : validEmails) {
                CreateOrderRequest request = new CreateOrderRequest("Test User", email);
                var violations = validator.validate(request);
                assertTrue(violations.isEmpty(), "Email " + email + " should be valid");
            }
        }
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("All args constructor works")
        void allArgsConstructor() {
            CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@example.com");
            assertEquals("John Doe", request.getCustomerName());
            assertEquals("john@example.com", request.getCustomerEmail());
        }

        @Test
        @DisplayName("No args constructor creates empty instance")
        void noArgsConstructor() {
            CreateOrderRequest request = new CreateOrderRequest();
            assertNull(request.getCustomerName());
            assertNull(request.getCustomerEmail());
        }

        @Test
        @DisplayName("Setters and getters work")
        void settersAndGetters() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCustomerName("Jane Doe");
            request.setCustomerEmail("jane@example.com");

            assertEquals("Jane Doe", request.getCustomerName());
            assertEquals("jane@example.com", request.getCustomerEmail());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Email with subdomain passes validation")
        void emailWithSubdomain_passesValidation() {
            CreateOrderRequest request = new CreateOrderRequest("User", "user@mail.server.example.com");
            var violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Email with dots in local part passes validation")
        void emailWithDots_passesValidation() {
            CreateOrderRequest request = new CreateOrderRequest("User", "first.middle.last@example.com");
            var violations = validator.validate(request);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Email with localhost format considered valid by Jakarta validator")
        void emailWithLocalhost_passesValidation() {
            // Jakarta validator considers @domain valid even without TLD
            CreateOrderRequest request = new CreateOrderRequest("User", "user@domain");
            var violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Validator accepts domain without TLD");
        }
    }
}