package org.sifu.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for ProductResource REST endpoints
 */
@QuarkusTest
class ProductResourceTest {

    @Test
    @DisplayName("POST /api/products - Create product with valid data returns 201")
    void createProduct_validData_returns201() {
        String requestBody = """
            {
                "name": "Test Laptop Pro",
                "price": 1299.99,
                "stockQuantity": 10
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test Laptop Pro"))
                .body("price", equalTo(1299.99f))
                .body("stockQuantity", equalTo(10))
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("POST /api/products - Missing name returns 400")
    void createProduct_missingName_returns400() {
        String requestBody = """
            {
                "price": 99.99,
                "stockQuantity": 5
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/products - Missing stock quantity returns 400")
    void createProduct_missingStock_returns400() {
        String requestBody = """
            {
                "name": "Test Product",
                "price": 49.99
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/products - Missing price returns 400")
    void createProduct_missingPrice_returns400() {
        String requestBody = """
            {
                "name": "Test Product",
                "stockQuantity": 5
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/products - Negative price returns 400")
    void createProduct_negativePrice_returns400() {
        String requestBody = """
            {
                "name": "Negative Product",
                "sku": "NEG-001",
                "price": -10.00,
                "stockQuantity": 5
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/products - Zero stock quantity returns 400")
    void createProduct_zeroStock_returns400() {
        String requestBody = """
            {
                "name": "Zero Stock Product",
                "sku": "ZERO-001",
                "price": 29.99,
                "stockQuantity": 0
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/products - Null request body returns 400")
    void createProduct_nullBody_returns400() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/products - List products returns 200")
    void listProducts_returns200() {
        given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalElements", notNullValue());
    }

    @Test
    @DisplayName("GET /api/products - List products with pagination returns correct data")
    void listProducts_withPagination_returnsCorrectData() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(10));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Get existing product returns 200")
    void getProduct_existingProduct_returns200() {
        String requestBody = """
            {
                "name": "Get Test Product",
                "sku": "GET-TEST-001",
                "price": 199.99,
                "stockQuantity": 5
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Get Test Product"))
                .body("id", equalTo(productId));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Non-existent product returns 404")
    void getProduct_notFound_returns404() {
        given()
                .when()
                .get("/api/products/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Invalid UUID format returns 400")
    void getProduct_invalidUUID_returns400() {
        given()
                .when()
                .get("/api/products/not-a-uuid")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Update existing product returns 200")
    void updateProduct_existingProduct_returns200() {
        String requestBody = """
            {
                "name": "Original Name",
                "sku": "ORIG-001",
                "price": 99.99,
                "stockQuantity": 5
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");

        String updateBody = """
            {
                "name": "Updated Name",
                "stockQuantity": 10
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Name"))
                .body("stockQuantity", equalTo(10));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Update non-existent product returns 404")
    void updateProduct_notFound_returns404() {
        String updateBody = """
            {
                "name": "Should Fail",
                "stockQuantity": 10
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/products/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Invalid UUID format returns 400")
    void updateProduct_invalidUUID_returns400() {
        String updateBody = """
            {
                "name": "Test",
                "stockQuantity": 5
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/products/invalid-id")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Delete existing product returns 204")
    void deleteProduct_existingProduct_returns204() {
        String requestBody = """
            {
                "name": "To Delete",
                "sku": "DEL-001",
                "price": 49.99,
                "stockQuantity": 3
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when()
                .delete("/api/products/" + productId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Delete non-existent product returns 404")
    void deleteProduct_notFound_returns404() {
        given()
                .when()
                .delete("/api/products/00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Delete already deleted product returns 400")
    void deleteProduct_alreadyDeleted_returns400() {
        String requestBody = """
            {
                "name": "Double Delete",
                "sku": "DBL-001",
                "price": 29.99,
                "stockQuantity": 2
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        given()
                .when()
                .delete("/api/products/" + productId)
                .then()
                .statusCode(204);
        
        given()
                .when()
                .delete("/api/products/" + productId)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Invalid UUID format returns 400")
    void deleteProduct_invalidUUID_returns400() {
        given()
                .when()
                .delete("/api/products/invalid-id")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/products - Duplicate name returns 400")
    void createProduct_duplicateName_returns400() {
        String requestBody = """
            {
                "name": "Duplicate Name Test Product",
                "price": 99.99,
                "stockQuantity": 5
            }
            """;
        
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201);
        
        String requestBody2 = """
            {
                "name": "Duplicate Name Test Product",
                "price": 149.99,
                "stockQuantity": 3
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody2)
                .when()
                .post("/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /api/products - Empty list returns 200 with empty content")
    void listProducts_emptyList_returns200() {
        given()
                .queryParam("page", 100)
                .queryParam("size", 10)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("content.size()", equalTo(0));
    }
}