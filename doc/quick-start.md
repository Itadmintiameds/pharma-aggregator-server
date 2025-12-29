## Quick start

### Prerequisites

- **Docker** and **Docker Compose** installed
- Java and Maven are *not* required on the host (build happens inside Docker)

### 1. Build the Docker image


From the project root:


```bash
docker build -t pharma-aggregator-server -f dockerfile .
```

### 2. Run the app + PostgreSQL with Docker Compose

- **Development environment (Spring profile `dev`)**:

  ```bash
  docker-compose up app-dev           or        docker-compose up -d app-dev
  ```

  - App: `http://localhost:8080`

- **Test environment (Spring profile `test`)**:

  ```bash
  docker-compose up app-test
  ```

  - App: `http://localhost:8081`

- **Production-like environment (Spring profile `prod`)**:

  ```bash
  docker-compose up app-prod
  ```

  - App: `http://localhost:8082`

### 3. Using Docker Compose to start and stop the application

#### Starting the application

To start the application with Docker Compose, use the `docker-compose up` command:

**Development environment:**
```bash
docker-compose up app-dev
```

**Test environment:**
```bash
docker-compose up app-test
```

**Production environment:**
```bash
docker-compose up app-prod
```

**Run in detached mode (background):**
Add the `-d` flag to run containers in the background:
```bash
docker-compose up -d app-dev
```

#### Stopping the application

To stop the application and remove containers, use:
```bash
docker-compose down
```

This command will:
- Stop all running containers
- Remove containers
- Remove networks created by Docker Compose

**Stop a specific environment:**
```bash
docker-compose down app-dev
```

**Stop and remove volumes (clears database data):**
```bash
docker-compose down -v
```

#### Automatic image rebuilding

When you update the source code, Docker Compose will **automatically rebuild** the Docker image when you run `docker-compose up`. You do **not** need to manually build the image first.

**How it works:**
- Docker Compose detects changes in your source code
- It automatically rebuilds the image before starting the containers
- This ensures you're always running the latest code

**Force a rebuild:**
If you want to force a rebuild even if nothing changed, use the `--build` flag:
```bash
docker-compose up --build app-dev
```

**Note:** The first time you run `docker-compose up`, it will build the image. Subsequent runs will reuse the existing image unless source code changes are detected.

### 4. Run the app container directly (optional)

Use this command (change the profile as needed):

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  pharma-aggregator-server
```


