# Lab: Creating REST Endpoints

**Estimated time needed:** 120 minutes

## Overview

Welcome to this lab on **Creating REST Endpoints**.

This lab demonstrates how to build structure, service, and controller layers in a Spring Boot application for a Clinic Management System. You will implement RESTful APIs using controllers, structure your business logic within service classes, and pass data efficiently using Data Transfer Objects (DTOs). The service and controller layer will enable communication between the client-side frontend and the backend models and repositories created in earlier labs.

You will also handle request validation, error handling, and service delegation to ensure modular and maintainable code.

## Learning Objectives

By the end of this lab, you will be able to:

- Create and annotate controller classes to handle HTTP requests
- Implement service classes that encapsulate business logic
- Use DTOs to transfer data between layers cleanly and securely
- Apply validation annotations to incoming data through DTOs
- Handle exceptions gracefully with global or controller-level handlers
- Map between entities and DTOs for input or output transformation

## Pre-requisites

- Ensure you have completed the previous lab on model creation and database integration
- Have your GitHub Personal Access Token ready for pushing your completed work to GitHub

## About Cloud IDE lab

This Cloud IDE with Docker lab environment is where all of your development will take place. It has all of the tools you will need to use Docker.

It is important to understand that the lab environment is **ephemeral**. It only lives for a short while and then it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time it is needed.

Also note that this environment is shared and therefore not secure. You should not store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

> **Note:**  
> 1. If you haven't generated a GitHub Personal Access Token you should do so now. You will need it to push code back to your repository. It should have `repo` and `write` permissions, and set to expire in 60 days.  
> 2. When Git prompts you for a password in the Cloud IDE environment, use your Personal Access Token instead.  
> 3. The environment may be recreated at any time so you may find that you have to perform the **Initialize Development Environment** step each time the environment is created.  
> 4. Create a repository from the GitHub template provided for this lab in the next step.

## Key Terms

- **Controller:** A Spring component that handles incoming HTTP requests and maps them to service methods
- **@RestController:** Annotation to define a controller where every method returns a domain object instead of a view
- **@RequestMapping / @GetMapping / @PostMapping:** Annotations to define HTTP route handlers
- **Service Layer:** The part of the application that contains business logic and interacts with the repository layer
- **@Service:** Annotation to mark a class as a service provider in Spring
- **DTO (Data Transfer Object):** A plain Java object used to transfer data between the frontend and backend
- **@Valid:** Annotation used to trigger validation on incoming DTOs
- **@RequestBody / @PathVariable / @RequestParam:** Used to bind web request data to method parameters
- **Exception Handling:** Managing and responding to errors gracefully in your API

## Creating DTOs

### Appointment DTO

You'll now create a DTO to represent appointment data. This class helps decouple frontend requirements from the internal database structure.

1. Open the `AppointmentDTO.java` file.
2. Create a DTO class that represents appointment data to be used in communication between your backend services and frontend clients.

Add the following fields:

- `id`: `Long` - Unique identifier for the appointment
- `doctorId`: `Long` - ID of the doctor assigned to the appointment
- `doctorName`: `String` - Full name of the doctor
- `patientId`: `Long` - ID of the patient
- `patientName`: `String` - Full name of the patient
- `patientEmail`: `String` - Email address of the patient
- `patientPhone`: `String` - Contact number of the patient
- `patientAddress`: `String` - Residential address of the patient
- `appointmentTime`: `LocalDateTime` - Full date and time of the appointment
- `status`: `int` - Appointment status
- `appointmentDate`: `LocalDate` - Extracted date from `appointmentTime`
- `appointmentTimeOnly`: `LocalTime` - Extracted time from `appointmentTime`
- `endTime`: `LocalDateTime` - Calculated as `appointmentTime + 1 hour`

Add a constructor that initializes all core fields and automatically computes:

