# Lab: Building Models with Validations

**Estimated time needed:** 45 minutes

## Learning objectives

After completing this lab, you will be able to:

- Create and annotate Java model classes using JPA and Hibernate
- Apply validation annotations to enforce data integrity
- Manage entity relationships and reference mappings
- Implement helper methods and exclude them from persistence
- Use `@JsonProperty` to control JSON serialization
- Work with MongoDB using Spring Data's `@Document`

Welcome to this lab on **Building Models with Validations**.

## Prerequisites

- Ensure you have cloned your GitHub repo `java-database-capstone`
- Ensure you have generated **GitHub Personal Access Token** to push your work back into your GitHub repository for peer review
- This lab builds on previous labs, please ensure you have completed the previous labs before starting this lab

## Overview

In this lab, you will create Java model classes that represent the core entities of a Clinic Management System, including admins, doctors, patients, appointments, and prescriptions. You will annotate these models using JPA and Hibernate for relational data and Spring Data MongoDB for document storage. Along the way, you'll apply validation rules, control JSON serialization to protect sensitive information, define entity relationships, and implement helper methods for dynamic logic. These models form the foundation of the backend system that powers the clinic portal.

To perform the exercises given in this lab, you'll use Cloud IDE lab environment. You will be able to build robust Java model classes for use in a Spring Boot backend application.

## About Cloud IDE lab

It is important to understand that the lab environment is **ephemeral**. It only lives for a short while and then it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time later.

Also, this environment is shared. It is recommended not to store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

> **Note:** If you haven't generated a GitHub Personal Access Token, you should do so now. You will need it to push code back to your repository. It should have `repo` and `write:packages`, and set to expire in **60 days**. When Git prompts for a password in the Cloud IDE environment, use your Personal Access Token instead. The environment may be recreated at any time so you may find that you have to perform the **Initialize Development Environment** step each time the environment is created. Create a repository from the GitHub template provided for this lab in the next step.

## Project Structure and Key Terms

### Project Structure

```text
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── project
│   │   │           └── back_end
│   │   │               └── models
│   │   │                   ├── Admin.java
│   │   │                   ├── Appointment.java
│   │   │                   ├── Doctor.java
│   │   │                   ├── Patient.java
│   │   │                   └── Prescription.java
│   │   └── resources
│   └── test
│       ├── java
│       └── resources
```

### Explanation

- `pom.xml`: The core configuration file for the Maven build system. It manages project dependencies, plugins, build profiles, and other settings required to compile, test, and package the Java application.
- `src/main/java`: Contains the primary source code for the application. This is where the core logic, controllers, services, models, and other business-related classes are placed. It's the heart of your backend application.
- `src/main/resources`: Stores non-code assets used by the application such as application configuration files (e.g., `application.properties`, `log4j.xml`), static resources, and other environment-specific data.
- `src/test/java`: Includes all test-related Java classes, such as unit tests and integration tests. It ensures that your application logic is working as expected by verifying components in isolation or combined.

### Key terms

Here are some important terms and annotations you will use while building your model classes in this lab.

- **JPA Entity**: A Java class mapped to a database table
- `@Entity`: Annotation to mark a class as a JPA entity
- `@Id` / `@GeneratedValue`: Used to define primary key fields
- `@NotNull`, `@Size`, `@Email`: Hibernate annotations for validation
- `@ManyToOne`: Defines a many-to-one relationship between entities
- `@Future`: Ensures date/time fields are set in the future
- `@JsonProperty`: Controls how fields are handled in JSON
- `@Transient`: Marks fields/methods to be excluded from persistence
- `@Document`: Annotation for defining MongoDB documents

## Admin model

The `Admin` model represents system administrators who have access to manage the backend portal of the Clinic Management System. Admins typically handle high-level operations such as user access, data review, and system maintenance. This model contains basic login credentials required to authenticate an admin.

1. Open the `Admin.java` file: `src/main/java/com/project/back_end/models/Admin.java`

2. Add the following attributes along with getters and setters:

- `id`: `private Long` — Auto-incremented primary key
- `username`: `private String` — Cannot be null
- `password`: `private String` — Cannot be null. This field should be write-only in JSON responses.

### Hints

- Annotate the class with `@Entity` to indicate that it should be mapped to a database table.
- Mark the `id` field with `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)` so it auto-increments as the primary key.
- Use `@NotNull` on the `username` and `password` fields to ensure that these values are always provided. Example:

