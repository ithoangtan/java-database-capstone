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

2. **Configure databases** (if needed): edit `app/src/main/resources/application.properties`:
   - MySQL: database `cms`, user `root`, password (if any)
   - MongoDB: URI `mongodb://localhost:27017/prescriptions`

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

1. **Build the image:**

   ```bash
   cd app
   docker build -t smart-clinic-backend .
   ```

2. **Run the container** (mapping port 8080):

   ```bash
   docker run -d -p 8080:8080 --name smart-clinic smart-clinic-backend
   ```

   If the backend needs to connect to MySQL/MongoDB on the host:

   ```bash
   docker run -d -p 8080:8080 \
     -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/cms \
     -e SPRING_DATASOURCE_USERNAME=root \
     -e SPRING_DATASOURCE_PASSWORD= \
     -e SPRING_DATA_MONGODB_URI=mongodb://host.docker.internal:27017/prescriptions \
     --name smart-clinic smart-clinic-backend
   ```

3. Open: **http://localhost:8080**

4. **Stop and remove the container:**

   ```bash
   docker stop smart-clinic
   docker rm smart-clinic
   ```

#### Backend + MySQL + MongoDB (Docker Compose)

1. **Start the full stack:**

   ```bash
   cd app
   docker compose up -d
   ```

   The first run will build the backend image and pull MySQL and MongoDB images.

2. App: **http://localhost:8080**  
   MySQL: `localhost:3306` (user `root`, password `root`, database `mydb`)  
   MongoDB: `localhost:27017` (database `my_mongo_db`)

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
| Run backend container | `docker run -d -p 8080:8080 --name smart-clinic smart-clinic-backend` |
| Run full stack       | `cd app` → `docker compose up -d` |
| Stop container       | `docker stop smart-clinic` |
| Stop Compose         | `cd app` → `docker compose down` |