- `appointmentDate` using `appointmentTime.toLocalDate()`
- `appointmentTimeOnly` using `appointmentTime.toLocalTime()`
- `endTime` using `appointmentTime.plusHours(1)`

Add standard getter methods for each field.

#### Hint

This DTO class should not contain persistence annotations like `@Entity` or `@Id`.  
It is meant to simplify and format data transferred to and from the frontend.

### Login DTO

You'll create a DTO to handle user login requests.

1. Open the `Login.java` file.
2. Create a DTO class to represent login request data.

Add the following fields:

- `identifier`: `String` - The unique identifier of the user attempting to log in (`email` for Doctor/Patient, `username` for Admin)
- `password`: `String` - The password provided by the user

Add standard getter and setter methods for both fields.

#### Hint

This class is typically used in `@RequestBody` parameters inside controller methods.  
Do not add persistence annotations.

## Creating Repositories

### Admin Repository

1. Open the `AdminRepository.java` file.
2. Create a repository for the `Admin` model by extending `JpaRepository`.

Add the following method:

- `findByUsername`
  - Return type: `Admin`
  - Parameter: `String username`

#### Hint

Extend `JpaRepository<Admin, Long>` to inherit CRUD functionality.

### Appointment Repository

1. Open the `AppointmentRepository.java` file.
2. Create a repository for the `Appointment` model by extending `JpaRepository`.

Add these methods:

- `findByDoctorIdAndAppointmentTimeBetween`
- `findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween`
- `deleteAllByDoctorId`
- `findByPatientId`
- `findByPatient_IdAndStatusOrderByAppointmentTimeAsc`
- `filterByDoctorNameAndPatientId`
- `filterByDoctorNameAndPatientIdAndStatus`

#### Details

- Use `@Query` with `LEFT JOIN FETCH` where needed
- Use `@Modifying` and `@Transactional` for delete operation
- Use `LOWER`, `CONCAT`, and `%` for partial, case-insensitive matching

### Doctor Repository

1. Open the `DoctorRepository.java` file.
2. Create a repository for the `Doctor` model by extending `JpaRepository`.

Add the following methods:

- `findByEmail`
- `findByNameLike`
- `findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase`
- `findBySpecialtyIgnoreCase`

#### Hint

Use Spring Data naming conventions and `@Query` with `LIKE`, `LOWER`, and `CONCAT` where needed.

### Patient Repository

1. Open the `PatientRepository.java` file.
2. Create a repository for the `Patient` model by extending `JpaRepository`.

Add the following methods:

- `findByEmail`
- `findByEmailOrPhone`

### Prescription Repository

1. Open the `PrescriptionRepository.java` file.
2. Create a repository for the `Prescription` model by extending `MongoRepository`.

Add the following method:

- `findByAppointmentId`

## Creating Services

### AppointmentService

Create the `AppointmentService` class and annotate it with `@Service`.

Declare these private dependencies:

- `AppointmentRepository`
- `PatientRepository`
- `DoctorRepository`
- `TokenService`

Add the following methods:

- `bookAppointment(Appointment appointment)` -> `int`
- `updateAppointment(Appointment appointment)` -> `ResponseEntity<Map<String, String>>`
- `cancelAppointment(long id, String token)` -> `ResponseEntity<Map<String, String>>`
- `getAppointment(String pname, LocalDate date, String token)` -> `Map<String, Object>`

#### Hints

- Use `appointmentRepository.save(appointment)` to save
- Check appointment existence before update
- Ensure the cancel flow validates the patient
- Filter doctor appointments by date and optional patient name

### DoctorService

Create the `DoctorService` class and annotate it with `@Service`.

Declare these private dependencies:

- `DoctorRepository`
- `AppointmentRepository`
- `TokenService`

Add the following methods:

