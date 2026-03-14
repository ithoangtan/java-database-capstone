package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("${api.path}appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;
    private final TokenService tokenService;

    public AppointmentController(AppointmentService appointmentService, Service service, TokenService tokenService) {
        this.appointmentService = appointmentService;
        this.service = service;
        this.tokenService = tokenService;
    }

    /** Trả về ngày mới nhất có lịch của doctor (để dashboard tự chọn ngày khi load). Đặt trước mapping tổng quát để Spring match đúng. */
    @GetMapping("/latestDate/{token}")
    public ResponseEntity<Map<String, Object>> getLatestAppointmentDate(@PathVariable String token) {
        if (!service.validateToken(token, "doctor").isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token."));
        }
        String doctorEmail = tokenService.extractSubject(token);
        if (doctorEmail == null || doctorEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token."));
        }
        try {
            Optional<LocalDate> latest = appointmentService.getLatestAppointmentDateForDoctor(doctorEmail);
            if (latest.isEmpty()) {
                return ResponseEntity.ok(Map.<String, Object>of("date", ""));
            }
            return ResponseEntity.ok(Map.of("date", latest.get().toString()));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AppointmentController.class).warn("getLatestAppointmentDate failed", e);
            return ResponseEntity.ok(Map.<String, Object>of("date", ""));
        }
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {
        if (!service.validateToken(token, "doctor").isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token.", "appointments", List.of()));
        }
        String doctorEmail = tokenService.extractSubject(token);
        if (doctorEmail == null || doctorEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token.", "appointments", List.of()));
        }
        LocalDate parsedDate = null;
        if (date != null && !date.isBlank() && !"null".equalsIgnoreCase(date.trim())) {
            try {
                parsedDate = LocalDate.parse(date.trim());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid date format.", "appointments", List.of()));
            }
        }
        String nameFilter = "null".equalsIgnoreCase(patientName) ? null : patientName;
        List<Appointment> appointments = appointmentService.getAppointmentsForDoctor(doctorEmail, parsedDate, nameFilter);
        return ResponseEntity.ok(Map.of("appointments", appointments != null ? appointments : List.of()));
    }

    /** Book appointment (patient). Patient id must not be duplicated: same (doctor, patient, time) is rejected. */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @PathVariable String token,
            @RequestBody(required = false) Map<String, Object> body) {
        if (!service.validateToken(token, "patient").isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token."));
        }
        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Request body is required (doctor, patient, appointmentTime)."));
        }
        Long doctorId = null;
        Long patientId = null;
        LocalDateTime appointmentTime = null;
        int status = 0;
        if (body.get("doctor") instanceof Map) {
            Object id = ((Map<?, ?>) body.get("doctor")).get("id");
            if (id != null && !id.toString().isBlank()) {
                try {
                    doctorId = id instanceof Number ? ((Number) id).longValue() : Long.parseLong(id.toString().trim());
                } catch (NumberFormatException ignored) { }
            }
        }
        if (body.get("patient") instanceof Map) {
            Object id = ((Map<?, ?>) body.get("patient")).get("id");
            if (id != null && !id.toString().isBlank()) {
                try {
                    patientId = id instanceof Number ? ((Number) id).longValue() : Long.parseLong(id.toString().trim());
                } catch (NumberFormatException ignored) { }
            }
        }
        if (body.get("appointmentTime") != null && !body.get("appointmentTime").toString().isBlank()) {
            String timeStr = body.get("appointmentTime").toString().trim();
            try {
                appointmentTime = LocalDateTime.parse(timeStr);
            } catch (DateTimeParseException e1) {
                try {
                    appointmentTime = LocalDateTime.parse(timeStr + ":00");
                } catch (DateTimeParseException e2) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Invalid appointment time format. Use e.g. 2025-03-14T09:00:00"));
                }
            }
        }
        if (body.get("status") != null && body.get("status") instanceof Number) {
            status = ((Number) body.get("status")).intValue();
        }
        if (doctorId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "doctor id is required."));
        }
        if (patientId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "patient id is required."));
        }
        if (appointmentTime == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "appointmentTime is required (e.g. 2025-03-14T09:00:00)."));
        }
        if (appointmentService.hasExistingAppointment(doctorId, patientId, appointmentTime)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "An appointment for this patient with this doctor at this time already exists."));
        }
        Appointment saved = appointmentService.bookAppointment(doctorId, patientId, appointmentTime, status);
        if (saved == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Doctor or patient not found."));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Appointment booked successfully.", "id", saved.getId()));
    }

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST API controller.
//    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
//    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


// 2. Autowire Dependencies:
//    - Inject `AppointmentService` for handling the business logic specific to appointments.
//    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.


// 3. Define the `getAppointments` Method:
//    - Handles HTTP GET requests to fetch appointments based on date and patient name.
//    - Takes the appointment date, patient name, and token as path variables.
//    - First validates the token for role `"doctor"` using the `Service`.
//    - If the token is valid, returns appointments for the given patient on the specified date.
//    - If the token is invalid or expired, responds with the appropriate message and status code.


// 4. Define the `bookAppointment` Method:
//    - Handles HTTP POST requests to create a new appointment.
//    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
//    - Validates the token for the `"patient"` role.
//    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
//    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.


// 5. Define the `updateAppointment` Method:
//    - Handles HTTP PUT requests to modify an existing appointment.
//    - Accepts a validated `Appointment` object and a token as input.
//    - Validates the token for `"patient"` role.
//    - Delegates the update logic to the `AppointmentService`.
//    - Returns an appropriate success or failure response based on the update result.


// 6. Define the `cancelAppointment` Method:
//    - Handles HTTP DELETE requests to cancel a specific appointment.
//    - Accepts the appointment ID and a token as path variables.
//    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
//    - Calls `AppointmentService` to handle the cancellation process and returns the result.


}
