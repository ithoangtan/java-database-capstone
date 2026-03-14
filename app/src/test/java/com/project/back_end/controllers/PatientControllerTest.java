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
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        login.setEmail("");
        login.setPassword("pass123");

        mockMvc.perform(post("/patient/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email and password are required."));
    }

    @Test
    void login_returns401_whenCredentialsInvalid() throws Exception {
        Login login = new Login();
        login.setEmail("patient@test.com");
        login.setPassword("wrong");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(null);

        mockMvc.perform(post("/patient/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials. Please try again."));
    }

    @Test
    void login_returns200_andToken_whenValid() throws Exception {
        Login login = new Login();
        login.setEmail("patient@test.com");
        login.setPassword("secret");
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setEmail("patient@test.com");
        patient.setPassword("secret");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(patient);
        when(tokenService.generateToken("patient@test.com")).thenReturn("jwt-token");

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
        when(service.validateToken(eq("bad-token"), eq("patient"))).thenReturn(Map.of("error", "Invalid"));

        mockMvc.perform(get("/patient/1/patient/bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token."));
    }

    @Test
    void getPatientAppointments_returns403_whenPatientTokenValidButWrongPatientId() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(Map.of());
        when(tokenService.extractSubject("valid-token")).thenReturn("patient@test.com");
        Patient patient = new Patient();
        patient.setId(99L);
        patient.setEmail("patient@test.com");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(patient);

        mockMvc.perform(get("/patient/1/patient/valid-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));
    }

    @Test
    void getPatientAppointments_returns200_withAppointments_whenPatientTokenValid() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(Map.of());
        when(tokenService.extractSubject("valid-token")).thenReturn("patient@test.com");
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setEmail("patient@test.com");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(patient);
        when(appointmentRepository.findByPatient_IdOrderByAppointmentTimeDesc(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/patient/1/patient/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray());
    }

    @Test
    void getPatientAppointments_returns200_whenDoctorTokenValid() throws Exception {
        when(service.validateToken(eq("doc-token"), eq("doctor"))).thenReturn(Map.of());
        when(appointmentRepository.findByPatient_IdOrderByAppointmentTimeDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/patient/1/doctor/doc-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray());
    }

    @Test
    void getPatient_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("patient"))).thenReturn(Map.of("error", "Invalid"));

        mockMvc.perform(get("/patient/bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPatient_returns404_whenPatientNotFound() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(Map.of());
        when(tokenService.extractSubject("valid-token")).thenReturn("unknown@test.com");
        when(patientRepository.findByEmail("unknown@test.com")).thenReturn(null);

        mockMvc.perform(get("/patient/valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found."));
    }

    @Test
    void getPatient_returns200_withPatient_whenValid() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("patient"))).thenReturn(Map.of());
        when(tokenService.extractSubject("valid-token")).thenReturn("patient@test.com");
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setName("John");
        patient.setEmail("patient@test.com");
        patient.setPhone("1234567890");
        patient.setAddress("123 Main St");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(patient);

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
                .andExpect(jsonPath("$.message").value("A patient with this email or phone already exists."));
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
                .andExpect(jsonPath("$.message").value("Patient registered successfully."));
    }
}
