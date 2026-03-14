# Lab: Containerizing Spring Boot Application with Docker

**Estimated time:** 45 minutes

## Learning objectives

By the end of this guide, you will be able to:

1. Explain how to build a multi-stage Dockerfile for a Java Spring Boot backend
2. Build and run the Spring Boot container using Docker
3. Push images to a container registry and understand how this fits into a CI/CD workflow
4. Follow best practices to optimize and secure your Docker images

## Prerequisites

- Ensure you have finished all the labs so far and the code is working in the lab IDE
- Start the MySQL and the MongoDB databases in the lab environment

## Overview

This lab walks you through containerizing the smart clinic full-stack application using Docker. The backend is a Spring Boot application that connects to both MySQL and MongoDB databases. By containerizing your backend, you achieve a consistent and reproducible environment that simplifies deployment and scaling.

To perform the exercises given in this lab, you'll use Cloud IDE lab environment. This is where all of your development will take place.

## About Cloud IDE lab

It is important to understand that the lab environment is ephemeral. It only lives for a short while and then it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time later.

Also, this environment is shared. It is recommended not to store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

> **Note:**  
> 1. If you haven't generated a GitHub Personal Access Token you should do so now. You will need it to push code back to your repository. It should have `repo` and `write` permissions, and set to expire in **60 days**. When Git prompts you for a password in the Cloud IDE environment, use your Personal Access Token instead.  
> 2. The environment may be recreated at any time so you may find that you have to perform the **Initialize Development Environment** step each time the environment is created.  
> 3. Create a repository from the GitHub template provided for this lab in the next step.

## Key Terms

- **Docker:** A platform to package applications and dependencies into containers for consistent deployment.
- **Container:** A lightweight, standalone executable that includes everything needed to run a piece of software.
- **Dockerfile:** A script containing instructions to build a Docker image.
- **Multi-stage Build:** A Dockerfile technique that separates build and runtime environments to reduce image size.
- **Image:** A snapshot of a container's file system and configuration used to create containers.
- **Container Registry:** A service to store and distribute Docker images, for example Docker Hub or GitHub Container Registry.

## Step 0: Clone the Repository

Clone the repository in the lab environment if you do not have it already.

1. Open the terminal by using the **Terminal -> New Terminal** menu.
2. Use Git to clone the repository.

Replace `{git-username}` with your GitHub username.

```text
git clone https://github.com/{git-username}/java-database-capstone
cd java-database-capstone
```

## Step 1: Create the Dockerfile for the Backend

1. Inside your `app` folder, create a new file named `Dockerfile`.
2. Open `Dockerfile` in the IDE.
3. Add the following content to your `Dockerfile`.

In this step, you'll create a multi-stage Dockerfile to containerize your Spring Boot backend. The Dockerfile will use Maven to build your application in one stage and then copy the resulting `.jar` into a lightweight Java runtime image in a second stage. This results in a smaller, production-ready Docker image that contains only the compiled application and its runtime dependencies.

