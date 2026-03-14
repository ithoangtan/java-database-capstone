package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.PrescriptionRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private Service service;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private AdminRepository adminRepository;
    @MockBean
    private DoctorRepository doctorRepository;
    @MockBean
    private DoctorService doctorService;
    @MockBean
    private PatientRepository patientRepository;
    @MockBean
    private AppointmentRepository appointmentRepository;
    @MockBean
    private PrescriptionService prescriptionService;
    @MockBean
    private PrescriptionRepository prescriptionRepository;

    @Test
    void getLatestAppointmentDate_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("doctor"))).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired token")));

        mockMvc.perform(get("/appointments/latestDate/bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void getLatestAppointmentDate_returns200_withEmptyDate_whenNoAppointments() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(ResponseEntity.ok(Map.of()));
        when(tokenService.extractSubject("valid-token")).thenReturn("doctor@test.com");
        when(appointmentService.getLatestAppointmentDateForDoctor("doctor@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/appointments/latestDate/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(""));
    }

    @Test
    void getLatestAppointmentDate_returns200_withDate_whenHasAppointments() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(ResponseEntity.ok(Map.of()));
        when(tokenService.extractSubject("valid-token")).thenReturn("doctor@test.com");
        when(appointmentService.getLatestAppointmentDateForDoctor("doctor@test.com"))
                .thenReturn(Optional.of(LocalDate.of(2025, 3, 14)));

        mockMvc.perform(get("/appointments/latestDate/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-03-14"));
    }

    @Test
    void getAppointments_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("doctor"))).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired token")));

        mockMvc.perform(get("/appointments/2025-03-14/John/bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAppointments_returns400_whenDateInvalid() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(ResponseEntity.ok(Map.of()));
        when(tokenService.extractSubject("valid-token")).thenReturn("doctor@test.com");

        mockMvc.perform(get("/appointments/not-a-date/null/valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid date format."));
    }

    @Test
    void getAppointments_returns200_withAppointments() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(ResponseEntity.ok(Map.of()));
        when(tokenService.extractSubject("valid-token")).thenReturn("doctor@test.com");
        when(appointmentService.getAppointmentsForDoctor(eq("doctor@test.com"), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/appointments/2025-03-14/null/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray());
    }

    @Test
    void bookAppointment_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("patient"))).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired token")));
        Map<String, Object> body = Map.of(
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-14T09:00:00"
        );

        mockMvc.perform(post("/appointments/bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bookAppointment_returns400_whenBodyEmpty() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));

        mockMvc.perform(post("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is required (doctor, patient, appointmentTime)."));
    }

    @Test
    void bookAppointment_returns400_whenDoctorIdMissing() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));
        Map<String, Object> body = Map.of(
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-14T09:00:00"
        );

        mockMvc.perform(post("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("doctor id is required."));
    }

    @Test
    void bookAppointment_returns409_whenDuplicateSlot() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));
        Map<String, Object> body = Map.of(
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-14T09:00:00"
        );
        when(appointmentService.hasExistingAppointment(1L, 2L, LocalDateTime.of(2025, 3, 14, 9, 0))).thenReturn(true);

        mockMvc.perform(post("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An appointment for this patient with this doctor at this time already exists."));
    }

    @Test
    void bookAppointment_returns400_whenDoctorOrPatientNotFound() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));
        Map<String, Object> body = Map.of(
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-14T09:00:00"
        );
        when(appointmentService.hasExistingAppointment(1L, 2L, LocalDateTime.of(2025, 3, 14, 9, 0))).thenReturn(false);
        when(appointmentService.bookAppointment(1L, 2L, LocalDateTime.of(2025, 3, 14, 9, 0), 0)).thenReturn(null);

        mockMvc.perform(post("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Doctor or patient not found."));
    }

    @Test
    void bookAppointment_returns201_whenSuccess() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));
        Map<String, Object> body = Map.of(
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-14T09:00:00"
        );
        Appointment saved = new Appointment();
        saved.setId(10L);
        when(appointmentService.hasExistingAppointment(1L, 2L, LocalDateTime.of(2025, 3, 14, 9, 0))).thenReturn(false);
        when(appointmentService.bookAppointment(1L, 2L, LocalDateTime.of(2025, 3, 14, 9, 0), 0)).thenReturn(saved);

        mockMvc.perform(post("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Appointment booked successfully."))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void updateAppointment_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("patient"))).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired token")));
        Map<String, Object> body = Map.of(
                "id", 1,
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-15T10:00:00"
        );

        mockMvc.perform(put("/appointments/bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAppointment_returns400_whenAppointmentIdMissing() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));
        Map<String, Object> body = Map.of(
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-15T10:00:00"
        );

        mockMvc.perform(put("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Appointment id is required."));
    }

    @Test
    void updateAppointment_returns400_whenNotFoundOrDenied() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));
        Map<String, Object> body = Map.of(
                "id", 1,
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-15T10:00:00"
        );
        when(appointmentService.updateAppointment(1L, 2L, 1L, LocalDateTime.of(2025, 3, 15, 10, 0), 0)).thenReturn(null);

        mockMvc.perform(put("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Appointment not found, access denied, or slot already taken."));
    }

    @Test
    void updateAppointment_returns200_whenSuccess() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(ResponseEntity.ok(Map.of()));
        Map<String, Object> body = Map.of(
                "id", 1,
                "doctor", Map.of("id", 1),
                "patient", Map.of("id", 2),
                "appointmentTime", "2025-03-15T10:00:00"
        );
        Appointment updated = new Appointment();
        updated.setId(1L);
        when(appointmentService.updateAppointment(1L, 2L, 1L, LocalDateTime.of(2025, 3, 15, 10, 0), 0)).thenReturn(updated);

        mockMvc.perform(put("/appointments/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment updated successfully."))
                .andExpect(jsonPath("$.id").value(1));
    }
}
