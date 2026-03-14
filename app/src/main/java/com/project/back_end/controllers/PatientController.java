package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.path}patient")
public class PatientController {

    private final PatientRepository patientRepository;
    private final TokenService tokenService;
    private final Service service;
    private final AppointmentRepository appointmentRepository;

    public PatientController(PatientRepository patientRepository, TokenService tokenService, Service service,
                             AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.tokenService = tokenService;
        this.service = service;
        this.appointmentRepository = appointmentRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
        if (login == null || login.getEmail() == null || login.getEmail().isBlank()
                || login.getPassword() == null || login.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email and password are required."));
        }
        Patient patient = patientRepository.findByEmail(login.getEmail().trim());
        if (patient == null || !login.getPassword().equals(patient.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials. Please try again."));
        }
        String token = tokenService.generateToken(patient.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    /** Get appointments for a patient: GET /patient/{id}/{user}/{token}. User is "patient" or "doctor". Token may contain dots (JWT). */
    @GetMapping("/{id}/{user}/{token:.+}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable String id,
            @PathVariable String user,
            @PathVariable String token) {
        Long patientId;
        try {
            patientId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid patient id.", "appointments", List.of()));
        }
        if ("patient".equalsIgnoreCase(user)) {
            if (!service.validateToken(token, "patient").isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid or expired token.", "appointments", List.of()));
            }
            String email = tokenService.extractSubject(token);
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid token.", "appointments", List.of()));
            }
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null || !patient.getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access denied.", "appointments", List.of()));
            }
        } else if ("doctor".equalsIgnoreCase(user)) {
            if (!service.validateToken(token, "doctor").isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid or expired token.", "appointments", List.of()));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid user role.", "appointments", List.of()));
        }
        List<Appointment> appointments = appointmentRepository.findByPatient_IdOrderByAppointmentTimeDesc(patientId);
        List<Map<String, Object>> list = appointments.stream()
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "doctor", a.getDoctor() != null ? Map.of(
                                "id", a.getDoctor().getId(),
                                "name", a.getDoctor().getName() != null ? a.getDoctor().getName() : "",
                                "specialty", a.getDoctor().getSpecialty() != null ? a.getDoctor().getSpecialty() : ""
                        ) : Map.of(),
                        "patient", a.getPatient() != null ? Map.of(
                                "id", a.getPatient().getId(),
                                "name", a.getPatient().getName() != null ? a.getPatient().getName() : ""
                        ) : Map.of(),
                        "appointmentTime", a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "",
                        "status", a.getStatus()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("appointments", list));
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        if (!service.validateToken(token, "patient").isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token."));
        }
        String email = tokenService.extractSubject(token);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token."));
        }
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Patient not found."));
        }
        return ResponseEntity.ok(Map.of(
                "patient",
                Map.of(
                        "id", patient.getId(),
                        "name", patient.getName(),
                        "email", patient.getEmail(),
                        "phone", patient.getPhone(),
                        "address", patient.getAddress()
                )
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody Map<String, Object> body) {
        String name = body != null && body.get("name") != null ? body.get("name").toString().trim() : null;
        String email = body != null && body.get("email") != null ? body.get("email").toString().trim() : null;
        String password = body != null && body.get("password") != null ? body.get("password").toString() : null;
        String phone = body != null && body.get("phone") != null ? body.get("phone").toString().trim().replaceAll("[^0-9]", "") : null;
        String address = body != null && body.get("address") != null ? body.get("address").toString().trim() : null;

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required."));
        }
        if (name.length() < 3 || name.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name must be between 3 and 100 characters."));
        }
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please enter a valid email address."));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters."));
        }
        if (phone == null || phone.length() != 10) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone must be exactly 10 digits."));
        }
        if (address == null || address.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Address is required."));
        }
        if (address.length() > 255) {
            return ResponseEntity.badRequest().body(Map.of("message", "Address must not exceed 255 characters."));
        }

        if (patientRepository.findByEmailOrPhone(email, phone) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "A patient with this email or phone already exists."));
        }

        Patient patient = new Patient();
        patient.setName(name);
        patient.setEmail(email);
        patient.setPassword(password);
        patient.setPhone(phone);
        patient.setAddress(address);
        try {
            patientRepository.save(patient);
            return ResponseEntity.ok(Map.of("message", "Patient registered successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to register. Please try again."));
        }
    }
}