- `getDoctorAvailability(Long doctorId, LocalDate date)` -> `List<String>`
- `saveDoctor(Doctor doctor)` -> `int`
- `updateDoctor(Doctor doctor)` -> `int`
- `getDoctors()` -> `List<Doctor>`
- `deleteDoctor(long id)` -> `int`
- `validateDoctor(Login login)` -> `ResponseEntity<Map<String, String>>`
- `findDoctorByName(String name)` -> `Map<String, Object>`
- `filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm)` -> `Map<String, Object>`
- `filterDoctorByNameAndTime(String name, String amOrPm)` -> `Map<String, Object>`
- `filterDoctorByNameAndSpecility(String name, String specilty)` -> `Map<String, Object>`
- `filterDoctorByTimeAndSpecility(String specilty, String amOrPm)` -> `Map<String, Object>`
- `filterDoctorBySpecility(String specilty)` -> `Map<String, Object>`
- `filterDoctorsByTime(String amOrPm)` -> `Map<String, Object>`
- `filterDoctorByTime(List<Doctor> doctors, String amOrPm)` -> `List<Doctor>` (private helper)

#### Hints

- Check for duplicate doctors by email before save
- Delete appointments tied to a doctor before deleting the doctor
- Filter availability by AM/PM

### PatientService

Open the `PatientService.java` file.

Add these methods:

- `createPatient(Patient patient)` -> `int`
- `getPatientAppointment(Long id, String token)` -> `ResponseEntity<Map<String, Object>>`
- `filterByCondition(String condition, Long id)` -> `ResponseEntity<Map<String, Object>>`
- `filterByDoctor(String name, Long patientId)` -> `ResponseEntity<Map<String, Object>>`
- `filterByDoctorAndCondition(String condition, String name, long patientId)` -> `ResponseEntity<Map<String, Object>>`
- `getPatientDetails(String token)` -> `ResponseEntity<Map<String, Object>>`

Use `AppointmentDTO` and `TokenService` to simplify appointment output and validate authorization.

### PrescriptionService

Open the `PrescriptionService.java` file.

Add these methods:

- `savePrescription(Prescription prescription)` -> `ResponseEntity<Map<String, String>>`
- `getPrescription(Long appointmentId)` -> `ResponseEntity<Map<String, Object>>`

#### Hints

- Save with `prescriptionRepository`
- Fetch with `prescriptionRepository.findByAppointmentId(appointmentId)`

### Service Class

Create the central `Service.java` class and annotate it with `@Service`.

Declare these private fields:

```java
private final TokenService tokenService;
private final AdminRepository adminRepository;
private final DoctorRepository doctorRepository;
private final PatientRepository patientRepository;
private final DoctorService doctorService;
private final PatientService patientService;
```

Add the following methods:

- `validateToken(String token, String user)` -> `ResponseEntity<Map<String, String>>`
- `validateAdmin(Admin receivedAdmin)` -> `ResponseEntity<Map<String, String>>`
- `filterDoctor(String name, String specialty, String time)` -> `Map<String, Object>`
- `validateAppointment(Appointment appointment)` -> `int`
- `validatePatient(Patient patient)` -> `boolean`
- `validatePatientLogin(Login login)` -> `ResponseEntity<Map<String, String>>`
- `filterPatient(String condition, String name, String token)` -> `ResponseEntity<Map<String, Object>>`

#### Hints

- Use `tokenService.validateToken()` to validate tokens
- Use `tokenService.generateToken()` after successful admin or patient login
- Use `doctorService.getDoctorAvailability()` to validate appointment slots
- Use `patientRepository.findByEmailOrPhone()` to prevent duplicate patient registration

### TokenService

Create `TokenService.java` and annotate it with `@Component`.

Declare these private dependencies:

```java
private final AdminRepository adminRepository;
private final DoctorRepository doctorRepository;
private final PatientRepository patientRepository;
```

Add the following methods:

- `generateToken(String identifier)` -> `String`
- `extractIdentifier(String token)` -> `String`
- `validateToken(String token, String user)` -> `boolean`
- `getSigningKey()` -> `SecretKey`

