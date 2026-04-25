# Sifu E-Commerce Order API

API REST basada en Quarkus para la gestión de pedidos de comercio electrónico con catálogo de productos, carrito de compras, procesamiento de pedidos e integración con Kafka para notificaciones. De forma básica a fin de mostrar las funcionalidades e integración.

## Stack Tecnológico

- **Framework**: Quarkus 3.34.1 (Jakarta EE)
- **Lenguaje**: Java 25
- **Base de Datos**: PostgreSQL (cliente reactivo) con Hibernate ORM
- **Herramienta de Build**: Maven
- **Validación**: Hibernate Validator
- **Mensajería**: Apache Kafka con Confluent Schema Registry (AVRO)
- **Pruebas**: JUnit 5, Mockito, REST Assured, H2 (en memoria)
- **Adicionales**: Lombok, MapStruct

## Requisitos Previos

- Java 25 o superior
- Maven 3.9+
- Base de datos PostgreSQL (o usar dev services)
- Apache Kafka (para funcionalidades de mensajería)
- Docker

---

## Ejecución de la Aplicación

### Docker

El proyecto cuenta con un archivo `docker-compose.yml` para la ejecución de la aplicación en un entorno Dockerizado. En dicho archivo se cuenta con lo siguiente:

- postgresql
- kafka
- schema-registry
- zookeeper
- control-center
- kafka-init (el cual realiza el registro del tópico)

> **Importante**: Antes de iniciar la aplicación en modo desarrollo, es necesario ejecutar el contenedor el siguiente comando:

```shell
docker-compose up -d
```

Para detener todos los servicios ejecutar:

```shell
docker-compose down -v
```

### Modo Desarrollo

Ejecuta la aplicación en modo desarrollo con soporte de live coding:

```shell
./mvnw quarkus:dev
```

La API estará disponible en: `http://localhost:8080`

> **Nota**: La Dev UI de Quarkus está disponible en modo desarrollo en `http://localhost:8080/q/dev/`.


## Estructura del Proyecto

```
src/
├── main/
│   ├── java/org/sifu/
│   │   ├── clients/           # REST clients
│   │   ├── config/            # Clases de configuración
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entities/          # Entidades JPA
│   │   ├── exceptions/        # Clases de excepción y mappers
│   │   ├── messaging/         # Servicios de Kafka
│   │   ├── repository/        # Repositorios de datos
│   │   ├── resources/         # Recursos REST JAX-RS
│   │   ├── service/           # Servicios de lógica de negocio
│   │   ├── domain/            # Lógica de dominio (máquina de estados, gestor de stock)
│   │   └── validators/        # Validadores personalizados
│   └── resources/
│       ├── application.properties
│       └── import.sql         # Datos iniciales (opcional)
└── test/
    ├── java/org/sifu/
    └── resources/
        └── application.properties  # Configuración de pruebas
```

---

## Endpoints de la API

### Tipo de Contenido

Todos los endpoints aceptan y retornan JSON:

```
Content-Type: application/json
```

---

## Productos

### 1. Listar Productos

Obtiene todos los productos con soporte de paginación.

**Endpoint**: `GET /api/products`

**Parámetros de Query**:

| Parámetro | Tipo | Por Defecto | Descripción |
|-----------|------|-------------|-------------|
| page | int | 0 | Número de página (0-indexado) |
| size | int | 20 | Número de elementos por página |

**Respuesta**: `200 OK`

