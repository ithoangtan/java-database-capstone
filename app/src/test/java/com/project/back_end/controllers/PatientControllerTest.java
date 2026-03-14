package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.PrescriptionRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.PatientService;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientRepository patientRepository;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private Service service;

    @MockBean
    private AppointmentRepository appointmentRepository;

    @MockBean
    private PatientService patientService;

    @MockBean
    private AdminRepository adminRepository;
    @MockBean
    private DoctorRepository doctorRepository;
    @MockBean
    private DoctorService doctorService;
    @MockBean
    private AppointmentService appointmentService;
    @MockBean
    private PrescriptionService prescriptionService;
    @MockBean
    private PrescriptionRepository prescriptionRepository;

    @Test
    void login_returns401_whenEmailMissing() throws Exception {
        Login login = new Login();
        login.setIdentifier("");
        login.setPassword("pass123");
        when(service.validatePatientLogin(any(Login.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Identifier and password are required.")));

        mockMvc.perform(post("/patient/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Identifier and password are required."));
    }

    @Test
    void login_returns401_whenCredentialsInvalid() throws Exception {
        Login login = new Login();
        login.setIdentifier("patient@test.com");
        login.setPassword("wrong");
        when(service.validatePatientLogin(any(Login.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials. Please try again.")));

        mockMvc.perform(post("/patient/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials. Please try again."));
    }

    @Test
    void login_returns200_andToken_whenValid() throws Exception {
        Login login = new Login();
        login.setIdentifier("patient@test.com");
        login.setPassword("secret");
        when(service.validatePatientLogin(any(Login.class)))
                .thenReturn(ResponseEntity.ok(Map.of("token", "jwt-token")));

        mockMvc.perform(post("/patient/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void getPatientAppointments_returns400_whenPatientIdInvalid() throws Exception {
        mockMvc.perform(get("/patient/not-a-number/patient/token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid patient id."));
    }

    @Test
    void getPatientAppointments_returns400_whenUserRoleInvalid() throws Exception {
        mockMvc.perform(get("/patient/1/invalid-role/token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid user role."));
    }

    @Test
    void getPatientAppointments_returns401_whenPatientTokenInvalid() throws Exception {
        when(patientService.getPatientAppointment(1L, "bad-token"))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired token", "appointments", List.of())));

        mockMvc.perform(get("/patient/1/patient/bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void getPatientAppointments_returns403_whenPatientTokenValidButWrongPatientId() throws Exception {
        when(patientService.getPatientAppointment(1L, "valid-token"))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied", "appointments", List.of())));

        mockMvc.perform(get("/patient/1/patient/valid-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void getPatientAppointments_returns200_withAppointments_whenPatientTokenValid() throws Exception {
        when(patientService.getPatientAppointment(1L, "valid-token"))
                .thenReturn(ResponseEntity.ok(Map.of("appointments", Collections.emptyList())));

        mockMvc.perform(get("/patient/1/patient/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray());
    }

    @Test
    void getPatientAppointments_returns200_whenDoctorTokenValid() throws Exception {
        when(service.validateToken(eq("doc-token"), eq("doctor"))).thenReturn(ResponseEntity.ok(Map.of()));
        when(appointmentRepository.findByPatient_IdOrderByAppointmentTimeDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/patient/1/doctor/doc-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray());
    }

    @Test
    void getPatient_returns401_whenTokenInvalid() throws Exception {
        when(patientService.getPatientDetails("bad-token"))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired token")));

        mockMvc.perform(get("/patient/bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPatient_returns404_whenPatientNotFound() throws Exception {
        when(patientService.getPatientDetails("valid-token"))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found")));

        mockMvc.perform(get("/patient/valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found"));
    }

    @Test
    void getPatient_returns200_withPatient_whenValid() throws Exception {
        when(patientService.getPatientDetails("valid-token"))
                .thenReturn(ResponseEntity.ok(Map.of("patient", Map.of(
                        "id", 1L, "name", "John", "email", "patient@test.com", "phone", "1234567890", "address", "123 Main St"
                ))));

        mockMvc.perform(get("/patient/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.id").value(1))
                .andExpect(jsonPath("$.patient.name").value("John"))
                .andExpect(jsonPath("$.patient.email").value("patient@test.com"));
    }

    @Test
    void createPatient_returns400_whenNameMissing() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "new@test.com",
                "password", "pass1234",
                "phone", "1234567890",
                "address", "123 Main St"
        );

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required."));
    }

    @Test
    void createPatient_returns400_whenNameTooShort() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Ab",
                "email", "new@test.com",
                "password", "pass1234",
                "phone", "1234567890",
                "address", "123 Main St"
        );

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name must be between 3 and 100 characters."));
    }

    @Test
    void createPatient_returns400_whenEmailInvalid() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "John Doe",
                "email", "not-an-email",
                "password", "pass1234",
                "phone", "1234567890",
                "address", "123 Main St"
        );

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter a valid email address."));
    }

    @Test
    void createPatient_returns400_whenPasswordTooShort() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "John Doe",
                "email", "john@test.com",
                "password", "12345",
                "phone", "1234567890",
                "address", "123 Main St"
        );

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 6 characters."));
    }

    @Test
    void createPatient_returns400_whenPhoneNot10Digits() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "John Doe",
                "email", "john@test.com",
                "password", "pass1234",
                "phone", "123",
                "address", "123 Main St"
        );

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phone must be exactly 10 digits."));
    }

    @Test
    void createPatient_returns409_whenEmailOrPhoneExists() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "John Doe",
                "email", "existing@test.com",
                "password", "pass1234",
                "phone", "1234567890",
                "address", "123 Main St"
        );
        when(patientRepository.findByEmailOrPhone("existing@test.com", "1234567890")).thenReturn(new Patient());

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Patient with email id or phone no already exist"));
    }

    @Test
    void createPatient_returns200_whenValid() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "John Doe",
                "email", "new@test.com",
                "password", "pass1234",
                "phone", "1234567890",
                "address", "123 Main St"
        );
        when(patientRepository.findByEmailOrPhone("new@test.com", "1234567890")).thenReturn(null);

        mockMvc.perform(post("/patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Signup successful"));
    }
}