#### Hints

- Use `Jwts.builder()` to create JWT tokens
- Set expiration to 7 days
- Use the secret configured in `application.properties`
- Validate tokens by matching the extracted identifier with the correct user type in the database

## Creating Controllers

### AdminController

1. Open `AdminController.java`
2. Annotate with `@RestController`
3. Use `@RequestMapping("${api.path}" + "admin")`
4. Autowire `Service`
5. Define `adminLogin` with `@PostMapping`
6. Accept `Admin` in request body
7. Call `service.validateAdmin()`

Return type:

- `ResponseEntity<Map<String, String>>`

### AppointmentController

1. Open `AppointmentController.java`
2. Annotate with `@RestController`
3. Use `@RequestMapping("/appointments")`
4. Autowire:
   - `AppointmentService`
   - `Service`

Define these endpoints:

- `@GetMapping("/{date}/{patientName}/{token}")`
- `@PostMapping("/{token}")`
- `@PutMapping("/{token}")`
- `@DeleteMapping("/{id}/{token}")`

Use `service.validateToken()` before doctor/patient operations and delegate to `appointmentService`.

### DoctorController

1. Open `DoctorController.java`
2. Annotate with `@RestController`
3. Use `@RequestMapping("${api.path}" + "doctor")`
4. Autowire:
   - `DoctorService`
   - `Service`

Define these endpoints:

- `@GetMapping("/availability/{user}/{doctorId}/{date}/{token}")`
- `@GetMapping`
- `@PostMapping("/{token}")`
- `@PostMapping("/login")`
- `@PutMapping("/{token}")`
- `@DeleteMapping("/{id}/{token}")`
- `@GetMapping("/filter/{name}/{time}/{speciality}")`

Expected messages in the lab include:

- `"Doctor added to db"`
- `"Doctor already exists"`
- `"Doctor updated"`
- `"Doctor not found"`
- `"Doctor deleted successfully"`
- `"Doctor not found with id"`

### PatientController

1. Open `PatientController.java`
2. Annotate with `@RestController`
3. Use `@RequestMapping("/patient")`
4. Autowire:
   - `PatientService`
   - `Service`

Define these endpoints:

- `@GetMapping("/{token}")`
- `@PostMapping()`
- `@PostMapping("/login")`
- `@GetMapping("/{id}/{token}")`
- `@GetMapping("/filter/{condition}/{name}/{token}")`

Expected messages include:

- `"Signup successful"`
- `"Patient with email id or phone no already exist"`
- `"Internal server error"`

### PrescriptionController

1. Open `PrescriptionController.java`
2. Annotate with `@RestController`
3. Use `@RequestMapping("${api.path}" + "prescription")`
4. Autowire:
   - `PrescriptionService`
   - `Service`

Define these endpoints:

- `@PostMapping("/{token}")`
- `@GetMapping("/{appointmentId}/{token}")`

Validate the doctor token before saving or retrieving prescriptions.

## ValidationFailed Class

The `ValidationFailed` class is a custom exception handler annotated with `@RestControllerAdvice`.

It handles `MethodArgumentNotValidException`, which occurs when request validation fails.

### Key Points

1. Use `@RestControllerAdvice` for global REST exception handling
2. Use `@ExceptionHandler(MethodArgumentNotValidException.class)`
3. Collect validation errors from `FieldError`
4. Build a `Map<String, String>` with key `"message"`
5. Return `ResponseEntity` with `BAD_REQUEST (400)`

## Test Your App

### 1. Set Up Configuration and Database

As per Module 3 / Lab: Adding Databases and Tables:

- Configure `application.properties` with your MySQL and MongoDB usernames and passwords
- Import the provided SQL and MongoDB data files into your databases

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the App Locally

```bash
mvn spring-boot:run
```

### 4. Launch on Port 8080

Once the app is running, open the application in your browser.

### 5. Update API Base URL in `config.js`

