# Lab: Adding Databases and Tables

**Estimated time:** 45 minutes

---

## Learning Objectives

- Create and initialize a MySQL and MongoDB database for a **Smart Clinic Management** system.
- Insert sample data into relational and document-based tables.
- Understand the schema structure for a clinic backend.

---

## Prerequisites

- GitHub repo `java-database-capstone` cloned.
- GitHub Personal Access Token ready (with **repo** and **write** permissions; expiry 60 days).
- Previous labs completed.

---

## Overview

This lab walks you through:

1. Creating a **MySQL** database and tables for the Smart Clinic Management system using Spring Boot.
2. Inserting sample data into the **doctor**, **doctor_available_times**, **patient**, **appointment**, and **admin** tables.
3. Populating **MongoDB** with sample prescription documents.

All exercises are done in the **Cloud IDE** lab environment.

### About the Cloud IDE lab

- The lab environment is **ephemeral**: it is temporary and will be destroyed. **Push all changes to your GitHub repository** so you can recreate your work in a new lab later.
- The environment is **shared**. Do **not** store personal information, usernames, passwords, or access tokens here.

**Notes:**

1. When Git prompts for a password in Cloud IDE, use your **Personal Access Token** (not your GitHub password).
2. The environment may be recreated at any time; you may need to run **Initialize Development Environment** again.
3. Create a repository from the GitHub template provided for this lab before starting.

---

## Key Terms

| Term | Description |
|------|-------------|
| **Database tables** | Structures that store data (e.g. `appointment`, `doctor`, `patient`). |
| **SQL joins** | Combining rows from two or more tables using a related column (e.g. `doctor_id`, `patient_id`). |
| **Primary key** | A unique identifier for each row in a table. |
| **Foreign key** | A key that links one table to another (e.g. `doctor_id` in `appointment`). |
| **Aggregation functions** | Functions such as `COUNT()`, `SUM()`, `MAX()` for calculations (e.g. counting patients per doctor). |

---

## Getting Started

### Step 1: Create the database

In the **MySQL CLI**, run:

```sql
CREATE DATABASE cms;
```

### Step 2: Configure the application

1. Open `app/src/main/resources/application.properties` in your IDE.
2. Set the correct **username** and **password** for your MySQL instance.

### Step 3: Run the Spring Boot application

1. Open a terminal: **Terminal → New Terminal**.
2. Run:

```bash
cd java-database-capstone/app
mvn spring-boot:run
```

Spring Boot will start the backend and **create the tables automatically** in the `cms` database.

### Step 4: Verify tables

In MySQL:

```sql
USE cms;
SHOW TABLES;
```

You should see:

```
+------------------------+
| Tables_in_cms          |
+------------------------+
| admin                  |
| appointment            |
| doctor                 |
| doctor_available_times |
| patient                |
+------------------------+
```

**Role of each table:**

| Table | Purpose |
|-------|---------|
| `admin` | Admin user details. |
| `appointment` | Patient appointment details. |
| `doctor` | Doctor information. |
| `doctor_available_times` | Doctor availability time slots. |
| `patient` | Patient information. |

---

## Insert Data into Tables

Use the SQL below to insert sample data. Run each section in order (doctor → doctor_available_times → patient → appointment → admin).

---

### 1. Insert into `doctor` table

