# ExpenseManager

UBS ExpenseManager: Employee Expense Management System. This project was developed for the UBS Trainee 2026 selection process.

## Features

- **Employee Management:** CRUD operations for employees.
- **Department Management:** CRUD operations for departments.
- **Expense Management:** CRUD operations for expenses, including image upload for receipts.
- **User Authentication:** Secure authentication using JWT.
- **Expense Alerts:** System for creating and managing expense-related alerts.
- **Currency Conversion:** Automatic currency conversion for expenses using a currency exchange gateway.
- **Image Storage:** Integration with Firebase for storing expense receipt images.

## Technologies Used

- **Backend:**
  - Java 21
  - Spring Boot 3
  - Spring Data JPA
  - Spring Web
  - Spring Security
- **Database:**
  - PostgreSQL
  - Flyway for database migrations
- **Authentication:**
  - JSON Web Tokens (JWT)
- **API Documentation:**
  - Swagger (OpenAPI)
- **Build & Dependency Management:**
  - Apache Maven
- **Containerization:**
  - Docker
- **Cloud Services:**
  - Firebase for image storage

## Getting Started

### Prerequisites

- Java 21
- Apache Maven
- Docker (optional, for running in a container)
- A PostgreSQL database

### Environment Variables

Before running the application, you need to set up the environment variables. Create a `.env` file in the `ExpenseManager` directory with the following content:

```
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/your-database
SPRING_DATASOURCE_USERNAME=your-username
SPRING_DATASOURCE_PASSWORD=your-password

# Firebase configuration (replace with your actual Firebase service account key)
FIREBASE_SERVICE_ACCOUNT_KEY='{ "type": "service_account", "project_id": "...", ... }'
```

### Running Locally

1.  Navigate to the `ExpenseManager` directory.
2.  Set the environment variables. For Linux/macOS:
    ```bash
    set -a
    source .env
    set +a
    ```
    For Windows (PowerShell):
    ```powershell
    $env:SPRING_PROFILES_ACTIVE="dev"
    $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/your-database"
    $env:SPRING_DATASOURCE_USERNAME="your-username"
    $env:SPRING_DATASOURCE_PASSWORD="your-password"
    $env:FIREBASE_SERVICE_ACCOUNT_KEY='{ "type": "service_account", "project_id": "...", ... }'
    ```
3.  Run the application using Maven:
    ```bash
    ./mvnw spring-boot:run
    ```

### Running with Docker

1.  Make sure you have Docker and Docker Compose installed.
2.  Navigate to the `ExpenseManager` directory.
3.  Create the `.env` file as described above.
4.  Run the application using Docker Compose:
    ```bash
    docker-compose up --build
    ```
    This will build the Docker image and start the application and a PostgreSQL database container.

## API Documentation

The API documentation is generated using Swagger/OpenAPI. Once the application is running, you can access it at:

[http://localhost:8080/swagger.html](http://localhost:8080/swagger.html)

The OpenAPI specification in JSON format is available at:

[http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Database Migrations

Database migrations are managed using Flyway. The migration scripts are located in `src/main/resources/db/migration`. Flyway automatically applies the migrations on application startup.

## Project Structure

```
.
├── src
│   ├── main
│   │   ├── java/com/ubs/ExpenseManager
│   │   │   ├── config          # Spring configuration files
│   │   │   ├── controllers     # REST API controllers
│   │   │   ├── entities        # JPA entities
│   │   │   ├── exception       # Custom exception handlers
│   │   │   ├── gateways        # Gateways for external services
│   │   │   ├── security        # Security configuration (JWT)
│   │   │   └── usecases        # Business logic
│   │   └── resources
│   │       ├── db/migration    # Flyway database migrations
│   │       └── application.properties
│   └── test                    # Unit and integration tests
├── .env                        # Environment variables (needs to be created)
├── pom.xml                     # Maven project configuration
└── Dockerfile                  # Docker configuration
```
