package org.sifu.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for OrderQueryResource REST endpoints
 */
@QuarkusTest
class OrderQueryResourceTest {

    @Test
    @DisplayName("GET /api/orders - List orders with default pagination returns 200")
    void listOrders_default_returns200() {
        given()
                .when()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .body("content", is(notNullValue()))
                .body("page", is(notNullValue()))
                .body("size", is(notNullValue()));
    }

    @Test
    @DisplayName("GET /api/orders - List orders with custom pagination returns 200")
    void listOrders_customPagination_returns200() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .body("size", equalTo(10));
    }

    @Test
    @DisplayName("GET /api/orders - Page zero with size one returns 200")
    void listOrders_pageZeroSizeOne_returns200() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .body("size", equalTo(1));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Get order by valid ID returns 200")
    void getOrderById_validId_returns200() {
        String requestBody = """
            {
                "customerName": "Query Test",
                "customerEmail": "query@test.com"
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
                .get("/api/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId))
                .body("customerName", equalTo("Query Test"))
                .body("orderStatus", equalTo("PENDING"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Get order by non-existent ID returns 404")
    void getOrderById_notFound_returns404() {
        given()
                .when()
                .get("/api/orders/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Get order with invalid UUID returns 400")
    void getOrderById_invalidUUID_returns400() {
        given()
                .when()
                .get("/api/orders/not-a-uuid")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/items - Get items for order with no items returns 200")
    void getOrderItems_orderWithNoItems_returns200() {
        String requestBody = """
            {
                "customerName": "Items Test",
                "customerEmail": "items@test.com"
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
                .get("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/items - Get items for non-existent order returns 404")
    void getOrderItems_notFound_returns404() {
        given()
                .when()
                .get("/api/orders/00000000-0000-0000-0000-000000000000/items")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/orders/{orderId}/items - Get items with invalid UUID returns 400")
    void getOrderItems_invalidUUID_returns400() {
        given()
                .when()
                .get("/api/orders/invalid-id/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Response returns JSON content type")
    void returnsJsonContentType() {
        String requestBody = """
            {
                "customerName": "Content Type Test",
                "customerEmail": "content@test.com"
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
                .get("/api/orders/" + orderId)
                .then()
                .contentType(ContentType.JSON);
    }
}