```sql
INSERT INTO doctor (email, name, password, phone, specialty) VALUES
('dr.adams@example.com', 'Dr. Emily Adams', 'pass12345', '555-101-2020', 'Cardiologist'),
('dr.johnson@example.com', 'Dr. Mark Johnson', 'secure4567', '555-202-3030', 'Neurologist'),
('dr.lee@example.com', 'Dr. Sarah Lee', 'leePass987', '555-303-4040', 'Orthopedist'),
('dr.wilson@example.com', 'Dr. Tom Wilson', 'w!ls0nPwd', '555-404-5050', 'Pediatrician'),
('dr.brown@example.com', 'Dr. Alice Brown', 'brownie123', '555-505-6060', 'Dermatologist'),
('dr.taylor@example.com', 'Dr. Taylor Grant', 'taylor321', '555-606-7070', 'Cardiologist'),
('dr.white@example.com', 'Dr. Sam White', 'whiteSecure1', '555-707-8080', 'Neurologist'),
('dr.clark@example.com', 'Dr. Emma Clark', 'clarkPass456', '555-808-9090', 'Orthopedist'),
('dr.davis@example.com', 'Dr. Olivia Davis', 'davis789', '555-909-0101', 'Pediatrician'),
('dr.miller@example.com', 'Dr. Henry Miller', 'millertime!', '555-010-1111', 'Dermatologist'),
('dr.moore@example.com', 'Dr. Ella Moore', 'ellapass33', '555-111-2222', 'Cardiologist'),
('dr.martin@example.com', 'Dr. Leo Martin', 'martinpass', '555-222-3333', 'Neurologist'),
('dr.jackson@example.com', 'Dr. Ivy Jackson', 'jackson11', '555-333-4444', 'Orthopedist'),
('dr.thomas@example.com', 'Dr. Owen Thomas', 'thomasPWD', '555-444-5555', 'Pediatrician'),
('dr.hall@example.com', 'Dr. Ava Hall', 'hallhall', '555-555-6666', 'Dermatologist'),
('dr.green@example.com', 'Dr. Mia Green', 'greenleaf', '555-666-7777', 'Cardiologist'),
('dr.baker@example.com', 'Dr. Jack Baker', 'bakeitup', '555-777-8888', 'Neurologist'),
('dr.walker@example.com', 'Dr. Nora Walker', 'walkpass12', '555-888-9999', 'Orthopedist'),
('dr.young@example.com', 'Dr. Liam Young', 'young123', '555-999-0000', 'Pediatrician'),
('dr.king@example.com', 'Dr. Zoe King', 'kingkong1', '555-000-1111', 'Dermatologist'),
('dr.scott@example.com', 'Dr. Lily Scott', 'scottish', '555-111-2223', 'Cardiologist'),
('dr.evans@example.com', 'Dr. Lucas Evans', 'evansEv1', '555-222-3334', 'Neurologist'),
('dr.turner@example.com', 'Dr. Grace Turner', 'turnerBurner', '555-333-4445', 'Orthopedist'),
('dr.hill@example.com', 'Dr. Ethan Hill', 'hillclimb', '555-444-5556', 'Pediatrician'),
('dr.ward@example.com', 'Dr. Ruby Ward', 'wardWard', '555-555-6667', 'Dermatologist');
```

---

### 2. Insert into `doctor_available_times` table