```java
@NotNull(message = "Username cannot be null")
```

- Annotate the password field with `@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)` so it is hidden from API responses but still accepted in incoming JSON requests.
- Add standard getters and setters for each field to allow Spring and Jackson to access the data.

### Tasks

- Define the class as a JPA entity
- Apply the required validation and JSON annotations to the fields
- Keep the implementation clean and modular to support future authentication or authorization features

## Appointment model

The `Appointment` model represents a scheduled meeting between a doctor and a patient. It includes metadata such as the date, time, status of the appointment, and helper methods to extract specific time-based information. This model links together the `Doctor` and `Patient` entities to form the core of the clinic's scheduling system.

1. Open the `Appointment.java` file: `src/main/java/com/project/back_end/models/Appointment.java`

2. Add the following attributes along with getters and setters:

- `id`: `private Long` — Auto-incremented primary key
- `doctor`: `private Doctor` — The doctor assigned to the appointment (**required**)
- `patient`: `private Patient` — The patient assigned to the appointment (**required**)
- `appointmentTime`: `private LocalDateTime` — The date and time of the appointment (**must be in the future**)
- `status`: `private int` — Status of the appointment (`0` for Scheduled, `1` for Completed) (**required**)

3. Add helper methods:

- `getEndTime()`: Returns the end time of the appointment (1 hour after start time)
- `getAppointmentDate()`: Returns only the date portion of the appointment
- `getAppointmentTimeOnly()`: Returns only the time portion of the appointment

### Hints

- Annotate the class with `@Entity` to indicate that it should be mapped to a JPA table.
- Mark `id` with:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

- Use `@ManyToOne` and `@NotNull` to define relationships to both `Doctor` and `Patient`.
- Apply `@Future` on `appointmentTime`:

```java
@Future(message = "Appointment time must be in the future")
```

- Mark the helper methods with `@Transient` so they aren't persisted in the database.
- Add standard getters and setters for all fields.

### Tasks

- Establish proper relationships between entities
- Implement custom methods to support UI display logic
- Validate appointment timing to prevent past scheduling

## Doctor model

The `Doctor` model stores information about healthcare providers, including their contact details, medical specialty, and availability. This model is crucial for mapping appointments and verifying doctor credentials.

1. Open the `Doctor.java` file: `src/main/java/com/project/back_end/models/Doctor.java`

2. Add the following attributes along with getters and setters:

- `id`: `private Long` — Auto-incremented primary key
- `name`: `private String` — Doctor's full name (**required, 3-100 characters**)
- `specialty`: `private String` — Medical specialty (**required, 3-50 characters**)
- `email`: `private String` — Valid email address (**required, must match email format**)
- `password`: `private String` — Password (**required, at least 6 characters, write-only in JSON**)
- `phone`: `private String` — Phone number (**required, must be 10 digits**)
- `availableTimes`: `private List<String>` — List of available time slots (Example: `"09:00-10:00"`)

### Hints

- Annotate the class with `@Entity`.
- Apply validation annotations like:

```java
@NotNull
@Size(min = 3, max = 100)
@Email
@Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
```

