# Subscription Service

A microservice for managing users and their subscriptions to various digital services.

## Features

- User management (CRUD operations)
- Subscription management
- Top subscriptions analytics

## Tech Stack

- Java 17
- Spring Boot 3
- PostgreSQL
- Docker
- Flyway for database migrations
- OpenAPI/Swagger for API documentation

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 17 (for local development)
- Maven (for local development)

### Running with Docker

The easiest way to run the application is using Docker Compose:

```bash
# Clone the repository
git clone <repository-url>
cd subscription-service

# Start the application and database
docker-compose up -d

# Application will be available at http://localhost:8080/api
# Swagger UI will be available at http://localhost:8080/api/swagger-ui/index.html
```

### Running Locally

```bash
# Start PostgreSQL
docker-compose up -d postgres

# Run the application
./mvnw spring-boot:run
```

## API Endpoints

### User Management

- `POST /api/users` - Create a new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Subscription Management

- `POST /api/users/{userId}/subscriptions` - Add subscription to user
- `GET /api/users/{userId}/subscriptions` - Get user's subscriptions
- `DELETE /api/users/{userId}/subscriptions/{subscriptionId}` - Delete user's subscription
- `GET /api/subscriptions/top` - Get top 3 popular subscriptions

## Example Requests

### Create User

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Add Subscription

```bash
curl -X POST http://localhost:8080/api/users/1/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionTypeId": 1,
    "status": "ACTIVE"
  }'
```

## Development

### Database Migrations

Database migrations are handled automatically by Flyway on application startup.

### Building the Project

```bash
./mvnw clean package
``` 