```sql
INSERT INTO doctor_available_times (doctor_id, available_times) VALUES
(1, '09:00-10:00'), (1, '10:00-11:00'), (1, '11:00-12:00'), (1, '14:00-15:00'),
(2, '10:00-11:00'), (2, '11:00-12:00'), (2, '14:00-15:00'), (2, '15:00-16:00'),
(3, '09:00-10:00'), (3, '11:00-12:00'), (3, '14:00-15:00'), (3, '16:00-17:00'),
(4, '09:00-10:00'), (4, '10:00-11:00'), (4, '15:00-16:00'), (4, '16:00-17:00'),
(5, '09:00-10:00'), (5, '10:00-11:00'), (5, '14:00-15:00'), (5, '15:00-16:00'),
(6, '09:00-10:00'), (6, '10:00-11:00'), (6, '11:00-12:00'), (6, '14:00-15:00'),
(7, '09:00-10:00'), (7, '10:00-11:00'), (7, '15:00-16:00'), (7, '16:00-17:00'),
(8, '10:00-11:00'), (8, '11:00-12:00'), (8, '14:00-15:00'), (8, '15:00-16:00'),
(9, '09:00-10:00'), (9, '11:00-12:00'), (9, '13:00-14:00'), (9, '14:00-15:00'),
(10, '10:00-11:00'), (10, '11:00-12:00'), (10, '14:00-15:00'), (10, '16:00-17:00'),
(11, '09:00-10:00'), (11, '12:00-13:00'), (11, '14:00-15:00'), (11, '15:00-16:00'),
(12, '10:00-11:00'), (12, '11:00-12:00'), (12, '13:00-14:00'), (12, '14:00-15:00'),
(13, '13:00-14:00'), (13, '14:00-15:00'), (13, '15:00-16:00'), (13, '16:00-17:00'),
(14, '09:00-10:00'), (14, '10:00-11:00'), (14, '14:00-15:00'), (14, '16:00-17:00'),
(15, '10:00-11:00'), (15, '11:00-12:00'), (15, '13:00-14:00'), (15, '14:00-15:00'),
(16, '09:00-10:00'), (16, '11:00-12:00'), (16, '14:00-15:00'), (16, '16:00-17:00'),
(17, '09:00-10:00'), (17, '10:00-11:00'), (17, '11:00-12:00'), (17, '12:00-13:00'),
(18, '09:00-10:00'), (18, '10:00-11:00'), (18, '11:00-12:00'), (18, '15:00-16:00'),
(19, '13:00-14:00'), (19, '14:00-15:00'), (19, '15:00-16:00'), (19, '16:00-17:00'),
(20, '10:00-11:00'), (20, '13:00-14:00'), (20, '14:00-15:00'), (20, '15:00-16:00'),
(21, '09:00-10:00'), (21, '10:00-11:00'), (21, '14:00-15:00'), (21, '15:00-16:00'),
(22, '10:00-11:00'), (22, '11:00-12:00'), (22, '14:00-15:00'), (22, '16:00-17:00'),
(23, '11:00-12:00'), (23, '13:00-14:00'), (23, '15:00-16:00'), (23, '16:00-17:00'),
(24, '12:00-13:00'), (24, '13:00-14:00'), (24, '14:00-15:00'), (24, '15:00-16:00'),
(25, '09:00-10:00'), (25, '10:00-11:00'), (25, '14:00-15:00'), (25, '15:00-16:00');
```

---

### 3. Insert into `patient` table

```sql
INSERT INTO patient (address, email, name, password, phone) VALUES
('101 Oak St, Cityville', 'jane.doe@example.com', 'Jane Doe', 'passJane1', '888-111-1111'),
('202 Maple Rd, Townsville', 'john.smith@example.com', 'John Smith', 'smithSecure', '888-222-2222'),
('303 Pine Ave, Villageton', 'emily.rose@example.com', 'Emily Rose', 'emilyPass99', '888-333-3333'),
('404 Birch Ln, Metropolis', 'michael.j@example.com', 'Michael Jordan', 'airmj23', '888-444-4444'),
('505 Cedar Blvd, Springfield', 'olivia.m@example.com', 'Olivia Moon', 'moonshine12', '888-555-5555'),
('606 Spruce Ct, Gotham', 'liam.k@example.com', 'Liam King', 'king321', '888-666-6666'),
('707 Aspen Dr, Riverdale', 'sophia.l@example.com', 'Sophia Lane', 'sophieLane', '888-777-7777'),
('808 Elm St, Newtown', 'noah.b@example.com', 'Noah Brooks', 'noahBest!', '888-888-8888'),
('909 Willow Way, Star City', 'ava.d@example.com', 'Ava Daniels', 'avaSecure8', '888-999-9999'),
('111 Chestnut Pl, Midvale', 'william.h@example.com', 'William Harris', 'willH2025', '888-000-0000'),
('112 Redwood St, Fairview', 'mia.g@example.com', 'Mia Green', 'miagreen1', '889-111-1111'),
('113 Cypress Rd, Edgewater', 'james.b@example.com', 'James Brown', 'jamiebrown', '889-222-2222'),
('114 Poplar Ave, Crestwood', 'amelia.c@example.com', 'Amelia Clark', 'ameliacool', '889-333-3333'),
('115 Sequoia Dr, Elmwood', 'ben.j@example.com', 'Ben Johnson', 'bennyJ', '889-444-4444'),
('116 Palm Blvd, Harborview', 'ella.m@example.com', 'Ella Monroe', 'ellam123', '889-555-5555'),
('117 Cottonwood Ct, Laketown', 'lucas.t@example.com', 'Lucas Turner', 'lucasTurn', '889-666-6666'),
('118 Sycamore Ln, Hilltop', 'grace.s@example.com', 'Grace Scott', 'graceful', '889-777-7777'),
('119 Magnolia Pl, Brookside', 'ethan.h@example.com', 'Ethan Hill', 'hill2025', '889-888-8888'),
('120 Fir St, Woodland', 'ruby.w@example.com', 'Ruby Ward', 'rubypass', '889-999-9999'),
('121 Beech Way, Lakeside', 'jack.b@example.com', 'Jack Baker', 'bakerjack', '889-000-0000'),
('122 Alder Ave, Pinehill', 'mia.h@example.com', 'Mia Hall', 'hallMia', '890-111-1111'),
('123 Hawthorn Blvd, Meadowbrook', 'owen.t@example.com', 'Owen Thomas', 'owen123', '890-222-2222'),
('124 Dogwood Dr, Summit', 'ivy.j@example.com', 'Ivy Jackson', 'ivyIvy', '890-333-3333'),
('125 Juniper Ct, Greenwood', 'leo.m@example.com', 'Leo Martin', 'leopass', '890-444-4444'),
('126 Olive Rd, Ashville', 'ella.moore@example.com', 'Ella Moore', 'ellamoore', '890-555-5555');
```

