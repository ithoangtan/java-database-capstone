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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tr = service.validateToken(token, user);
        if (tr.getStatusCode().isError()) {
            Map<String, Object> err = new HashMap<>(tr.getBody() != null ? tr.getBody() : Map.of());
            err.put("slots", List.of());
            return ResponseEntity.status(tr.getStatusCode()).body(err);
        }
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid date format", "slots", List.of()));
        }
        List<String> slots = doctorService.getDoctorAvailability(doctorId, parsedDate);
        return ResponseEntity.ok(Map.of("slots", slots != null ? slots : List.of()));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getDoctors();
        return ResponseEntity.ok(Map.of("doctors", doctors != null ? doctors : List.of()));
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctorsByPath(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        String n = "null".equalsIgnoreCase(name) ? null : name;
        String t = "null".equalsIgnoreCase(time) ? null : time;
        String s = "null".equalsIgnoreCase(speciality) ? null : speciality;
        Map<String, Object> result = service.filterDoctor(n, s, t);
        return ResponseEntity.ok(result);
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
        ResponseEntity<Map<String, String>> r = doctorService.validateDoctor(login);
        Map<String, Object> body = r.getBody() != null ? new HashMap<>(r.getBody()) : new HashMap<>();
        return ResponseEntity.status(r.getStatusCode()).body(body);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> addDoctor(@PathVariable String token, @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, String>> tr = service.validateToken(token, "admin");
        if (tr.getStatusCode().isError()) {
            return ResponseEntity.status(tr.getStatusCode()).body(new HashMap<>(tr.getBody() != null ? tr.getBody() : Map.of()));
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
            return ResponseEntity.ok(Map.of("message", "Doctor added to db"));
        }
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Doctor already exists"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to add doctor."));
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateDoctor(@PathVariable String token, @RequestBody Map<String, Object> body) {
        ResponseEntity<Map<String, String>> tr = service.validateToken(token, "admin");
        if (tr.getStatusCode().isError()) {
            return ResponseEntity.status(tr.getStatusCode()).body(new HashMap<>(tr.getBody() != null ? tr.getBody() : Map.of()));
        }
        Object idObj = body != null ? body.get("id") : null;
        if (idObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Doctor not found"));
        }
        Long id = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        Doctor existing = doctorRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
        }
        String name = body.get("name") != null ? body.get("name").toString().trim() : existing.getName();
        String email = body.get("email") != null ? body.get("email").toString().trim() : existing.getEmail();
        String password = body.get("password") != null ? body.get("password").toString() : existing.getPassword();
        String specialty = body.get("specialty") != null ? body.get("specialty").toString().trim() : existing.getSpecialty();
        String phone = body.get("phone") != null ? body.get("phone").toString().trim().replaceAll("[^0-9]", "") : existing.getPhone();
        if (phone.length() != 10) phone = existing.getPhone();
        List<String> availableTimes = existing.getAvailableTimes();
        if (body.get("availableTimes") instanceof List) {
            availableTimes = new ArrayList<>();
            for (Object o : (List<?>) body.get("availableTimes")) {
                if (o != null) availableTimes.add(o.toString());
            }
        }
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setName(name);
        doctor.setEmail(email);
        doctor.setPassword(password);
        doctor.setSpecialty(specialty);
        doctor.setPhone(phone);
        doctor.setAvailableTimes(availableTimes != null ? availableTimes : List.of());
        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor updated"));
        }
        if (result == -2) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Doctor already exists"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found"));
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> tr = service.validateToken(token, "admin");
        if (tr.getStatusCode().isError()) {
            return ResponseEntity.status(tr.getStatusCode()).body(new HashMap<>(tr.getBody() != null ? tr.getBody() : Map.of()));
        }
        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        }
        if (result == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found with id"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to delete doctor."));
    }
}
