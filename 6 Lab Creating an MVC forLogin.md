# Lab: Creating an MVC for Login

**Estimated time:** 45 minutes

## Overview

This lab demonstrates how to use Thymeleaf with Spring Boot for rendering views after validating user roles (admin and doctor) through token-based access control. You will implement a controller that dynamically serves Thymeleaf templates based on user authentication status.

## Learning objectives

By the end of this lab, you will be able to:

- Explain how to use `@Controller` for returning Thymeleaf views
- Implement token validation logic using a Spring Boot service
- Secure routes conditionally and render different views for different roles
- Apply best practices in separating MVC logic and using `@GetMapping` with path variables

## Prerequisites

- Ensure you have completed the previous lab on model creation and database integration
- Have your GitHub Personal Access Token ready for pushing your completed work to GitHub

## About Cloud IDE lab

This Cloud IDE with Docker lab environment is where all of your development will take place. It has all the tools you will need to use Docker.

It is important to understand that the lab environment is **ephemeral**. It only lives for a short while and then it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time it is needed.

Also note that this environment is shared and therefore not secure. You should not store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

> **Note:** If you haven't generated a GitHub Personal Access Token you should do so now. You will need it to push code back to your repository. It should have `repo` and `write` permissions, and set to expire in **60 days**. When Git prompts you for a password in the Cloud IDE environment, use your Personal Access Token instead. The environment may be recreated at any time so you may find that you have to perform the **Initialize Development Environment** step each time the environment is created. Create a repository from the GitHub template provided for this lab in the next step.

## Key Terms

- **Thymeleaf:** A modern server-side Java template engine used for rendering HTML views
- **@Controller:** A Spring annotation that marks a class as a web controller for returning views
- **@GetMapping:** Annotation for mapping HTTP GET requests to specific handler methods
- **@PathVariable:** Used to extract values from URI templates and bind them to method parameters
- **Token Validation:** The process of checking whether a provided authentication token is valid
- **Redirect:** An HTTP response that instructs the browser to navigate to a different URL

## Configuring the Application

### Thymeleaf configuration in `application.properties`

This section explains how to configure Thymeleaf and static resource handling in a Spring Boot application to support rendering HTML templates and serving static files.

### 1. Open the `application.properties` file

Open `application.properties` in the IDE.

### 2. Configure static resource locations

```properties
spring.web.resources.static-locations=classpath:/static/
```

- This setting tells Spring Boot where to look for static assets (for example, CSS, JS, images)
- Files placed in `src/main/resources/static/` will be served directly by Spring without the need for a controller
- Example: A CSS file in `src/main/resources/static/css/style.css` will be accessible at `http://localhost:8080/css/style.css`

### 3. Set Thymeleaf template location and behavior

```properties
spring.thymeleaf.prefix=classpath:/templates/
```

- Defines the base directory for Thymeleaf templates
- Spring will look for templates inside `src/main/resources/templates/`

```properties
spring.thymeleaf.suffix=.html
```

- Specifies that all Thymeleaf templates will have an `.html` suffix
- You can return `"admin/adminDashboard"` and Spring will resolve it to `admin/adminDashboard.html`

```properties
spring.thymeleaf.mode=HTML
```

- Sets the rendering mode for templates
- `HTML` mode is most commonly used for rendering well-formed HTML5 pages

```properties
spring.thymeleaf.cache=false
```

- Useful during development to see changes immediately without restarting the app

```properties
spring.thymeleaf.encoding=UTF-8
```

- Ensures that all templates are read using UTF-8 encoding to support internationalization and special characters

### Steps summary

1. **Static resources:** Set `spring.web.resources.static-locations` to serve files like CSS and JS from `/static/`
2. **Templates directory:** Point `spring.thymeleaf.prefix` to `/templates/` to locate HTML files
3. **Template behavior:** Configure suffix, mode, cache, and encoding for optimal development settings

## Creating Controller

### Dashboard controller

The dashboard controller handles view rendering for Thymeleaf templates after validating a token for either admin or doctor users.

