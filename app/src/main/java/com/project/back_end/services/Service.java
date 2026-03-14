package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService, AdminRepository adminRepository,
                   DoctorRepository doctorRepository, PatientRepository patientRepository,
                   DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Validates the token for the given user role. Returns 200 with empty map if valid,
     * or 401 with error message if invalid (per lab: ResponseEntity&lt;Map&lt;String, String&gt;&gt;).
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        if (tokenService.validateToken(token, user)) {
            return ResponseEntity.ok(Collections.emptyMap());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid or expired token"));
    }

    /**
     * Legacy: returns empty map if valid, or map with error for use by controllers that check isEmpty().
     */
    public Map<String, Object> validateTokenAsMap(String token, String user) {
        if (tokenService.validateToken(token, user)) {
            return Collections.emptyMap();
        }
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Invalid or expired token");
        return errors;
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        if (receivedAdmin == null || receivedAdmin.getUsername() == null || receivedAdmin.getUsername().isBlank()
                || receivedAdmin.getPassword() == null || receivedAdmin.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Username and password are required."));
        }
        Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername().trim());
        if (admin == null || !receivedAdmin.getPassword().equals(admin.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials. Please try again."));
        }
        String token = tokenService.generateToken(admin.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        List<?> doctors = doctorService.filterDoctors(name, time, specialty);
        return Map.of("doctors", doctors != null ? doctors : List.of());
    }

    /**
     * Validates if the requested appointment time is available for the doctor.
     * @return 1 if valid, 0 if invalid slot, -1 if doctor not found
     */
    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getAppointmentTime() == null) {
            return 0;
        }
        Long doctorId = appointment.getDoctor().getId();
        if (doctorId == null) return -1;
        var availability = doctorService.getDoctorAvailability(doctorId, appointment.getAppointmentTime().toLocalDate());
        if (availability == null || availability.isEmpty()) return 0;
        String timeOnly = appointment.getAppointmentTime().toLocalTime().toString();
        if (timeOnly.length() >= 5) timeOnly = timeOnly.substring(0, 5);
        for (String slot : availability) {
            String start = slot.split("-")[0].trim();
            if (start.length() >= 5) start = start.substring(0, 5);
            if (start.equals(timeOnly)) return 1;
        }
        return 0;
    }

    /**
     * Returns true if patient is valid for new registration (no duplicate email/phone).
     */
    public boolean validatePatient(Patient patient) {
        if (patient == null || patient.getEmail() == null || patient.getPhone() == null) {
            return false;
        }
        return patientRepository.findByEmailOrPhone(patient.getEmail().trim(), patient.getPhone().trim()) == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        if (login == null || login.getIdentifier() == null || login.getIdentifier().isBlank()
                || login.getPassword() == null || login.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Identifier and password are required."));
        }
        Patient patient = patientRepository.findByEmail(login.getIdentifier().trim());
        if (patient == null || !login.getPassword().equals(patient.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials. Please try again."));
        }
        String token = tokenService.generateToken(patient.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        return patientService.filterPatient(condition, name, token);
    }
}