---

### 4. Insert into `appointment` table

```sql
INSERT INTO appointment (appointment_time, status, doctor_id, patient_id) VALUES
('2025-05-01 09:00:00.000000', 0, 1, 1),
('2025-05-02 10:00:00.000000', 0, 1, 2),
('2025-05-03 11:00:00.000000', 0, 1, 3),
('2025-05-04 14:00:00.000000', 0, 1, 4),
('2025-05-05 15:00:00.000000', 0, 1, 5),
('2025-05-06 13:00:00.000000', 0, 1, 6),
('2025-05-07 09:00:00.000000', 0, 1, 7),
('2025-05-08 16:00:00.000000', 0, 1, 8),
('2025-05-09 11:00:00.000000', 0, 1, 9),
('2025-05-10 10:00:00.000000', 0, 1, 10),
('2025-05-11 12:00:00.000000', 0, 1, 11),
('2025-05-12 15:00:00.000000', 0, 1, 12),
('2025-05-13 13:00:00.000000', 0, 1, 13),
('2025-05-14 10:00:00.000000', 0, 1, 14),
('2025-05-15 11:00:00.000000', 0, 1, 15),
('2025-05-16 14:00:00.000000', 0, 1, 16),
('2025-05-17 09:00:00.000000', 0, 1, 17),
('2025-05-18 12:00:00.000000', 0, 1, 18),
('2025-05-19 13:00:00.000000', 0, 1, 19),
('2025-05-20 16:00:00.000000', 0, 1, 20),
('2025-05-21 14:00:00.000000', 0, 1, 21),
('2025-05-22 10:00:00.000000', 0, 1, 22),
('2025-05-23 11:00:00.000000', 0, 1, 23),
('2025-05-24 15:00:00.000000', 0, 1, 24),
('2025-05-25 09:00:00.000000', 0, 1, 25),
('2025-05-01 10:00:00.000000', 0, 2, 1),
('2025-05-02 11:00:00.000000', 0, 3, 2),
('2025-05-03 14:00:00.000000', 0, 4, 3),
('2025-05-04 15:00:00.000000', 0, 5, 4),
('2025-05-05 10:00:00.000000', 0, 6, 5),
('2025-05-06 11:00:00.000000', 0, 7, 6),
('2025-05-07 14:00:00.000000', 0, 8, 7),
('2025-05-08 15:00:00.000000', 0, 9, 8),
('2025-05-09 10:00:00.000000', 0, 10, 9),
('2025-05-10 14:00:00.000000', 0, 11, 10),
('2025-05-11 13:00:00.000000', 0, 12, 11),
('2025-05-12 14:00:00.000000', 0, 13, 12),
('2025-05-13 15:00:00.000000', 0, 14, 13),
('2025-05-14 10:00:00.000000', 0, 15, 14),
('2025-05-15 11:00:00.000000', 0, 16, 15),
('2025-05-16 14:00:00.000000', 0, 17, 16),
('2025-05-17 10:00:00.000000', 0, 18, 17),
('2025-05-18 13:00:00.000000', 0, 19, 18),
('2025-05-19 14:00:00.000000', 0, 20, 19),
('2025-05-20 11:00:00.000000', 0, 21, 20),
('2025-05-21 13:00:00.000000', 0, 22, 21),
('2025-05-22 14:00:00.000000', 0, 23, 22),
('2025-05-23 10:00:00.000000', 0, 24, 23),
('2025-05-24 15:00:00.000000', 0, 25, 24),
('2025-05-25 13:00:00.000000', 0, 25, 25),
('2025-04-01 10:00:00.000000', 1, 1, 2),
('2025-04-02 11:00:00.000000', 1, 2, 3),
('2025-04-03 14:00:00.000000', 1, 3, 4),
('2025-04-04 15:00:00.000000', 1, 4, 5),
('2025-04-05 10:00:00.000000', 1, 5, 6),
('2025-04-06 11:00:00.000000', 1, 6, 7),
('2025-04-07 14:00:00.000000', 1, 7, 8),
('2025-04-08 15:00:00.000000', 1, 8, 9),
('2025-04-09 10:00:00.000000', 1, 9, 10),
('2025-04-10 14:00:00.000000', 1, 10, 11),
('2025-04-11 13:00:00.000000', 1, 11, 12),
('2025-04-12 14:00:00.000000', 1, 12, 13),
('2025-04-13 15:00:00.000000', 1, 13, 14),
('2025-04-14 10:00:00.000000', 1, 14, 15),
('2025-04-15 11:00:00.000000', 1, 15, 16),
('2025-04-16 14:00:00.000000', 1, 16, 17),
('2025-04-17 10:00:00.000000', 1, 17, 18),
('2025-04-18 13:00:00.000000', 1, 18, 19),
('2025-04-19 14:00:00.000000', 1, 19, 20),
('2025-04-20 11:00:00.000000', 1, 20, 21),
('2025-04-21 13:00:00.000000', 1, 21, 22),
('2025-04-22 14:00:00.000000', 1, 22, 23),
('2025-04-23 10:00:00.000000', 1, 23, 24),
('2025-04-24 15:00:00.000000', 1, 24, 25),
('2025-04-25 13:00:00.000000', 1, 25, 25),
('2025-04-01 09:00:00.000000', 1, 1, 1),
('2025-04-02 10:00:00.000000', 1, 1, 2),
('2025-04-03 11:00:00.000000', 1, 1, 3),
('2025-04-04 14:00:00.000000', 1, 1, 4),
('2025-04-05 10:00:00.000000', 1, 1, 5),
('2025-04-10 10:00:00.000000', 1, 1, 6),
('2025-04-11 09:00:00.000000', 1, 1, 7),
('2025-04-14 13:00:00.000000', 1, 1, 8),
('2025-04-01 10:00:00.000000', 1, 2, 1),
('2025-04-01 11:00:00.000000', 1, 2, 2),
('2025-04-02 09:00:00.000000', 1, 2, 3),
('2025-04-02 10:00:00.000000', 1, 2, 4),
('2025-04-03 11:00:00.000000', 1, 2, 5),
('2025-04-03 12:00:00.000000', 1, 2, 6),
('2025-04-04 14:00:00.000000', 1, 2, 7),
('2025-04-04 15:00:00.000000', 1, 2, 8),
('2025-04-05 10:00:00.000000', 1, 2, 9),
('2025-04-05 11:00:00.000000', 1, 2, 10),
('2025-04-06 13:00:00.000000', 1, 2, 11),
('2025-04-06 14:00:00.000000', 1, 2, 12),
('2025-04-07 09:00:00.000000', 1, 2, 13),
('2025-04-07 10:00:00.000000', 1, 2, 14),
('2025-04-08 11:00:00.000000', 1, 2, 15),
('2025-04-08 12:00:00.000000', 1, 2, 16),
('2025-04-09 13:00:00.000000', 1, 2, 17),
('2025-04-09 14:00:00.000000', 1, 2, 18),
('2025-04-10 11:00:00.000000', 1, 2, 19),
('2025-04-10 12:00:00.000000', 1, 2, 20),
('2025-04-11 14:00:00.000000', 1, 2, 21),
('2025-04-11 15:00:00.000000', 1, 2, 22),
('2025-04-12 10:00:00.000000', 1, 2, 23),
('2025-04-12 11:00:00.000000', 1, 2, 24),
('2025-04-13 13:00:00.000000', 1, 2, 25),
('2025-04-13 14:00:00.000000', 1, 2, 1),
('2025-04-14 09:00:00.000000', 1, 2, 2),
('2025-04-14 10:00:00.000000', 1, 2, 3),
('2025-04-15 12:00:00.000000', 1, 2, 4),
('2025-04-15 13:00:00.000000', 1, 2, 5),
('2025-04-01 12:00:00.000000', 1, 3, 1),
('2025-04-02 11:00:00.000000', 1, 3, 2),
('2025-04-03 13:00:00.000000', 1, 3, 3),
('2025-04-04 15:00:00.000000', 1, 3, 4),
('2025-04-05 12:00:00.000000', 1, 3, 5),
('2025-04-08 13:00:00.000000', 1, 3, 6),
('2025-04-09 10:00:00.000000', 1, 3, 7),
('2025-04-10 14:00:00.000000', 1, 3, 8),
('2025-04-11 13:00:00.000000', 1, 3, 9),
('2025-04-12 09:00:00.000000', 1, 3, 10),
('2025-04-01 14:00:00.000000', 1, 4, 1),
('2025-04-02 12:00:00.000000', 1, 4, 2),
('2025-04-03 14:00:00.000000', 1, 4, 3),
('2025-04-04 16:00:00.000000', 1, 4, 4),
('2025-04-05 14:00:00.000000', 1, 4, 5),
('2025-04-09 11:00:00.000000', 1, 4, 6),
('2025-04-10 13:00:00.000000', 1, 4, 7);
```

