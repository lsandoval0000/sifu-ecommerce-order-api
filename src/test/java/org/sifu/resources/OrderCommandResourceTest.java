package org.sifu.resources;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sifu.messaging.OrderNotificationKafkaService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for OrderCommandResource REST endpoints
 */
@QuarkusTest
class OrderCommandResourceTest {

    @InjectMock
    OrderNotificationKafkaService orderNotificationKafkaService;

    @Test
    @DisplayName("POST /api/orders - Create order with valid data returns 201")
    void createOrder_validData_returns201() {
        String requestBody = """
            {
                "customerName": "John Doe",
                "customerEmail": "john@example.com"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body("customerName", equalTo("John Doe"))
                .body("customerEmail", equalTo("john@example.com"))
                .body("orderStatus", equalTo("PENDING"))
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("POST /api/orders - Missing customer name returns 400")
    void createOrder_missingName_returns400() {
        String requestBody = """
            {
                "customerEmail": "john@example.com"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders - Missing customer email returns 400")
    void createOrder_missingEmail_returns400() {
        String requestBody = """
            {
                "customerName": "John Doe"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders - Invalid email format returns 400")
    void createOrder_invalidEmail_returns400() {
        String requestBody = """
            {
                "customerName": "John Doe",
                "customerEmail": "not-an-email"
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/orders/{id}/confirm - Confirm pending order returns 200")
    void confirmOrder_validId_returns200() {
        String requestBody = """
            {
                "customerName": "To Confirm",
                "customerEmail": "confirm@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        given()
                .when()
                .get("/api/orders/" + orderId + "/confirm")
                .then()
                .statusCode(200)
                .body("orderStatus", equalTo("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/orders/{id}/confirm - Non-existent order returns 404")
    void confirmOrder_notFound_returns404() {
        given()
                .when()
                .get("/api/orders/00000000-0000-0000-0000-000000000000/confirm")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/orders/{id}/cancel - Cancel pending order returns 200")
    void cancelOrder_pendingOrder_returns200() {
        String requestBody = """
            {
                "customerName": "To Cancel",
                "customerEmail": "cancel@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when()
                .get("/api/orders/" + orderId + "/cancel")
                .then()
                .statusCode(200)
                .body("orderStatus", equalTo("CANCELLED"));
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Delete existing order returns 204")
    void deleteOrder_existing_returns204() {
        String requestBody = """
            {
                "customerName": "To Delete",
                "customerEmail": "delete@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when()
                .delete("/api/orders/" + orderId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Non-existent order returns 404")
    void deleteOrder_notFound_returns404() {
        given()
                .when()
                .delete("/api/orders/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/orders/{id}/confirm - Invalid UUID format returns 400")
    void invalidUUID_confirm_returns400() {
        given()
                .when()
                .get("/api/orders/not-a-uuid/confirm")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Invalid UUID format returns 400")
    void invalidUUID_delete_returns400() {
        given()
                .when()
                .delete("/api/orders/invalid-id")
                .then()
                .statusCode(400);
    }
    
}