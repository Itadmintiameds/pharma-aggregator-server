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

#### Option A: Always rebuild (recommended for development)

**Windows (PowerShell):**
```powershell
.\start-dev.ps1
```

**Windows (Command Prompt):**
```cmd
start-dev.bat
```

**Linux/Mac:**
```bash
chmod +x start-dev.sh
./start-dev.sh
```

These scripts will:
- Stop existing containers
- Rebuild the Docker image without cache (includes all new files)
- Start the application

#### Option B: Standard Docker Compose

- **Development environment (Spring profile `dev`)**:

  ```bash
  docker-compose up --build app-dev
  ```

  Or in detached mode:
  ```bash
  docker-compose up --build -d app-dev
  ```

  - App: `http://localhost:8080`

**Note:** Use `--build` flag to ensure latest code changes are included.

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
docker-compose up app-dev     or    docker-compose up --build app-dev
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

**Important:** Docker Compose does **NOT** automatically detect source code changes. You must rebuild the image to include new files.

**To always rebuild with latest code:**

1. **Use the helper scripts** (recommended):
   - Windows: `start-dev.bat` or `.\start-dev.ps1`
   - Linux/Mac: `./start-dev.sh`
   - These scripts always rebuild without cache

2. **Use `--build` flag:**
   ```bash
   docker-compose up --build app-dev
   ```

3. **Force rebuild without cache:**
   ```bash
   docker-compose build --no-cache app-dev
   docker-compose up app-dev
   ```

**Why rebuild is needed:**
- Docker images are built from your source code at build time
- Changes to `.java` files require rebuilding the image
- The container runs the compiled JAR, not your source files
- Always use `--build` or the helper scripts to ensure latest code is included

### 4. Run the app container directly (optional)

Use this command (change the profile as needed):

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  pharma-aggregator-server
```