---

### 5. Insert into `admin` table

```sql
INSERT INTO admin (username, password)
VALUES ('admin', 'admin@1234');
```

---

### 6. Insert into MongoDB `prescriptions` collection

In the **MongoDB shell** (or Mongo CLI):

```javascript
use prescriptions;

db.prescriptions.insertMany([
  {
    "_id": ObjectId("6807dd712725f013281e7201"),
    "patientName": "John Smith",
    "appointmentId": 51,
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7202"),
    "patientName": "Emily Rose",
    "appointmentId": 52,
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7203"),
    "patientName": "Michael Jordan",
    "appointmentId": 53,
    "medication": "Ibuprofen",
    "dosage": "400mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7204"),
    "patientName": "Olivia Moon",
    "appointmentId": 54,
    "medication": "Antihistamine",
    "dosage": "10mg",
    "doctorNotes": "Take 1 tablet daily before bed.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7205"),
    "patientName": "Liam King",
    "appointmentId": 55,
    "medication": "Vitamin C",
    "dosage": "1000mg",
    "doctorNotes": "Take 1 tablet daily.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7206"),
    "patientName": "Sophia Lane",
    "appointmentId": 56,
    "medication": "Antibiotics",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 12 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7207"),
    "patientName": "Noah Brooks",
    "appointmentId": 57,
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7208"),
    "patientName": "Ava Daniels",
    "appointmentId": 58,
    "medication": "Ibuprofen",
    "dosage": "200mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7209"),
    "patientName": "William Harris",
    "appointmentId": 59,
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7210"),
    "patientName": "Mia Green",
    "appointmentId": 60,
    "medication": "Vitamin D",
    "dosage": "1000 IU",
    "doctorNotes": "Take 1 tablet daily with food.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7211"),
    "patientName": "James Brown",
    "appointmentId": 61,
    "medication": "Antihistamine",
    "dosage": "10mg",
    "doctorNotes": "Take 1 tablet every morning.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7212"),
    "patientName": "Amelia Clark",
    "appointmentId": 62,
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7213"),
    "patientName": "Ben Johnson",
    "appointmentId": 63,
    "medication": "Ibuprofen",
    "dosage": "400mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7214"),
    "patientName": "Ella Monroe",
    "appointmentId": 64,
    "medication": "Vitamin C",
    "dosage": "1000mg",
    "doctorNotes": "Take 1 tablet daily.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7215"),
    "patientName": "Lucas Turner",
    "appointmentId": 65,
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7216"),
    "patientName": "Grace Scott",
    "appointmentId": 66,
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7217"),
    "patientName": "Ethan Hill",
    "appointmentId": 67,
    "medication": "Ibuprofen",
    "dosage": "400mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7218"),
    "patientName": "Ruby Ward",
    "appointmentId": 68,
    "medication": "Vitamin D",
    "dosage": "1000 IU",
    "doctorNotes": "Take 1 tablet daily with food.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7219"),
    "patientName": "Jack Baker",
    "appointmentId": 69,
    "medication": "Antibiotics",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 12 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7220"),
    "patientName": "Mia Hall",
    "appointmentId": 70,
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7221"),
    "patientName": "Owen Thomas",
    "appointmentId": 71,
    "medication": "Ibuprofen",
    "dosage": "200mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7222"),
    "patientName": "Ivy Jackson",
    "appointmentId": 72,
    "medication": "Antihistamine",
    "dosage": "10mg",
    "doctorNotes": "Take 1 tablet every morning.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7223"),
    "patientName": "Leo Martin",
    "appointmentId": 73,
    "medication": "Vitamin C",
    "dosage": "1000mg",
    "doctorNotes": "Take 1 tablet daily.",
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7224"),
    "patientName": "Ella Moore",
    "appointmentId": 74,
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "_class": "com.project.back_end.models.Prescription"
  }
]);
```