Open `config.js` and update:

```js
export const API_BASE_URL = "http://localhost:8080";
```

Replace `http://localhost:8080` with your actual app URL, such as:

```text
https://<your-theia-url>
```

Refresh the browser after changing it.

### 6. Test the endpoint using CURL

```bash
curl http://localhost:8080/doctor
```

This should return a list of doctors.

### 7. Test patient signup, login, and appointments

#### A) Sign Up

```text
curl -X POST <URL>/patient -H "Content-Type: application/json" -d '{"name":"name","email":"useremail","phone":"..."}'
```

#### B) Login

```text
curl -X POST <URL>/patient/login -H "Content-Type: application/json" -d '{"email":"email","password":"password"}'
```

#### C) Get Appointments

```bash
curl -i -X GET <URL>/patient/1/patient/<JWT-TOKEN> -H "Accept: application/json"
```

> **Note:** It is possible that no appointments exist yet for the selected patient. If that's the case, book an appointment through the application and re-run the command.

### 8. Test doctor filtering by specialty and time

```bash
curl -X GET <URL>/doctor/filter/null/09:00-10:00/Cardiologist
```

This returns a list of doctors who specialize in `Cardiologist` and are available from `09:00` to `10:00`.

## Deliverables

Please take the following screenshots and save them using the suggested filenames. You can save them in either `.png` or `.jpg` format.

- Admin Portal Login Screen  
  Filename: `Admin Portal.png` or `Admin Portal.jpg`

- Doctor Portal Login Screen  
  Filename: `Doctor Portal.png` or `Doctor Portal.jpg`

- Patient Portal Login Screen  
  Filename: `Patient Portal.png` or `Patient Portal.jpg`

- Admin Adding a Doctor  
  Filename: `Adding Doctor.png` or `Adding Doctor.jpg`

- Patient Searching for a Doctor  
  Filename: `Searching Doctor.png` or `Searching Doctor.jpg`

- Doctor Viewing Appointments  
  Filename: `Appointments List.png` or `Appointments List.jpg`

## Save your work

Before you finish this lab, make sure to add, commit, and push your updated code to your GitHub repository.

```bash
git add .
git commit -m "Completed DTO's, controllers, service classes, and repositories"
git push https://<your_github_username>:<your_personal_access_token>@github.com/<your_github_username>/java-database-capstone.git
```

### Example

```bash
git push https://johnsmith:ghp_ABC123xyzTOKEN@github.com/johnsmith/java-database-capstone.git
```

## Conclusion and Next Steps

### Conclusion

In this lab, you:

- Created DTOs to structure data transfer
- Developed controllers to handle incoming HTTP requests
- Built service classes to encapsulate business logic
- Used repositories to interact with the database layer

You also applied best practices such as:

- Separating concerns between layers (Controller -> Service -> Repository)
- Using DTOs to decouple internal models from external APIs
- Leveraging Spring Data to avoid boilerplate CRUD code
- Validating input data for reliability and security

### Next Steps

- Implement authentication and authorization using Spring Security
- Protect API endpoints based on user roles such as admin, doctor, or patient
- Store and verify passwords securely using hashing algorithms
- Apply role-based access control (RBAC) to secure sensitive operations

These enhancements will make your application production-ready.

## Author(s)

Skills Network Team  
Upkar Lidder


## Check list complete
Did you create a Data Transfer Object (DTO) class to represent appointment data?
Did you add the “findByUsername” method when building the AdminRepository?Did you add the “findByUsername” method when building the AdminRepository?
Did you use “@Query” and “LIKE” with “LOWER” and “CONCAT” when adding methods to the DoctorRepository interface?
Did you create a service class to manage appointment-related operations?
Did you create a service class to manage JSON Web Token (JWT) token generation, extraction, and validation?
Did you add a controller to manage all CRUD operations related to appointments? 
Did you save and commit the file?
Did you push the file to GitHub successfully?