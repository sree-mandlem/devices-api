# devices-api

A Spring Boot REST API for managing **devices**, built with Java 21, Spring Data JPA, and PostgreSQL. The service exposes CRUD endpoints for devices, enforces domain/business rules in a dedicated service layer, and uses Flyway for database migrations.

## Getting Started

### Local Deployment - By setting up db and app manually

* Use docker to start postgres(change the details if needed). Make sure no other instance is running.

```
docker run -d \
  --name devices-postgres \
  -e POSTGRES_DB=devices \
  -e POSTGRES_USER=devices_user \
  -e POSTGRES_PASSWORD=changeme \
  -p 5432:5432 \
  -v devices_pgdata:/var/lib/postgresql/data \
  postgres:16

```
* Build using `mvn clean install`
* Run using `mvn spring-boot:run`

### Local Deployment - Using Docker Compose

* Make sure no other app or postgres DB instances are running
* Set up the environment variables in `.env` file.
* Run `docker-compose up`to spin up the containerized app and other dependencies.

## Access the API on the Swagger UI(OpenAPI documentation)
* The UI is available at `http://localhost:8080/swagger-ui/index.html`
  * Use **Try Me** functionality to test the endpoints.
* The API spec is available at `http://localhost:8080/v3/api-docs`

## High-level architecture

### Layers:

**Controller layer** - REST (Representational State Transfer) endpoints, request/response models (Data Transfer Objects).

**Service layer** - Business rules and domain validations.

**Repository layer** - Spring Data Java Persistence Application Programming Interface (JPA) for database access.

**Domain model** - Device entity.

**Cross-cutting**
 * Global exception handling 
 * Open Application Programming Interface documentation 
 * Database migration using Flyway

## Project Structure

The main packages under `com.company.devices`:

- `api`
    - `DeviceController` – REST controller exposing device endpoints.
    - `DeviceApi` – API interface/contract for the controller.
    - `dto` – Data Transfer Objects:
        - `DeviceCreateRequest`, `DeviceUpdateRequest`, `DevicePatchRequest`
        - `DeviceResponse`
        - `ErrorResponse`
    - `GlobalExceptionHandler` – centralized exception handling and error mapping.
- `domain`
    - `Device` – JPA entity representing a device in the system.
    - `DeviceState` – enum representing device state.
    - `exception` – domain-specific exceptions:
        - `DeviceNotFoundException`
        - `InvalidDeviceOperationException`
- `repository`
    - `DeviceRepository` – Spring Data JPA repository for `Device`.
- `service`
    - `DeviceService` – business logic and domain validations.
    - `DeviceMapper` – mapping between domain entities and DTOs.
- `DevicesApiApplication` – Spring Boot entry point.

Database migrations live under `src/main/resources/db/migration` (e.g. `V1__create_devices_table.sql`).

## Tools and Technologies used
* Java 21
* Spring Boot 4.x
* Maven
* Postgres DB backed with JPA, Flyway for migration
* JUnit Jupiter and Mockito for tests
* Lombok and MapStruct for code generation
* Spring OpenAPI for documentation
* Test Containers to test
* Dockerize the app and db with `docker-compose` to containerize

### Testing Strategy
* **Unit Tests** for coverage and validate business rules on service layer
* **Integration Tests** with **mockmvc** for controller layer
* **Test Containers** to test db migrations and repository tests

## Roadmap / Future Plans

The following improvements are planned to evolve `devices-api` into a more scalable, secure, and search-friendly platform:

### 1. Advanced Search & `getAll` with Elasticsearch

Current `getAll` and search-like operations are backed by the relational database via JPA.  

Planned enhancements:
- Integrate **Elasticsearch** to power and scale search functionality:
    - Full-text search on device attributes.
    - Filtering, sorting, and pagination for large device datasets.
- Implementation Plans:
    - **Indexed search documents** for devices.
    - Proper synchronization between PostgreSQL and Elasticsearch(Outbox Pattern + Kafkaa).

### 2. Split into Multiple Microservices

Currently, `devices-api` is a **single monolithic service**. Future architecture goals:

- Extract functionality into **separate microservices**, for example:
    - **Device Service** – core CRUD and device lifecycle.
    - **Search Service** – Elasticsearch-backed search and analytics.
- Introduce:
    - Clear **bounded contexts** and service contracts.
    - Inter-service communication (synchronous via REST, or asynchronous via messaging(Kafka)).

### 3. Securing Important Endpoints

At the moment, endpoints are designed for simplicity and local development.  
Planned security improvements:

- **Authentication & Authorization**
- **Token-based Security**
- **Endpoint Hardening** with method level security, input **validation** & **rate-limiting**.
- **API Gateway / Edge Security** (in microservices phase)

---

## Development and Contribution

Typical flow:

1. Fork the repository.
2. Create a feature branch.
3. Add/update tests where appropriate.
4. Open a pull request with a clear description of the change.

n `docker-compose up`