**Purpose:** This controller serves as a gatekeeper to the Thymeleaf dashboard views `adminDashboard` and `doctorDashboard` by verifying access tokens for authenticated users.

### 1. Open the `DashboardController.java` file

Open `DashboardController.java` in the IDE.

### 2. Set up the Controller class

- Annotate the class with `@Controller` to indicate it returns views, not JSON responses
- This class maps requests to Thymeleaf templates based on user roles and tokens

### 3. Autowired dependencies

Autowired the required service:

- `Service` for handling the token validation logic

### 4. Define the `adminDashboard` method

- Accept the token as a `@PathVariable`
- Call `validateToken(token, "admin")` using the service and check if the returned map is empty:
  - If empty: token is valid -> return the `admin/adminDashboard` view
  - If not empty: redirect to login page

### 5. Define the `doctorDashboard` method

- Annotate with `@GetMapping("/doctorDashboard/{token}")` to handle doctor dashboard access
- Accept the token as a `@PathVariable`
- Call `validateToken(token, "doctor")` and apply the same logic as in `adminDashboard`

### 6. Response

Each method returns a view name (`String`) for Thymeleaf to resolve:

- If token is valid: returns the respective dashboard template
- If invalid: redirects the user to the login page at `http://localhost:8080`

### Steps summary

1. **Set up the controller:** Annotate with `@Controller`
2. **Inject the service:** Autowire the token validation logic
3. **Define the view endpoints:** Use `@GetMapping`, validate tokens, and return the appropriate dashboard view or redirect

## Save your work

Before you finish this lab, make sure to **add, commit, and push** your updated code to your GitHub repository. You will be asked to provide your public GitHub repo URL for graded evaluation at the end of the capstone.

Follow these steps to push your changes:

1. Stage your changes:

```bash
git add .
```

2. Commit your changes with a meaningful message:

```bash
git commit -m "Setup Thymeleaf and created controller"
```

3. Push your changes using your GitHub Personal Access Token:

```bash
git push https://<your_github_username>:<your_personal_access_token>@github.com/<your_github_username>/java-database-capstone.git
```

Replace:

- `<your_github_username>` with your GitHub username
- `<your_personal_access_token>` with the token you generated at the start of the lab

### Example

If your GitHub username is `johnsmith` and your token is `ghp_ABC123xyzTOKEN`, your push command would look like:

```bash
git push https://johnsmith:ghp_ABC123xyzTOKEN@github.com/johnsmith/java-database-capstone.git
```

### Remember

- Always push your work after completing each lab
- Save your GitHub URL — you will need to submit it for your final project evaluation

## Conclusion and Next Steps

### Conclusion

In this lab, you have configured and worked with Spring Boot's integration with Thymeleaf to render dynamic views and validate user tokens for different user roles. By setting the appropriate properties in the `application.properties` file, you've ensured that static resources and Thymeleaf templates are served correctly, and the application works seamlessly during development.

Key takeaways:

- How to configure Thymeleaf for rendering HTML templates
- How to serve static resources (CSS, JavaScript, etc.) from the `static` folder
- How to ensure efficient development through template caching and character encoding settings

You also implemented a dashboard controller that uses token validation to render user-specific views for both admins and doctors.

### Next steps

Now that you have learned the basic integration of Spring Boot and Thymeleaf, you can take the following steps to continue improving and extending your project:

1. **Implement authentication and authorization**
   - Explore Spring Security to add real authentication (JWT, OAuth, etc.) to further secure your app
   - Use roles and permissions to restrict access to specific resources based on user credentials

2. **Enhance token validation**
   - Build a more robust token validation system (for example, JWT token expiration handling)
   - Add error handling to provide user-friendly messages when token validation fails

## Author(s)

Skills Network Team  
Upkar Lidder


## Check list
Did you configure Thymeleaf settings in application.properties file?
Did you set the static resource location to serve files like CSS/JS from the /static/ directory?
Did you annotate your dashboard controller class with @Controller to handle view rendering for Thymeleaf templates?