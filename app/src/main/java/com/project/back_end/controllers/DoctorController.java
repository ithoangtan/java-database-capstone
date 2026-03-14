package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorRepository doctorRepository, TokenService tokenService,
                            DoctorService doctorService, Service service) {
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getDoctors();
        return ResponseEntity.ok(Map.of("doctors", doctors != null ? doctors : List.of()));
    }

    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String specialty) {
        List<Doctor> doctors = doctorService.filterDoctors(name, time, specialty);
        return ResponseEntity.ok(Map.of("doctors", doctors != null ? doctors : List.of()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> doctorLogin(@RequestBody Login login) {
        if (login == null || login.getEmail() == null || login.getEmail().isBlank() || login.getPassword() == null || login.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email and password are required."));
        }
        Doctor doctor = doctorRepository.findByEmail(login.getEmail().trim());
        if (doctor == null || !login.getPassword().equals(doctor.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials. Please try again."));
        }
        String token = tokenService.generateToken(doctor.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> addDoctor(@PathVariable String token, @RequestBody Map<String, Object> body) {
        if (!service.validateToken(token, "admin").isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token."));
        }
        String name = body != null && body.get("name") != null ? body.get("name").toString().trim() : null;
        String email = body != null && body.get("email") != null ? body.get("email").toString().trim() : null;
        String password = body != null && body.get("password") != null ? body.get("password").toString() : null;
        String specialtyRaw = body != null && body.get("specialty") != null ? body.get("specialty").toString().trim() : "";
        String specialty = (specialtyRaw == null || specialtyRaw.isEmpty()) ? "General" : specialtyRaw;
        String phone = "0000000000";
        if (body != null && body.get("phone") != null && !body.get("phone").toString().isBlank()) {
            String raw = body.get("phone").toString().trim().replaceAll("[^0-9]", "");
            if (raw.length() == 10) {
                phone = raw;
            } else if (!raw.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Phone must be exactly 10 digits."));
            }
        }
        List<String> availableTimes = new ArrayList<>();
        if (body != null && body.get("availableTimes") instanceof List) {
            for (Object o : (List<?>) body.get("availableTimes")) {
                if (o != null) availableTimes.add(o.toString());
            }
        }
        if (availableTimes.isEmpty()) {
            availableTimes.add("09:00-10:00");
        }
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
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password is required."));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters."));
        }
        if (specialty.length() < 3 || specialty.length() > 50) {
            return ResponseEntity.badRequest().body(Map.of("message", "Specialty must be between 3 and 50 characters."));
        }
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setPassword(password);
        doctor.setSpecialty(specialty);
        doctor.setPhone(phone);
        doctor.setAvailableTimes(availableTimes);
        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor added successfully."));
        }
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "A doctor with this email already exists."));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to add doctor."));
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        if (!service.validateToken(token, "admin").isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token."));
        }
        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully."));
        }
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found."));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to delete doctor."));
    }
}
