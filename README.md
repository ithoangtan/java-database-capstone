# Smart Clinic Management System

Clinic management system (Spring Boot, MySQL, MongoDB).

## Prerequisites

- **Java 17**
- **Maven 3.6+** (or use `./mvnw` in the `app` folder)
- **MySQL 8** and **MongoDB** (when running with Maven)
- **Docker** and **Docker Compose** (when running with Docker)

---

## Running locally

### Option 1: Run with Maven (Spring Boot)

1. **Start MySQL and MongoDB** on your machine (ports 3306 and 27017).

2. **Database config** is read from **environment variables**. If you don’t set any, defaults in `application.properties` are used:
   - MySQL: `localhost:3306`, database `cms`, user `root`, password (empty by default)
   - MongoDB: `mongodb://localhost:27017/prescriptions`
   - To override: set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_MONGODB_URI` in your shell or IDE before running.

3. **Run the application:**

   ```bash
   cd app
   ./mvnw spring-boot:run
   ```

   Or if you have Maven installed globally:

   ```bash
   cd app
   mvn spring-boot:run
   ```

4. Open in browser: **http://localhost:8080**

---

### Option 2: Run with Docker

#### Backend only (MySQL and MongoDB must be running elsewhere)

The app needs DB environment variables inside the container. Copy `app/.env.example` to `app/.env` and use `--env-file .env` when running.

1. **Create `.env`** in `app/`: copy `app/.env.example` to `app/.env`, edit host/user/password if needed.

2. **Build and run** (from the `app` directory):

   ```bash
   cd app
   docker build -t smart-clinic-backend .
   docker run -d -p 8080:8080 --env-file .env --name smart-clinic smart-clinic-backend
   ```

3. Open **http://localhost:8080**

4. **Stop and remove container:** `docker stop smart-clinic` then `docker rm smart-clinic`

#### Backend + MySQL + MongoDB (Docker Compose)

1. **Start the full stack:**

   ```bash
   cd app
   docker compose up -d
   ```

   The first run will build the backend image and pull MySQL and MongoDB images. All database settings come from **environment variables** (with defaults). To override, create a `.env` file in `app/` or export variables before `docker compose up` — see `app/.env.example` for variable names.

2. App: **http://localhost:8080**  
   With defaults: MySQL `localhost:3306` (user `root`, password `root`, database `mydb`), MongoDB `localhost:27017` (database `my_mongo_db`)

3. **Stop and remove:**

   ```bash
   cd app
   docker compose down
   ```

---

## Command summary

| Purpose              | Command |
|----------------------|---------|
| Run with Maven       | `cd app` → `./mvnw spring-boot:run` |
| Build Docker image   | `cd app` → `docker build -t smart-clinic-backend .` |
| Run backend container | From `app/`: create `.env` from `.env.example`, then `docker run -d -p 8080:8080 --env-file .env --name smart-clinic smart-clinic-backend` |
| Run full stack       | `cd app` → `docker compose up -d` |
| Stop container       | `docker stop smart-clinic` |
| Stop Compose         | `cd app` → `docker compose down` |
