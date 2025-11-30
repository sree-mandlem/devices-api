

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

## Deployment
### Local
* Use docker to start postgres(change the details if needed)
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
* Run `mvn spring-boot:run`
* OpenAPI documentation 
  * The UI is available at `http://localhost:8080/swagger-ui/index.html`
  * The API spec is available at `http://localhost:8080/v3/api-docs`

