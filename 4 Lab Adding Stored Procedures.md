# Lab: Adding Stored Procedures

**Estimated time:** 30 minutes

## Learning objectives

After completing this lab, you will be able to:

- Write stored procedures to generate daily and summary reports
- Use SQL joins to query across multiple tables
- Practice executing stored procedures with parameters

## Prerequisites

- Completion of the previous lab on creating the CMS database and inserting data

## Overview

This lab demonstrates how to create stored procedures for reports. In this lab, you will write SQL stored procedures to automate report generation and generate reports using SQL joins and aggregation.

To perform the exercises given in this lab, you'll use Cloud IDE lab environment. This is where all of your development will take place.

## About Cloud IDE lab

It is important to understand that the lab environment is **ephemeral**. It only lives for a short while and then, it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time later.

Also, this environment is shared. It is recommended not to store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

> **Note:** If you haven't generated a GitHub Personal Access Token, you should do so now. You will need it to push code back to your repository. It should have `repo` and `write` permissions, and set to expire in **60 days**. When Git prompts you for a password in the Cloud IDE environment, use your Personal Access Token instead. The environment may be recreated at any time so you may find that you have to perform the **Initialize Development Environment** step each time the environment is created. Create a repository from the GitHub template provided for this lab in the next step.

## Key Terms

- **Stored Procedure:** A saved collection of SQL statements that can be executed with a single call, often used for automation and reporting.
- **SQL JOIN:** Combines rows from two or more tables based on a related column between them. Example: `doctor_id`, `patient_id`.
- **DELIMITER:** A command used in MySQL to temporarily change the statement delimiter, allowing multi-statement procedures to be created.
- **CALL:** The SQL command used to invoke or run a stored procedure.
- **Input Parameters:** Variables passed into a procedure when it's called. Example: `report_date`, `input_month`, `input_year`.
- **GROUP BY:** A clause used to group rows that have the same values in specified columns, often used with aggregate functions.
- **ORDER BY:** Used to sort the result set based on one or more columns.
- **Aggregation Function:** A function like `COUNT()` that operates on sets of values to return a single summarizing value.

## Daily Appointment Report by Doctor

**Procedure:** `GetDailyAppointmentReportByDoctor`

This procedure generates a report listing all appointments on a specific date, grouped by doctor. It displays the doctor's name, appointment time, appointment status, and the patient's name and phone number. This is useful for daily operational reviews in the clinic.

### 1. Define the stored procedure

```sql
DELIMITER $$

CREATE PROCEDURE GetDailyAppointmentReportByDoctor(
    IN report_date DATE
)
BEGIN
    SELECT
        d.name AS doctor_name,
        a.appointment_time,
        a.status,
        p.name AS patient_name,
        p.phone AS patient_phone
    FROM
        appointment a
    JOIN
        doctor d ON a.doctor_id = d.id
    JOIN
        patient p ON a.patient_id = p.id
    WHERE
        DATE(a.appointment_time) = report_date
    ORDER BY
        d.name, a.appointment_time;
END$$

DELIMITER ;
```

### 2. Run the stored procedure

```sql
CALL GetDailyAppointmentReportByDoctor('2025-04-15');
```

> **Note:** For the assignment, you need to submit the complete output from each stored procedure execution. Save the terminal output.

## Doctor with Most Patients By Month

**Procedure:** `GetDoctorWithMostPatientsByMonth`

This procedure identifies the doctor who saw the most patients in a given month and year. It helps clinic managers understand which doctor had the highest patient load during a time period.

### 1. Define the stored procedure

```sql
DELIMITER $$

CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(
    IN input_month INT,
    IN input_year INT
)
BEGIN
    SELECT
        doctor_id,
        COUNT(patient_id) AS patients_seen
    FROM
        appointment
    WHERE
        MONTH(appointment_time) = input_month
        AND YEAR(appointment_time) = input_year
    GROUP BY
        doctor_id
    ORDER BY
        patients_seen DESC
    LIMIT 1;
END $$

DELIMITER ;
```

### 2. Run the stored procedure

```sql
CALL GetDoctorWithMostPatientsByMonth(4, 2025);
```

> **Note:** For the assignment, you need to submit the complete output from each stored procedure execution. Save the terminal output.

## Doctor with Most Patients by Year

**Procedure:** `GetDoctorWithMostPatientsByYear`

This procedure identifies the doctor with the most patients in a given year. It is helpful for generating annual performance summaries.

### 1. Define the stored procedure

```sql
DELIMITER $$

CREATE PROCEDURE GetDoctorWithMostPatientsByYear(
    IN input_year INT
)
BEGIN
    SELECT
        doctor_id,
        COUNT(patient_id) AS patients_seen
    FROM
        appointment
    WHERE
        YEAR(appointment_time) = input_year
    GROUP BY
        doctor_id
    ORDER BY
        patients_seen DESC
    LIMIT 1;
END $$

DELIMITER ;
```

### 2. Run the stored procedure

```sql
CALL GetDoctorWithMostPatientsByYear(2025);
```

> **Note:** For the assignment, you need to submit the complete output from each stored procedure execution. Save the terminal output.

## Conclusion

In this lab, you've written stored procedures that:

- Generate daily appointment reports
- Identify the doctor with the most patients by month and year

This concludes the reporting module. In future labs, you will use these procedures within a full-stack application.

## Author(s)

Skills Network Team  
Upkar Lidder

## Check list
- [x] Did you define and run the stored procedure for the doctor's daily appointment report? — **Yes.** `GetDailyAppointmentReportByDoctor` in `scripts/stored_procedures.sql`; run via `scripts/run_stored_procedures.sql`.
- [x] Did you create a stored procedure to view the doctor with the most patients for a month? — **Yes.** `GetDoctorWithMostPatientsByMonth` in `scripts/stored_procedures.sql`.
- [x] Did you create a stored procedure to view the doctor with the most patients for a year? — **Yes.** `GetDoctorWithMostPatientsByYear` in `scripts/stored_procedures.sql`.
- [ ] Did you commit and push the codes to GitHub successfully? — Run: `git add .` then `git commit -m "Lab 4: Add stored procedures"` then `git push`.