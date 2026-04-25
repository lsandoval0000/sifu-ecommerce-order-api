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
 * Integration tests for OrderItemResource REST endpoints
 */
@QuarkusTest
class OrderItemResourceTest {

    @InjectMock
    OrderNotificationKafkaService orderNotificationKafkaService;

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Add item to pending order returns 201")
    void addItem_toPendingOrder_returns201() {
        String productBody = """
            {
                "name": "Test Product for Order",
                "price": 99.99,
                "stockQuantity": 10
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String orderBody = """
            {
                "customerName": "Test Customer",
                "customerEmail": "test@order.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID",
                "quantity": 2
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(201)
                .body("productId", equalTo(productId))
                .body("quantity", equalTo(2))
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Add item to confirmed order returns 400")
    void addItem_toConfirmedOrder_returns400() {
        String productBody = """
            {
                "name": "Product for Confirmed Order",
                "price": 149.99,
                "stockQuantity": 5
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String orderBody = """
            {
                "customerName": "Confirmed Order Test",
                "customerEmail": "confirmed@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        given()
                .when()
                .get("/api/orders/" + orderId + "/confirm")
                .then()
                .statusCode(200);
        
        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID",
                "quantity": 1
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Missing productId returns 400")
    void addItem_missingProductId_returns400() {
        String orderBody = """
            {
                "customerName": "Missing Product Test",
                "customerEmail": "missing@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        String itemBody = """
            {
                "quantity": 2
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Missing quantity returns 400")
    void addItem_missingQuantity_returns400() {
        String productBody = """
            {
                "name": "Product Missing Qty",
                "price": 49.99,
                "stockQuantity": 5
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String orderBody = """
            {
                "customerName": "Qty Test",
                "customerEmail": "qty@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID"
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Zero quantity returns 400")
    void addItem_zeroQuantity_returns400() {
        String productBody = """
            {
                "name": "Product Zero Qty",
                "price": 29.99,
                "stockQuantity": 5
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String orderBody = """
            {
                "customerName": "Zero Qty Test",
                "customerEmail": "zeroqty@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID",
                "quantity": 0
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Negative quantity returns 400")
    void addItem_negativeQuantity_returns400() {
        String productBody = """
            {
                "name": "Product Negative Qty",
                "price": 19.99,
                "stockQuantity": 5
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String orderBody = """
            {
                "customerName": "Negative Qty Test",
                "customerEmail": "negqty@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID",
                "quantity": -1
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Invalid order UUID returns 400")
    void addItem_invalidOrderUUID_returns400() {
        String productBody = """
            {
                "name": "Product Inv UUID",
                "price": 59.99,
                "stockQuantity": 5
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");

        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID",
                "quantity": 1
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/invalid-order-id/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/items - Invalid product UUID returns 400")
    void addItem_invalidProductUUID_returns400() {
        String orderBody = """
            {
                "customerName": "Inv Prod UUID",
                "customerEmail": "invprod@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        String itemBody = """
            {
                "productId": "invalid-product-id",
                "quantity": 1
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("DELETE /api/orders/{orderId}/items/{itemId} - Remove item from pending order returns 204")
    void removeItem_fromPendingOrder_returns204() {
        String productBody = """
            {
                "name": "Product to Remove",
                "price": 199.99,
                "stockQuantity": 10
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String orderBody = """
            {
                "customerName": "Remove Test",
                "customerEmail": "remove@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID",
                "quantity": 3
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        String itemId = given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        given()
                .when()
                .delete("/api/orders/" + orderId + "/items/" + itemId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("DELETE /api/orders/{orderId}/items/{itemId} - Remove item from confirmed order returns 204")
    void removeItem_fromConfirmedOrder_returns204() {
        String productBody = """
            {
                "name": "Product Remove Confirmed",
                "price": 249.99,
                "stockQuantity": 8
            }
            """;

        String productId = given()
                .contentType(ContentType.JSON)
                .body(productBody)
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .extract().path("id");

        String orderBody = """
            {
                "customerName": "Remove Confirmed Test",
                "customerEmail": "rmconfirmed@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        String itemBody = """
            {
                "productId": "REPLACE_WITH_PRODUCT_ID",
                "quantity": 1
            }
            """.replace("REPLACE_WITH_PRODUCT_ID", productId);

        String itemId = given()
                .contentType(ContentType.JSON)
                .body(itemBody)
                .when()
                .post("/api/orders/" + orderId + "/items")
                .then()
                .statusCode(201)
                .extract().path("id");
        
        given()
                .when()
                .get("/api/orders/" + orderId + "/confirm")
                .then()
                .statusCode(200);
        
        given()
                .when()
                .delete("/api/orders/" + orderId + "/items/" + itemId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("DELETE /api/orders/{orderId}/items/{itemId} - Non-existent item returns 404")
    void removeItem_nonExistentItem_returns404() {
        String orderBody = """
            {
                "customerName": "Non Existent Item Test",
                "customerEmail": "nonexistent@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when()
                .delete("/api/orders/" + orderId + "/items/00000000-0000-0000-0000-000000000001")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /api/orders/{orderId}/items/{itemId} - Invalid order UUID returns 400")
    void removeItem_invalidOrderUUID_returns400() {
        given()
                .when()
                .delete("/api/orders/invalid-order-id/items/00000000-0000-0000-0000-000000000001")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("DELETE /api/orders/{orderId}/items/{itemId} - Invalid item UUID returns 400")
    void removeItem_invalidItemUUID_returns400() {
        String orderBody = """
            {
                "customerName": "Inv Item UUID",
                "customerEmail": "invitem@test.com"
            }
            """;

        String orderId = given()
                .contentType(ContentType.JSON)
                .body(orderBody)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when()
                .delete("/api/orders/" + orderId + "/items/invalid-item-id")
                .then()
                .statusCode(400);
    }
}