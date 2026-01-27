# Smart Clinic Management System - Architecture Design

## Section 1: Architecture Summary

This Spring Boot application implements a **three-tier architecture** for the Smart Clinic Management System, combining both MVC and REST architectural patterns. The **presentation layer** utilizes Thymeleaf templates for server-side rendering of Admin and Doctor dashboards, while REST APIs serve the Patient dashboard, Appointment module, and mobile/SPA clients. This hybrid approach provides flexibility for different client types while maintaining a unified backend.

The **application layer** follows a layered architecture with clear separation of concerns. Controllers handle HTTP requests and route them to the appropriate service layer. The service layer contains all business logic, including appointment scheduling rules, doctor availability checks, role-based access control, and JWT token validation. Services delegate data access operations to repositories, ensuring controllers remain thin and focused on request/response handling.

The **data layer** employs a polyglot persistence strategy using two database systems. **MySQL** stores structured relational data including patients, doctors, appointments, admins, and user authentication information. This data benefits from ACID properties and enforced relationships through foreign keys. **MongoDB** stores semi-structured documents such as prescriptions and medical notes, allowing flexible schemas that can evolve over time without migrations. Spring Data JPA manages MySQL entities with automatic ORM mapping, while Spring Data MongoDB handles document persistence with minimal configuration.

**Security** is implemented through JWT (JSON Web Tokens) for stateless authentication. The TokenService component generates, validates, and extracts user information from tokens. Each request to protected endpoints includes a JWT token that identifies the user and their role (admin, doctor, or patient). Role-based access control ensures users can only access resources appropriate to their permissions—patients view only their own records, doctors access their assigned appointments, and admins have full system management capabilities.

## Section 2: Numbered Flow of Data and Control

### Flow: Patient Books an Appointment

1. **User initiates request**: A patient accesses the appointment booking page through their dashboard. The patient selects a doctor, date, and time slot, then submits the form. The browser sends an HTTP POST request to `/api/appointments` with JSON payload containing `doctorId`, `patientId`, and `appointmentTime`. The request includes a JWT token in the Authorization header for authentication.

2. **Controller receives and validates request**: The `AppointmentController` (REST controller annotated with `@RestController`) receives the POST request. Spring automatically deserializes the JSON payload into a Java object. Bean validation annotations (`@Valid`, `@NotNull`, `@Future`) are checked automatically. If validation fails, Spring returns a 400 Bad Request response with error details. The controller extracts the JWT token from the request header.

3. **Token authentication and authorization**: Before processing the request, the controller calls `TokenService.validateToken()` to verify the JWT token. The TokenService decodes the token, extracts the user's email, and confirms the user exists in the Patient repository with the correct role. If validation fails, the controller immediately returns a 401 Unauthorized response. If successful, the patient's identity is confirmed and the request proceeds.

4. **Service layer processes business logic**: The controller delegates to `AppointmentService.createAppointment()`, passing the validated request data. The service layer performs critical business logic checks: (a) Verifies the doctor exists by calling `DoctorRepository.findById(doctorId)`; (b) Verifies the patient exists by calling `PatientRepository.findById(patientId)`; (c) Checks doctor availability for the requested time slot by querying existing appointments; (d) Validates the appointment time is in the future and during working hours. If any check fails, the service throws a business exception. If all validations pass, the service creates a new `Appointment` entity object, sets its properties (doctor, patient, appointmentTime, status=SCHEDULED), and prepares it for persistence.

5. **Repository layer persists data**: The service calls `AppointmentRepository.save(appointment)` to persist the new appointment. Spring Data JPA translates this method call into an SQL INSERT statement. The JPA entity manager handles the database transaction, manages the connection, executes the query against the MySQL database, and commits the transaction. MySQL enforces referential integrity through foreign key constraints linking to the doctors and patients tables. After successful insertion, MySQL returns the auto-generated appointment ID, which JPA maps back to the entity object.

6. **Response propagation back through layers**: The repository returns the saved `Appointment` entity (now with its database-generated ID) to the service layer. The service may transform the entity into an `AppointmentDTO` (Data Transfer Object) to control which fields are exposed to the client and prevent over-fetching or sensitive data leakage. The service returns this DTO to the controller. The controller wraps the DTO in a `ResponseEntity` with HTTP status 200 OK or 201 Created.

7. **Client receives response**: Spring Boot automatically serializes the Java DTO object into JSON format using Jackson. The JSON response is sent back to the patient's browser with appropriate HTTP headers. The frontend JavaScript receives the response, parses the JSON, updates the UI to display the new appointment in the patient's schedule, and shows a success message. The appointment is now visible in both the patient dashboard and the doctor's schedule, ready for the doctor to review and prepare for the consultation.

### Alternative Flow: Doctor Views Appointment List (Thymeleaf MVC)

1. **User accesses dashboard**: A doctor logs into the system and navigates to their dashboard at `/doctor/dashboard`. The browser sends an HTTP GET request with the doctor's JWT token in a cookie or Authorization header.

2. **MVC Controller handles request**: The `DashboardController` (annotated with `@Controller`) receives the request. Unlike REST controllers, MVC controllers return view names instead of data. The controller validates the JWT token to authenticate the doctor.

3. **Service retrieves data**: The controller calls `AppointmentService.getAppointmentsByDoctor(doctorId)` to fetch all appointments for this doctor. The service queries `AppointmentRepository` which executes a JPA query (e.g., `findByDoctorId()`) against MySQL.

4. **Data added to Model**: The controller receives a list of `Appointment` entities from the service and adds them to the Spring MVC `Model` object using `model.addAttribute("appointments", appointments)`. The Model acts as a data container that will be passed to the view template.

5. **Thymeleaf renders HTML**: The controller returns the string `"doctor/doctorDashboard"`, which Spring resolves to the Thymeleaf template at `templates/doctor/doctorDashboard.html`. Thymeleaf processes the template, iterating over the appointments list using `th:each`, accessing object properties with `th:text="${appointment.patientName}"`, and generating dynamic HTML based on the model data.

6. **Server sends HTML response**: The fully rendered HTML page is sent back to the doctor's browser. Unlike REST APIs that return raw data (JSON), the MVC approach returns a complete, styled web page ready for display.

7. **Browser displays page**: The doctor's browser receives and renders the HTML, displaying a formatted list of appointments with patient names, times, and action buttons (e.g., "View Details", "Add Prescription"). All presentation logic is handled server-side, simplifying the frontend.

---

## Architecture Diagram Reference

The system follows this high-level flow:

```
Client Layer (Browser/Mobile)
    ↓
Presentation Layer (Thymeleaf Templates / REST API Endpoints)
    ↓
Controller Layer (AdminController, DoctorController, PatientController, AppointmentController)
    ↓
Service Layer (AppointmentService, DoctorService, PatientService, TokenService)
    ↓
Repository Layer (Spring Data JPA / Spring Data MongoDB)
    ↓
Data Layer (MySQL: patients, doctors, appointments | MongoDB: prescriptions)
```

**Key Components:**
- **Security**: JWT-based authentication handled by TokenService
- **Validation**: Bean Validation annotations on entities and DTOs
- **Persistence**: Dual database strategy (MySQL + MongoDB)
- **Architecture Pattern**: Layered architecture with MVC + REST hybrid approach
