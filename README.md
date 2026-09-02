# Product API - Java Backend Assignment

RESTful Product CRUD API built with **Java 17**, **Spring Boot 3**, **Spring Security (JWT + Refresh Token)**, **PostgreSQL**, **JUnit 5**, **Mockito**, and **Docker**.

## Features

- Full CRUD operations on Products with nested Items
- JWT authentication with refresh token rotation
- Role-based access control (`ROLE_ADMIN`, `ROLE_USER`)
- API versioning under `/api/v1/`
- Pagination on collection endpoints
- Standardized error responses
- Jakarta Validation on request DTOs
- Database indexing on frequently queried columns
- Async audit logging for product mutations
- CORS configuration and optional HTTPS enforcement
- OpenAPI/Swagger documentation
- Unit and integration tests with H2 in-memory database

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client (REST / Swagger)                  │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│  Controller Layer                                            │
│  AuthController  │  ProductController                         │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│  Service Layer                                               │
│  AuthService  │  ProductService  │  AuditService (@Async)   │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│  Repository Layer (Spring Data JPA)                          │
│  UserRepository │ ProductRepository │ ItemRepository        │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│  PostgreSQL Database                                         │
│  users │ product │ item │ refresh_tokens                     │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Responsibility |
|-------|----------------|
| **Controller** | HTTP routing, request validation, response mapping |
| **Service** | Business logic, transactions, audit events |
| **Repository** | Data access via JPA/Hibernate |
| **Security** | JWT filter, authentication, role-based authorization |
| **Exception Handler** | Consistent error response format |

## Tech Stack

- Java 17+
- Spring Boot 3.2
- Spring Data JPA (Hibernate)
- Spring Security + JWT (jjwt)
- PostgreSQL
- JUnit 5 + Mockito
- SpringDoc OpenAPI
- Docker & Docker Compose

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker & Docker Compose (for containerized setup)

## Quick Start with Docker

```bash
# Clone the repository
git clone <your-repo-url>
cd product-api

# Start PostgreSQL and the application
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

## Local Development Setup

### 1. Start PostgreSQL

```bash
docker run -d \
  --name productdb \
  -e POSTGRES_DB=productdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

### 3. Run Tests

```bash
mvn test
```

## Default Users

| Username | Password  | Role        |
|----------|-----------|-------------|
| admin    | admin123  | ROLE_ADMIN  |
| user     | user123   | ROLE_USER   |

> **Note:** Change default credentials in production.

## API Endpoints

### Authentication

| Method | Endpoint               | Description                    | Auth Required |
|--------|------------------------|--------------------------------|---------------|
| POST   | `/api/v1/auth/register`| Register new user              | No            |
| POST   | `/api/v1/auth/login`   | Login and get tokens           | No            |
| POST   | `/api/v1/auth/refresh` | Refresh tokens (with rotation) | No            |

### Products

| Method | Endpoint                      | Description              | Role Required |
|--------|-------------------------------|--------------------------|---------------|
| GET    | `/api/v1/products`            | List products (paginated)| ADMIN, USER   |
| GET    | `/api/v1/products/{id}`       | Get product by ID        | ADMIN, USER   |
| GET    | `/api/v1/products/{id}/items` | List product items       | ADMIN, USER   |
| POST   | `/api/v1/products`            | Create product           | ADMIN         |
| PUT    | `/api/v1/products/{id}`       | Update product           | ADMIN         |
| DELETE | `/api/v1/products/{id}`       | Delete product           | ADMIN         |

### Query Parameters (Pagination)

- `page` – Page number (default: 0)
- `size` – Page size (default: 10)
- `sortBy` – Sort field (default: createdOn)
- `sortDir` – Sort direction: asc/desc (default: desc)
- `search` – Filter products by name (optional)

## Authentication Flow

1. **Login** → Receive `accessToken` and `refreshToken`
2. **API calls** → Include header: `Authorization: Bearer <accessToken>`
3. **Token expiry** → Call `/api/v1/auth/refresh` with `refreshToken`
4. **Rotation** → Old refresh token is revoked; new pair is issued

## Example Requests

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Create Product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <accessToken>" \
  -d '{
    "productName": "Laptop",
    "items": [{"quantity": 5}, {"quantity": 10}]
  }'
```

### List Products

```bash
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=10" \
  -H "Authorization: Bearer <accessToken>"
```

## Swagger UI

OpenAPI documentation is available at:

```
http://localhost:8080/api/v1/swagger-ui.html
```

## Database Schema

```sql
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP NOT NULL,
    modified_by VARCHAR(100),
    modified_on TIMESTAMP
);

CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    quantity INT NOT NULL
);
```

Indexes are applied on `product_name`, `created_by`, and `item.product_id`.

## Error Response Format

```json
{
  "timestamp": "2026-09-01T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 99",
  "path": "/api/v1/products/99",
  "fieldErrors": null
}
```

## Environment Variables

| Variable              | Default     | Description                    |
|-----------------------|-------------|--------------------------------|
| DB_HOST               | localhost   | PostgreSQL host                |
| DB_PORT               | 5432        | PostgreSQL port                |
| DB_NAME               | productdb   | Database name                  |
| DB_USERNAME           | postgres    | Database username              |
| DB_PASSWORD           | postgres    | Database password              |
| JWT_SECRET            | (see yml)   | JWT signing key (min 32 chars)|
| JWT_ACCESS_EXPIRATION | 900000      | Access token TTL (ms)          |
| JWT_REFRESH_EXPIRATION| 604800000   | Refresh token TTL (ms)         |
| CORS_ALLOWED_ORIGINS  | localhost   | Comma-separated allowed origins|
| REQUIRE_SSL           | false       | Enforce HTTPS channel          |
| SSL_ENABLED           | false       | Enable server SSL              |

## Project Structure

```
src/main/java/com/zest/productapi/
├── config/          # Security, CORS, OpenAPI, Async config
├── controller/      # REST controllers
├── dto/             # Request/Response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions & global handler
├── mapper/          # Entity ↔ DTO mappers
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, UserDetails, JwtService
└── service/         # Business logic

src/test/java/       # Unit & integration tests
```

## License

This project was created as part of the Zest India IT Pvt Ltd technical assessment.
