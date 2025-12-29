## Docker setup and Spring profiles

This project is configured to run completely inside Docker with three Spring profiles:

- **dev**: local development
- **test**: integration / test environment
- **prod**: production-like environment

Profile-specific configuration files:

- **Shared config**: `src/main/resources/application.yml`
- **Dev config**: `src/main/resources/application-dev.yml`
- **Test config**: `src/main/resources/application-test.yml`
- **Prod config**: `src/main/resources/application-prod.yml`

### Dev profile datasource (inside Docker)

`application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres-dev:5432/pharma-aggregator?charSet=UTF8
    username: postgres
    password: root
    driver-class-name: org.postgresql.Driver
```

The hostname `postgres-dev` matches the PostgreSQL service name defined in `docker-compose.yml`, so the application can connect to the database when both containers run on the same Docker network.

Test and prod profiles use the same pattern with `postgres-test` and `postgres-prod` hosts.

### Build Docker image

From the project root:

```bash
docker build -t pharma-aggregator-server -f dockerfile .
```

### Run with Docker Compose (recommended)

From the project root:

- **Dev environment**:

  ```bash
  docker-compose up app-dev
  ```

  - App URL: `http://localhost:8080`
  - PostgreSQL: `localhost:5432` (db: `pharma-aggregator`, user: `postgres`, password: `root`)

- **Test environment**:

  ```bash
  docker-compose up app-test
  ```

  - App URL: `http://localhost:8081`
  - PostgreSQL: `localhost:5433` (db: `pharma-aggregator_test`, user: `postgres`, password: `root`)

- **Prod environment**:

  ```bash
  docker-compose up app-prod
  ```

  - App URL: `http://localhost:8082`
  - PostgreSQL: `localhost:5434` (db: `pharma-aggregator_prod`, user: `postgres`, password: `root`)

Docker Compose builds the image once and reuses it for all three app services, switching environments via the `SPRING_PROFILES_ACTIVE` environment variable.

### Run container manually with a specific profile

If you want to run the application container without Docker Compose (for example, against an existing PostgreSQL instance), you can override the Spring profile:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  pharma-aggregator-server
```

Replace `dev` with `test` or `prod` as needed.