```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Nombre del Producto",
      "stockQuantity": 100,
      "price": 29.99,
      "isEnabled": 1
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

**Restricciones**:
- `page` debe ser >= 0
- `size` debe ser > 0

---

### 2. Obtener Producto por ID

Obtiene un solo producto por su identificador único.

**Endpoint**: `GET /api/products/{id}`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID | Identificador único del producto |

**Respuesta**: `200 OK`

```json
{
  "id": "uuid",
  "name": "Nombre del Producto",
  "stockQuantity": 100,
  "price": 29.99,
  "isEnabled": 1
}
```

**Restricciones**:
- `id` debe ser un formato UUID válido
- El producto debe existir en la base de datos
- Retorna `404 Not Found` si el producto no existe

---

### 3. Crear Producto

Crea un nuevo producto en el catálogo.

**Endpoint**: `POST /api/products`

**Cuerpo de la Solicitud**:

```json
{
  "name": "Nombre del Producto",
  "stockQuantity": 100,
  "price": 29.99
}
```

**Campos Requeridos**:

| Campo | Tipo | Validación |
|-------|------|------------|
| name | String | Requerido, no puede estar vacío |
| stockQuantity | Integer | Requerido, debe ser positivo (> 0) |
| price | BigDecimal | Requerido, debe ser positivo (> 0) |

**Respuesta**: `201 Created`

```json
{
  "id": "uuid",
  "name": "Nombre del Producto",
  "stockQuantity": 100,
  "price": 29.99,
  "isEnabled": 1
}
```

**Restricciones**:
- Todos los campos son requeridos
- `stockQuantity` debe ser > 0
- `price` debe ser > 0
- Retorna `400 Bad Request` si la validación falla

---

### 4. Actualizar Producto

Actualiza la información de un producto existente.

**Endpoint**: `PUT /api/products/{id}`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID | Identificador único del producto |

**Cuerpo de la Solicitud** (todos los campos opcionales):

```json
{
  "name": "Nombre Actualizado",
  "stockQuantity": 150,
  "price": 39.99
}
```

**Respuesta**: `200 OK`

```json
{
  "id": "uuid",
  "name": "Nombre Actualizado",
  "stockQuantity": 150,
  "price": 39.99,
  "isEnabled": 1
}
```

**Restricciones**:
- `id` debe ser un formato UUID válido
- El producto debe existir
- Al menos un campo debe ser proporcionado
- `stockQuantity` y `price` deben ser positivos si se proporcionan
- Retorna `404 Not Found` si el producto no existe
- Retorna `400 Bad Request` si la validación falla

---

### 5. Eliminar (Desactivar) Producto

Elimina lógicamente un producto estableciéndolo como desactivado.

**Endpoint**: `DELETE /api/products/{id}`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID | Identificador único del producto |

**Respuesta**: `204 No Content`

**Restricciones**:
- `id` debe ser un formato UUID válido
- El producto debe existir
- No se puede desactivar un producto ya desactivado (retorna `400 Bad Request`)

---

## Pedidos

### 6. Listar Pedidos

Obtiene todos los pedidos con soporte de paginación.

**Endpoint**: `GET /api/orders`

**Parámetros de Query**:

| Parámetro | Tipo | Por Defecto | Descripción |
|-----------|------|-------------|-------------|
| page | int | 0 | Número de página (0-indexado) |
| size | int | 20 | Número de elementos por página |

**Respuesta**: `200 OK`

```json
{
  "content": [
    {
      "id": "uuid",
      "customerName": "Juan Pérez",
      "customerEmail": "juan@ejemplo.com",
      "subTotalAmount": 100.00,
      "igv": 18.00,
      "itf": 0.50,
      "totalAmount": 118.50,
      "orderStatus": "PENDING",
      "createdAt": "2024-01-15T10:30:00",
      "orderItems": [...]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

**Restricciones**:
- `page` debe ser >= 0
- `size` debe ser > 0

---

### 7. Obtener Pedido por ID

Obtiene un solo pedido por su identificador único.

**Endpoint**: `GET /api/orders/{id}`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID | Identificador único del pedido |

**Respuesta**: `200 OK`

```json
{
  "id": "uuid",
  "customerName": "Juan Pérez",
  "customerEmail": "juan@ejemplo.com",
  "subTotalAmount": 100.00,
  "igv": 18.00,
  "itf": 0.50,
  "totalAmount": 118.50,
  "orderStatus": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "orderItems": [
    {
      "id": "uuid",
      "productId": "uuid",
      "productName": "Nombre del Producto",
      "quantity": 2,
      "unitPrice": 50.00,
      "totalPrice": 100.00
    }
  ]
}
```

**Restricciones**:
- `id` debe ser un formato UUID válido
- El pedido debe existir en la base de datos
- Retorna `404 Not Found` si el pedido no existe

---

### 8. Crear Pedido

Crea un nuevo pedido con información del cliente.

**Endpoint**: `POST /api/orders`

**Cuerpo de la Solicitud**:

```json
{
  "customerName": "Juan Pérez",
  "customerEmail": "juan@ejemplo.com"
}
```

**Campos Requeridos**:

| Campo | Tipo | Validación |
|-------|------|------------|
| customerName | String | Requerido, no puede estar vacío |
| customerEmail | String | Requerido, debe ser un email válido |

**Respuesta**: `201 Created`

```json
{
  "id": "uuid",
  "customerName": "Juan Pérez",
  "customerEmail": "juan@ejemplo.com",
  "subTotalAmount": 0.00,
  "igv": 0.00,
  "itf": 0.00,
  "totalAmount": 0.00,
  "orderStatus": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "orderItems": []
}
```

**Restricciones**:
- `customerName` es requerido y no puede estar vacío
- `customerEmail` es requerido y debe ser un formato de email válido
- Retorna `400 Bad Request` si la validación falla
- Los pedidos creados comienzan con estado `PENDING` y lista de artículos vacía

---

### 9. Eliminar Pedido

Elimina (cancela) un pedido por su ID.

**Endpoint**: `DELETE /api/orders/{id}`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID | Identificador único del pedido |

**Respuesta**: `204 No Content`

**Restricciones**:
- `id` debe ser un formato UUID válido
- El pedido debe existir
- Solo los pedidos en estado `PENDING` pueden ser eliminados
- Retorna `400 Bad Request` si el pedido no está en estado `PENDING`
- Retorna `404 Not Found` si el pedido no existe

---

### 10. Confirmar Pedido

Confirma un pedido, reserva stock y envía una notificación a Kafka.

**Endpoint**: `GET /api/orders/{id}/confirm`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID | Identificador único del pedido |

**Respuesta**: `200 OK`

```json
{
  "id": "uuid",
  "customerName": "Juan Pérez",
  "customerEmail": "juan@ejemplo.com",
  "subTotalAmount": 100.00,
  "igv": 18.00,
  "itf": 0.50,
  "totalAmount": 118.50,
  "orderStatus": "CONFIRMED",
  "createdAt": "2024-01-15T10:30:00",
  "orderItems": [...]
}
```

**Restricciones**:
- `id` debe ser un formato UUID válido
- El pedido debe existir
- Solo los pedidos en estado `PENDING` pueden ser confirmados
- El stock es reservado para todos los artículos del pedido durante la confirmación
- Retorna `400 Bad Request` si el pedido no está en estado `PENDING`
- Retorna `404 Not Found` si el pedido no existe
- Retorna `409 Conflict` si no hay suficiente stock disponible

---

### 11. Cancelar Pedido

Cancela un pedido y libera el stock reservado.

**Endpoint**: `GET /api/orders/{id}/cancel`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | UUID | Identificador único del pedido |

**Respuesta**: `200 OK`

```json
{
  "id": "uuid",
  "customerName": "Juan Pérez",
  "customerEmail": "juan@ejemplo.com",
  "subTotalAmount": 100.00,
  "igv": 18.00,
  "itf": 0.50,
  "totalAmount": 118.50,
  "orderStatus": "CANCELLED",
  "createdAt": "2024-01-15T10:30:00",
  "orderItems": [...]
}
```

**Restricciones**:
- `id` debe ser un formato UUID válido
- El pedido debe existir
- Solo los pedidos en estado `PENDING` pueden ser cancelados
- El stock es liberado de vuelta al inventario si previamente fue confirmado
- Retorna `400 Bad Request` si el pedido no está en estado `PENDING`
- Retorna `404 Not Found` si el pedido no existe

---

### 12. Obtener Artículos del Pedido

Obtiene todos los artículos asociados a un pedido específico.

**Endpoint**: `GET /api/orders/{orderId}/items`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| orderId | UUID | Identificador único del pedido |

**Respuesta**: `200 OK`

```json
[
  {
    "id": "uuid",
    "productId": "uuid",
    "productName": "Nombre del Producto",
    "quantity": 2,
    "unitPrice": 50.00,
    "totalPrice": 100.00
  }
]
```

**Restricciones**:
- `orderId` debe ser un formato UUID válido
- El pedido debe existir
- Retorna `404 Not Found` si el pedido no existe

---

## Artículos del Pedido (Carrito)

### 13. Agregar Artículo al Pedido

Agrega un artículo de producto a un pedido (carrito de compras).

**Endpoint**: `POST /api/orders/{orderId}/items`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| orderId | UUID | Identificador único del pedido |

**Cuerpo de la Solicitud**:

```json
{
  "productId": "uuid",
  "quantity": 2
}
```

**Campos Requeridos**:

| Campo | Tipo | Validación |
|-------|------|------------|
| productId | UUID | Requerido, debe ser UUID válido |
| quantity | Integer | Requerido, debe ser positivo (> 0) |

**Respuesta**: `201 Created`

```json
{
  "id": "uuid",
  "productId": "uuid",
  "productName": "Nombre del Producto",
  "quantity": 2,
  "unitPrice": 50.00,
  "totalPrice": 100.00
}
```

**Restricciones**:
- `orderId` debe ser un formato UUID válido
- El pedido debe existir
- Solo los pedidos en estado `PENDING` pueden tener artículos agregados
- El producto debe existir y estar habilitado
- La cantidad debe ser > 0
- El stock es validado al momento de la agregación
- Retorna `400 Bad Request` si el pedido no está en estado `PENDING`
- Retorna `404 Not Found` si el pedido o producto no existe
- Retorna `409 Conflict` si no hay suficiente stock disponible

---

### 14. Eliminar Artículo del Pedido

Elimina un artículo de un pedido (carrito de compras).

**Endpoint**: `DELETE /api/orders/{orderId}/items/{itemId}`

**Parámetros de Path**:

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| orderId | UUID | Identificador único del pedido |
| itemId | UUID | Identificador único del artículo del pedido |

**Respuesta**: `204 No Content`

**Restricciones**:
- `orderId` debe ser un formato UUID válido
- `itemId` debe ser un formato UUID válido
- El pedido debe existir
- El artículo debe pertenecer al pedido especificado
- Solo los pedidos en estado `PENDING` pueden tener artículos eliminados
- Retorna `400 Bad Request` si el pedido no está en estado `PENDING`
- Retorna `404 Not Found` si el pedido o artículo no existe

---

## Máquina de Estados de Pedidos

La API implementa una máquina de estados para las transiciones de estado de los pedidos. Los siguientes estados están disponibles:

| Estado | Descripción |
|--------|-------------|
| PENDING | Pedido creado, esperando confirmación |
| CONFIRMED | Pedido confirmado, stock reservado |
| PROCESSING | Pedido está siendo procesado |
| SHIPPED | Pedido ha sido enviado |
| DELIVERED | Pedido entregado al cliente |
| CANCELLED | Pedido cancelado |
| REFUNDED | Pedido reembolsado |
| DELETED | Pedido eliminado (lógico) |

### Transiciones de Estado Válidas

- **PENDING** -> CONFIRMED, CANCELLED, DELETED
- **CONFIRMED** -> PROCESSING, CANCELLED, DELETED
- **PROCESSING** -> SHIPPED, DELETED
- **SHIPPED** -> DELIVERED, DELETED
- **DELIVERED** -> REFUNDED, DELETED
- **CANCELLED** -> DELETED
- **REFUNDED** -> DELETED
- **DELETED** -> (no se permiten transiciones)

---

## Respuestas de Error

La API retorna respuestas de error estandarizadas:

```json
{
  "error": "Mensaje de error",
  "details": ["Detalle 1", "Detalle 2"]
}
```

### Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200 | Éxito |
| 201 | Creado |
| 204 | Sin Contenido |
| 400 | Solicitud Incorrecta (error de validación, transición de estado inválida) |
| 404 | No Encontrado (recurso no existe) |
| 409 | Conflicto (violación de regla de negocio, ej. stock insuficiente) |
| 500 | Error Interno del Servidor |