- For `password`, use:

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
```

- Use `@ElementCollection` on `availableTimes`:

```java
@ElementCollection
private List<String> availableTimes;
```

- Add getters and setters for all fields.

### Tasks

- Add detailed validations for fields
- Ensure that sensitive data is hidden from public APIs
- Structure time availability data using proper JPA techniques

## Patient model

The `Patient` model represents users who book appointments and receive treatment. It captures personal details like contact information and address, and links to appointments and prescriptions indirectly.

1. Open the `Patient.java` file: `src/main/java/com/project/back_end/models/Patient.java`

2. Add the following attributes along with getters and setters:

- `id`: `private Long` — Auto-incremented primary key
- `name`: `private String` — Patient's full name (**required, 3-100 characters**)
- `email`: `private String` — Valid email address (**required, must match email format**)
- `password`: `private String` — Password (**required, at least 6 characters**)
- `phone`: `private String` — Phone number (**required, must be 10 digits**)
- `address`: `private String` — Patient's address (**required, max 255 characters**)

### Hints

- Annotate the class with `@Entity`
- Apply validation annotations like:

```java
@NotNull
@Size(min = 3, max = 100)
@Email
@Size(min = 6)
@Pattern(regexp = "\\d{10}")
```

- Add `@Size(max = 255)` to the `address` field
- Don't forget to include standard getters and setters

### Tasks

- Validate inputs to ensure clean and accurate patient data
- Follow consistent design and annotation patterns
- Prepare this model for use in authentication and reporting features

## Prescription model

The `Prescription` model is a MongoDB document that stores medication instructions issued during appointments. It includes the patient's name, the referenced appointment, prescribed drugs, dosage, and optional notes from the doctor.

1. Open the `Prescription.java` file: `src/main/java/com/project/back_end/models/Prescription.java`

2. Add the following attributes along with getters and setters:

- `id`: `private String` — Unique identifier for the prescription (**MongoDB ID, auto-generated**)
- `patientName`: `private String` — Patient's full name (**required, 3-100 characters**)
- `appointmentId`: `private Long` — Reference to the appointment entity's ID (**required, must be a valid Long**)
- `medication`: `private String` — Name of the medication (**required, 3-100 characters**)
- `dosage`: `private String` — Dosage details (**required, 3-20 characters**)
- `doctorNotes`: `private String` — Optional field for any notes from the doctor (**max 200 characters**)

### Hints

- Annotate the class with:

```java
@Document(collection = "prescriptions")
```

- Use `@Id` to mark the MongoDB `_id` field
- Use `@NotNull` and `@Size` on all required fields to enforce string length and non-null constraints:

```java
@Size(min = 3, max = 100)
@Size(min = 3, max = 20)
@Size(max = 200)
```

- Implement a constructor to initialize the most important fields for easy object creation
- Add standard getters and setters

### Tasks

- Design a MongoDB-compatible class with strict validation
- Define a flexible schema using Spring Data MongoDB
- Include metadata relevant to prescriptions, while keeping doctor notes optional

## Save your work

### Pushing your work to GitHub

Before you finish this lab, make sure to **add, commit, and push** your updated code to your GitHub repository. You will be asked to provide your public GitHub repo URL for graded evaluation at the end of the capstone.

Follow these steps to push your changes:

1. Stage your changes:

```bash
git add .
```

2. Commit your changes with a meaningful message:

```bash
git commit -m "Completed model classes for Clinic Management System"
```

3. Push your changes using your GitHub Personal Access Token:

```bash
git push https://<your_github_username>:<your_personal_access_token>@github.com/<your_github_username>/java-database-capstone.git
```

- Replace `<your_github_username>` with your GitHub username
- Replace `<your_personal_access_token>` with the token you generated at the start of the lab

### Example

If your GitHub username is `johnsmith` and your token is `ghp_ABC123xyzTOKEN`, your push command would look like:

```bash
git push https://johnsmith:ghp_ABC123xyzTOKEN@github.com/johnsmith/java-database-capstone.git
```

### Remember

- Always push your work after completing each lab
- Save your GitHub URL - you'll need to submit it for your final project evaluation

## Conclusion and next steps

### Conclusion

Great job! You've successfully defined and annotated core model classes for the clinic management system using Spring Boot with both JPA and MongoDB. These models lay the foundation for the backend data structure, ensuring proper validation, secure data handling, and relational integrity.

### Next steps

Now that you have coded your models, you are ready to work on the rest of the code base. We encourage you to extend your model as your submission stands out.

Here are a few ideas to enhance your models further. This is completely optional and if you decide to add these models, you will also need to extend the remaining of the project to accommodate for this change.

- **Add additional fields** for more detailed information:
  - For `Doctor`: add `yearsOfExperience`, `clinicAddress`, or `rating`
  - For `Patient`: add `dateOfBirth`, `emergencyContact`, or `insuranceProvider`
  - For `Appointment`: add `reasonForVisit` or `notes`
  - For `Prescription`: add `refillCount` or `pharmacyName`

- **Apply more advanced validations** to ensure better data quality:
  - Use `@Pattern` to validate phone numbers with a specific format
  - Use `@Min` and `@Max` for fields like `yearsOfExperience` or `rating`
  - Use `@Past` for `dateOfBirth` to ensure dates are in the past
  - Limit string lengths using `@Size(min, max)` to avoid unexpected inputs

- **Enhance JSON handling**:
  - Use `@JsonIgnore` to hide internal fields you don't want exposed in API responses
  - Customize field names in JSON using `@JsonProperty("customName")`

By thoughtfully extending your models, you not only make your application more realistic but also demonstrate deeper knowledge of JPA, Hibernate, and MongoDB best practices.

## Author(s)

Skills Network Team  
Upkar Lidder
