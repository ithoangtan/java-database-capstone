# Lab: Adding GitHub CI Workflows and Actions

**Estimated time:** 45 minutes

## Overview

In this lab, you will implement automated checks using GitHub Actions, a popular CI/CD tool built into GitHub. These actions will automatically lint and compile your code whenever you push or submit a pull request. This ensures code quality, helps catch syntax issues early, and prepares your app for production.

You will set up four GitHub Actions workflows to:

- Lint HTML, CSS, and JavaScript files in your front end
- Lint Java code in your Spring Boot back end using Checkstyle
- Compile the back end with Maven to check for syntax or dependency issues
- Lint your Dockerfile for best practices and security issues

These checks run automatically and are visible from the Actions tab in your repository.

## Prerequisites

Before starting this lab, you must:

- Complete all previous labs included in the course and have your full-stack Spring Boot app completed.

## About Cloud IDE lab

This Cloud IDE with Docker lab environment is where all of your development will take place. It has all of the tools you will need to use Docker.

It is important to understand that the lab environment is ephemeral. It only lives for a short while and then it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time it is needed.

Also note that this environment is shared and therefore not secure. You should not store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

## Step 0: Clone the repository

Clone the repository in the lab environment if you do not have it already.

1. Open the terminal by using the **Terminal -> New Terminal** menu.
2. Use Git to clone the repository.

Replace `{git-username}` with your GitHub username.

```bash
git clone https://github.com/{git-username}/java-database-capstone
```

## Step 1: Lint HTML, CSS, and JavaScript

You will create a GitHub Actions workflow to automatically check the quality of your front-end code every time a developer pushes code or submits a pull request.

1. Create a file named `.github/workflows/lint-frontend.yml`
2. Open `lint-frontend.yml` in the IDE
3. Use these hints to create the action code

### Hints

- Set up Node.js in the GitHub runner, since the linting tools (`htmlhint`, `stylelint`, `eslint`) all require Node to run
- Use `npm install -g` to install these linters globally
- The relevant front-end files are located in:
  - HTML: `app/src/main/resources/static/assets/pages/**/*.html`
  - CSS: `app/src/main/resources/static/assets/css/**/*.css`
  - JS: `app/src/main/resources/static/assets/js/**/*.js`
- Use glob patterns like `**/*.ext` to match files recursively
- It is okay to allow the job to continue on warnings for now by using `|| true`
- Trigger the workflow on both `push` and `pull_request`

## Step 2: Lint Java code using Checkstyle

This workflow runs Checkstyle on your back-end Java code to catch formatting and style violations early in the development process.

1. Create a file named `.github/workflows/lint-backend.yml`
2. Open `lint-backend.yml` in the IDE
3. Use these hints to create the action code

### Hints

- Create a new workflow file named `.github/workflows/lint-backend.yml`
- Use `actions/setup-java` to install and configure Java 17 in the GitHub Actions runner
- Download the Checkstyle tool from GitHub using `curl` - this is a standalone `.jar` file
- Use the default `google_checks.xml` ruleset for now
- Target the Java source folder: `app/src/main/java/com/project/back_end`
- Checkstyle will exit with non-zero status on warnings or errors. Add `|| true` if you want to avoid breaking the build for now
- Trigger this job on both `push` and `pull_request`

## Step 3: Compile the Java back end with Maven

In this step, you'll create a GitHub Actions workflow that compiles your back end using Maven. This ensures your project builds correctly and all dependencies resolve without issues.

1. Create a file named `.github/workflows/compile-backend.yml`
2. Open `compile-backend.yml` in the IDE
3. Use the following hints to create the action code

### Hints

- Use `actions/setup-java` to install and configure Java 17 in the GitHub Actions runner
- Your Maven project is inside the `app/` folder. Ensure you change into the `app` directory before running Maven commands
- Use:

```bash
mvn clean compile
```

- Trigger the workflow on both `push` and `pull_request`

## Step 4: Lint the Dockerfile

This step sets up a workflow to analyze your Dockerfile using Hadolint, a linter that helps catch syntax issues and suggests Docker best practices.

1. Create a file named `.github/workflows/lint-docker.yml`
2. Open `lint-docker.yml` in the IDE
3. Use the following hints to create the action code

### Hints

- Use the official Hadolint GitHub Action
- Your Dockerfile is located at `app/Dockerfile`
- This workflow should also run on `push` and `pull_request`
- The `with:` section of `hadolint/hadolint-action` lets you specify the path to the Dockerfile you want to lint

## Conclusion and Next Steps

### Conclusion

Continuous Integration (CI) is a foundational part of modern software development. In this lab, you implemented automated workflows using GitHub Actions to lint and compile your code. These pipelines help ensure that your application remains clean, functional, and production-ready, even as your team grows or code changes become more frequent.

By setting up these CI checks, you've adopted a mindset of quality-first development. Linting ensures your code follows style guidelines, compiling catches build issues early, and automation keeps everything consistent. These are the same practices used by professional teams delivering real-world software.

You're not just writing code - you're building resilient, maintainable systems like an industry engineer.

### Next Steps

- Push all your changes to GitHub
- Go to the Actions tab in your repository to verify that each workflow runs successfully
- Review the logs and check for linting or compile errors
- Fix any issues shown in the CI logs and re-commit your changes
- In the next lab, you'll package your application using Docker and learn how to run your Spring Boot app in a containerized environment

You're now ready to take the next step: containerizing your application and preparing it for deployment.

## Author(s)

Skills Network Team  
Upkar Lidder


## Checklist
- Did you create a GitHub Actions workflow (lint-frontend.yml) to check the quality of your front-end code—HTML, CSS, and JavaScript? 
- Did you create a separate GitHub Actions workflow (lint-backend.yml) to catch formatting and style violations?
- Did you set up a GitHub Actions workflow (compile-backend.yml) that compiles the Java back end using Maven?
- Did you set up a workflow (lint-docker.yml) using Hadolint to catch syntax issues and suggest Docker best practices?
