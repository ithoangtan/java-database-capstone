-- Lab 4: Run stored procedures (save terminal output for assignment)
-- Run after: USE cms; and after scripts/stored_procedures.sql has been executed

-- 1. Daily appointment report by doctor (e.g. 2025-04-15)
CALL GetDailyAppointmentReportByDoctor('2025-04-15');

-- 2. Doctor with most patients by month (e.g. April 2025)
CALL GetDoctorWithMostPatientsByMonth(4, 2025);

-- 3. Doctor with most patients by year (e.g. 2025)
CALL GetDoctorWithMostPatientsByYear(2025);
