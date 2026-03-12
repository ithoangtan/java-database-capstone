# Database Schema Design — Smart Clinic Management System

This document describes the schema design for both the relational (MySQL) and document-based (MongoDB) databases used by the Smart Clinic Management System.

---

## 1. Key Types of Data Identified

The smart clinic stores the following main data types:

| Data Type       | Description |
|----------------|-------------|
| **Patients**   | People who book and attend appointments (name, email, password, phone, address). |
| **Doctors**    | Medical staff with specialty, contact info, and available time slots. |
| **Appointments**| Scheduled sessions linking a patient to a doctor at a specific date/time with a status. |
| **Prescriptions** | Medical prescriptions (medication, dosage, notes) linked to an appointment. |
| **Admin Users**| System administrators (username/password) for managing the clinic. |

These cover core clinic operations: identity (Admin, Doctor, Patient), scheduling (Appointment), and clinical data (Prescription).

---

## 2. Data Placement: MySQL vs MongoDB

| Storage        | Use Case | Data Stored |
|----------------|----------|-------------|
| **MySQL (relational)** | Structured, transactional data with relationships and integrity constraints. | Admin, Doctor, Patient, Appointment. |
| **MongoDB (document)**| Semi-structured, flexible documents that may evolve (e.g. prescriptions, notes). | Prescriptions (and optional future clinical documents). |

**Rationale:**

- **MySQL**: Identity, users, and appointments need strong consistency, foreign keys (e.g. appointment → doctor, patient), and ACID transactions.
- **MongoDB**: Prescriptions benefit from a flexible document model and optional nested structures (e.g. multiple medications, notes) without rigid table migrations.

---

## 3. MySQL Schema (Relational)

Database name: **`cms`** (Clinic Management System).

### 3.1 Table: `admin`

| Column     | Type         | Constraints | Description |
|-----------|--------------|-------------|-------------|
| `id`      | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique admin ID. |
| `username`| VARCHAR(255) | NOT NULL    | Login username. |
| `password`| VARCHAR(255) | NOT NULL    | Hashed password (write-only in API). |

### 3.2 Table: `doctor`

| Column            | Type         | Constraints | Description |
|-------------------|--------------|-------------|-------------|
| `id`              | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique doctor ID. |
| `name`            | VARCHAR(100) | NOT NULL, length 3–100 | Full name. |
| `specialty`       | VARCHAR(50)  | NOT NULL, length 3–50  | Medical specialty. |
| `email`           | VARCHAR(255) | NOT NULL, valid email  | Login and contact. |
| `password`        | VARCHAR(255) | NOT NULL, min length 6 | Hashed password. |
| `phone`           | VARCHAR(10)  | NOT NULL, exactly 10 digits | Phone number. |

**Separate collection (JPA `@ElementCollection`):** `doctor_available_times` — stores available time slots (e.g. `"09:00-10:00"`) for each doctor.

### 3.3 Table: `patient`

| Column     | Type         | Constraints | Description |
|-----------|--------------|-------------|-------------|
| `id`      | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique patient ID. |
| `name`    | VARCHAR(100) | NOT NULL, length 3–100 | Full name. |
| `email`   | VARCHAR(255) | NOT NULL, valid email  | Login and contact. |
| `password`| VARCHAR(255) | NOT NULL, min length 6 | Hashed password. |
| `phone`   | VARCHAR(10)  | NOT NULL, exactly 10 digits | Phone number. |
| `address` | VARCHAR(255) | NOT NULL, max 255      | Address. |

### 3.4 Table: `appointment`

| Column             | Type        | Constraints | Description |
|--------------------|-------------|-------------|-------------|
| `id`               | BIGINT      | PRIMARY KEY, AUTO_INCREMENT | Unique appointment ID. |
| `doctor_id`        | BIGINT      | NOT NULL, FOREIGN KEY → `doctor(id)` | Assigned doctor. |
| `patient_id`       | BIGINT      | NOT NULL, FOREIGN KEY → `patient(id)` | Assigned patient. |
| `appointment_time` | DATETIME    | NOT NULL, future when created | Scheduled date and time. |
| `status`           | INT         | NOT NULL    | 0 = scheduled, 1 = completed. |

**Relationships:**

- Many appointments → one doctor (`@ManyToOne`).
- Many appointments → one patient (`@ManyToOne`).

---

## 4. MongoDB Schema (Document-Based)

Database name: **`prescriptions`** (or as configured in `spring.data.mongodb.uri`).

### 4.1 Collection: `prescriptions`

Each document represents one prescription, with a flat structure and optional nested structures for future use.

**Document structure (current):**

| Field            | Type   | Constraints | Description |
|------------------|--------|-------------|-------------|
| `_id`            | ObjectId (String) | Required | Unique document ID. |
| `patientName`    | String | Required, length 3–100 | Patient name. |
| `appointmentId`  | Long   | Required   | Reference to MySQL `appointment.id`. |
| `medication`     | String | Required, length 3–100 | Medication name. |
| `dosage`         | String | Required   | Dosage instructions. |
| `doctorNotes`    | String | Optional, max 200 | Doctor notes. |

**Example document:**

```json
{
  "_id": "507f1f77bcf86cd799439011",
  "patientName": "Nguyen Van A",
  "appointmentId": 42,
  "medication": "Paracetamol 500mg",
  "dosage": "1 tablet every 6 hours",
  "doctorNotes": "Take after meals. Avoid alcohol."
}
```

### 4.2 Nested Structures (Optional / Future)

MongoDB allows nested structures without changing a fixed table schema. For example, a prescription could store **multiple medications** in a single document:

```json
{
  "_id": "507f1f77bcf86cd799439011",
  "patientName": "Nguyen Van A",
  "appointmentId": 42,
  "medications": [
    { "name": "Paracetamol 500mg", "dosage": "1 tablet every 6 hours" },
    { "name": "Vitamin C", "dosage": "1 tablet daily" }
  ],
  "doctorNotes": "Take after meals."
}
```

The current implementation uses a single medication per document; the schema can be extended to use nested `medications` when needed.

---

## 5. Checklist Summary

| # | Requirement | Status |
|---|-------------|--------|
| 1 | Key data types identified (patients, doctors, appointments, prescriptions, admin) | Yes — Section 1. |
| 2 | Data split: MySQL (relational) vs MongoDB (document) | Yes — Section 2. |
| 3 | Markdown file documenting schema for both databases | Yes — this file (`schema-design.md`). |
| 4 | Schema contains tables for Doctors, Patients, Admin Users | Yes — Sections 3.2, 3.3, 3.1. |
| 5 | MongoDB collections and document structures (with nested if needed) | Yes — Section 4 (structure + nested example). |
| 6 | Commit and push to GitHub | To be done by you after adding this file. |

---

## 6. Implementation Reference

- **MySQL entities (JPA):** `Admin`, `Doctor`, `Patient`, `Appointment` in `com.project.back_end.models`.
- **MongoDB document:** `Prescription` in `com.project.back_end.models` with `@Document(collection = "prescriptions")`.
- Tables are created/updated by Hibernate from entities (`spring.jpa.hibernate.ddl-auto=update` in `application.properties`).