```dockerfile
# Step 1: Use Maven with JDK 17 to build the app
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Step 2: Use lightweight JRE 17 for running the app
FROM eclipse-temurin:17.0.15_6-jre
WORKDIR /app

COPY --from=builder /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Explanation

- `FROM maven`: First stage uses Maven and JDK 17 to compile the application
- `COPY`: Brings in your Maven config and source code
- `RUN mvn clean package -DskipTests`: Builds the `.jar` file
- `FROM eclipse-temurin`: Second stage uses a smaller base image with only the JRE
- `COPY --from=builder`: Transfers the built jar from the first stage
- `EXPOSE 8080`: Documents the port your app runs on
- `ENTRYPOINT`: Specifies how the container should run your app

## Step 2: Build and Run the Container

Now that your Dockerfile is ready, let's build and run your backend as a container.

### Build the Docker image

Run the following command in the `java-database-capstone/app` folder of your project:

```text
docker build -t smart-clinic-backend .
```

This command:

- Builds a Docker image named `smart-clinic-backend`
- Uses the current directory, which contains the `Dockerfile`, as context

### Run the container

Once the image is built, start the container with:

```text
docker run -d -p 8080:8080 --name smart-clinic smart-clinic-backend
```

Explanation:

- `-d` runs the container in detached mode
- `-p 8080:8080` maps container port `8080` to the host
- `--name smart-clinic` gives your container a name

## Step 3: Access the Application

Use the **Launch Application** feature of the lab environment. Enter `8080` as the port and click the **Open in new browser tab** icon. The resulting screen will display the Home page of the application.

## Step 4: Push to a Container Registry (optional)

To enable CI/CD workflows and deployment pipelines, Docker images are often stored in a container registry such as Docker Hub or GitHub Container Registry.

In this step, you'll prepare your Docker image for publication by tagging it and pushing it to a container registry like Docker Hub. Container registries store Docker images in the cloud, making them accessible for deployment and CI/CD pipelines. By pushing your image to a registry, you can reuse it across environments and integrate it into automated workflows.

### Tag the image

```text
docker tag smart-clinic-backend your-docker-username/smart-clinic-backend:latest
```

### Log in to Docker Hub

```text
docker login
```

### Push the image

```text
docker push your-docker-username/smart-clinic-backend:latest
```

You can now reference this image in deployment workflows or use it in cloud services that pull from a container registry.

## Step 5: Docker Best Practices

Here are some best practices to follow when building Docker images and running containers.

### Image optimization

- Use multi-stage builds to separate build and runtime dependencies
- Start with minimal base images like Alpine or slim variants where possible
- Avoid installing unnecessary tools or dependencies in the final image
- Clean up intermediate files, for example Maven cache, if not using multi-stage builds

### Security

- Do not run containers as root unless necessary
- Use `.dockerignore` to avoid copying unnecessary files into the image
- Scan your images regularly for known vulnerabilities using tools like `docker scan`

### Container management

- Use explicit versions for base images to avoid unexpected changes
- Use health checks in production to detect container failures
- Use environment variables to configure containers at runtime

Following these practices ensures your Dockerized apps are lean, secure, and maintainable.

## Step 6: Cleaning up the Container

After testing your Spring Boot application, it's good practice to stop and remove the running Docker container to free up resources.

### Step 1: Stop the container

Use the following command to stop the container named `smart-clinic`:

```text
docker stop smart-clinic
```

This gracefully stops the container process.

### Step 2: Remove the container

Once stopped, you can remove the container:

```text
docker rm smart-clinic
```

This deletes the container instance, but not the image itself. You can recreate the container anytime from the image.

### Optional: Remove the Docker image

If you no longer need the image, you can also remove it:

```text
docker rmi smart-clinic-backend
```

This will delete the built image from your system. You will need to rebuild it using `docker build` if you want to run it again.

> **Tip:** Use `docker ps -a` to list all containers, including stopped ones, and `docker images` to list all images on your system.

Cleaning up ensures your development environment remains clean and does not consume unnecessary system resources.

## Step 7: Docker Compose (optional)

Docker Compose is a tool that lets you define and manage multi-container Docker applications using a simple YAML file.

Instead of starting each service manually, you can define your backend, database, and other services in a single `docker-compose.yml` file and bring them up together with one command. This is especially useful for local development or replicating a full environment for testing or production.

While Docker Compose is not required in this lab, since MySQL and MongoDB are already available, here is a sample setup you can use in a local or production environment.

### Sample `docker-compose.yml`

```yaml
version: "3.8"

services:
  app:
    build:
      context: .
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      mongodb:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/mydb
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_DATA_MONGODB_URI: mongodb://mongodb:27017/my_mongo_db

  mysql:
    image: mysql:8.0
    restart: always
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: mydb
    healthcheck:
      test: ["CMD", "mysql", "-uroot", "-proot", "-e", "SELECT 1"]
      interval: 10s
      timeout: 5s
      retries: 5

  mongodb:
    image: mongo:6.0
    restart: always
    ports:
      - "27017:27017"
    healthcheck:
      test: ["CMD", "mongosh", "--quiet", "--eval", "db.runCommand({ ping: 1 })"]
      interval: 10s
      timeout: 5s
      retries: 5
```

This setup can be used to replicate the full backend stack on any developer machine.

## Conclusion

In this lab, you containerized a full-stack Spring Boot backend using Docker. Specifically, you:

1. Created a multi-stage Dockerfile for a Spring Boot app
2. Built and ran a Docker container exposing the application
3. Pushed your image to a container registry for future CI/CD use
4. Learned optimization and security best practices for containerized apps
5. Optionally reviewed Docker Compose for multi-container setups

Containerization is a key skill in modern backend development. It improves reproducibility, scalability, and simplifies deployment pipelines across environments.

## Author(s)

Skills Network Team  
Upkar Lidder



## Checklist
Did you create the Dockerfile for the application with the file name `Dockerfile`?
Did you build your backend as a container?
Did you verify some best practices to follow when building Docker images and running containers?
Did you remove the running Docker container to free up resources?
Did you commit and push the stories to GitHub successfully?