

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

## Tools and Technologies used
### Currently in use
* Java 21
* Spring Boot 4.x
* Maven
* H2 in memory db for dev
* JUnit Jupiter and Mockito for tests
* Lombok and MapStruct for code generation
### Planned
* Postgres for non dev
* Flyway for migration
* Docker with `docker-compose` to containerize
* Test Containers to test
* Spring OpenAPI for documentation

### Testing Strategy
* **Unit Tests** for coverage and validate business rules on service layer
* **Integration Tests** with **mockmvc** for controller layer
* **Test Containers** to test db migrations and repository tests