---

## Conclusion and Next Steps

In this lab you:

- Created a MySQL database and tables for the Smart Clinic Management system.
- Inserted sample data into the MySQL tables (doctor, doctor_available_times, patient, appointment, admin).
- Added sample documents to the MongoDB `prescriptions` collection.

### Next steps: Verify the CMS database

Confirm that all tables and data are set up correctly. **For the assignment, submit the complete terminal output from each command below.** Save the output for all commands.

#### MySQL verification

Run in the **MySQL CLI**:

**View doctors (first 5):**

```sql
SELECT * FROM doctor LIMIT 5;
```

**View doctor availability (first 5):**

```sql
SELECT * FROM doctor_available_times LIMIT 5;
```

**View patients (first 5):**

```sql
SELECT * FROM patient LIMIT 5;
```

**View appointments (first 5 by time):**

```sql
SELECT * FROM appointment ORDER BY appointment_time LIMIT 5;
```

**View admin:**

```sql
SELECT * FROM admin;
```

#### MongoDB verification

In the **Mongo CLI**:

```javascript
use prescriptions;
db.prescriptions.find().limit(5).pretty();
```

If any data is missing or incorrect, re-run the corresponding INSERT section above.

---

1. **Did you create the MySQL instance in the IDE environment?**
   - [ ] Yes  
   - [ ] No  

2. **Did you create a database called `cms` by executing `create database cms;` in the MySQL CLI?**
   - [ ] Yes  
   - [ ] No  

3. **Did you insert data into the tables for the various users?**
   - [ ] Yes  
   - [ ] No  

4. **Did you complete the “Insert into prescriptions collection in MongoDB” step?**
   - [ ] Yes  
   - [ ] No  

5. **Did you commit and push your changes to GitHub successfully?**
   - [ ] Yes  
   - [ ] No  
