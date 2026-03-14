package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private Service service;

    @MockBean
    private AdminRepository adminRepository;
    @MockBean
    private PatientRepository patientRepository;
    @MockBean
    private AppointmentRepository appointmentRepository;
    @MockBean
    private AppointmentService appointmentService;
    @MockBean
    private PrescriptionService prescriptionService;
    @MockBean
    private PrescriptionRepository prescriptionRepository;

    @Test
    void getAllDoctors_returns200_withDoctorsList() throws Exception {
        when(doctorService.getDoctors()).thenReturn(List.of(new Doctor()));

        mockMvc.perform(get("/doctor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors").isArray())
                .andExpect(jsonPath("$.doctors.length()").value(1));
    }

    @Test
    void getAllDoctors_returns200_withEmptyList() throws Exception {
        when(doctorService.getDoctors()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/doctor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors").isArray())
                .andExpect(jsonPath("$.doctors.length()").value(0));
    }

    @Test
    void filterDoctors_returns200_withFilteredList() throws Exception {
        when(doctorService.filterDoctors("John", null, "Cardiology")).thenReturn(List.of(new Doctor()));

        mockMvc.perform(get("/doctor/filter")
                        .param("name", "John")
                        .param("specialty", "Cardiology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors").isArray());
    }

    @Test
    void doctorLogin_returns401_whenEmailMissing() throws Exception {
        Login login = new Login();
        login.setEmail("");
        login.setPassword("pass123");

        mockMvc.perform(post("/doctor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email and password are required."));
    }

    @Test
    void doctorLogin_returns401_whenCredentialsInvalid() throws Exception {
        Login login = new Login();
        login.setEmail("doc@test.com");
        login.setPassword("wrong");
        when(doctorRepository.findByEmail("doc@test.com")).thenReturn(null);

        mockMvc.perform(post("/doctor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials. Please try again."));
    }

    @Test
    void doctorLogin_returns200_andToken_whenValid() throws Exception {
        Login login = new Login();
        login.setEmail("doc@test.com");
        login.setPassword("secret");
        Doctor doctor = new Doctor();
        doctor.setEmail("doc@test.com");
        doctor.setPassword("secret");
        when(doctorRepository.findByEmail("doc@test.com")).thenReturn(doctor);
        when(tokenService.generateToken("doc@test.com")).thenReturn("jwt-token");

        mockMvc.perform(post("/doctor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void addDoctor_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("admin"))).thenReturn(Map.of("error", "Invalid"));

        Map<String, Object> body = Map.of(
                "name", "Dr. John",
                "email", "john@test.com",
                "password", "pass1234",
                "specialty", "Cardiology"
        );

        mockMvc.perform(post("/doctor/bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token."));
    }

    @Test
    void addDoctor_returns400_whenNameMissing() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("admin"))).thenReturn(Map.of());

        Map<String, Object> body = Map.of(
                "email", "john@test.com",
                "password", "pass1234"
        );

        mockMvc.perform(post("/doctor/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name is required."));
    }

    @Test
    void addDoctor_returns409_whenEmailExists() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("admin"))).thenReturn(Map.of());
        Map<String, Object> body = Map.of(
                "name", "Dr. John",
                "email", "existing@test.com",
                "password", "pass1234",
                "specialty", "Cardiology"
        );
        Doctor toSave = new Doctor();
        toSave.setName("Dr. John");
        toSave.setEmail("existing@test.com");
        toSave.setPassword("pass1234");
        toSave.setSpecialty("Cardiology");
        toSave.setPhone("0000000000");
        toSave.setAvailableTimes(List.of("09:00-10:00"));
        when(doctorService.saveDoctor(any(Doctor.class))).thenReturn(-1);

        mockMvc.perform(post("/doctor/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A doctor with this email already exists."));
    }

    @Test
    void addDoctor_returns200_whenSuccess() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("admin"))).thenReturn(Map.of());
        Map<String, Object> body = Map.of(
                "name", "Dr. John",
                "email", "new@test.com",
                "password", "pass1234",
                "specialty", "Cardiology"
        );
        when(doctorService.saveDoctor(any(Doctor.class))).thenReturn(1);

        mockMvc.perform(post("/doctor/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor added successfully."));
    }

    @Test
    void deleteDoctor_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("admin"))).thenReturn(Map.of("error", "Invalid"));

        mockMvc.perform(delete("/doctor/1/bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteDoctor_returns404_whenDoctorNotFound() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("admin"))).thenReturn(Map.of());
        when(doctorService.deleteDoctor(999L)).thenReturn(-1);

        mockMvc.perform(delete("/doctor/999/valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Doctor not found."));
    }

    @Test
    void deleteDoctor_returns200_whenSuccess() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("admin"))).thenReturn(Map.of());
        when(doctorService.deleteDoctor(1L)).thenReturn(1);

        mockMvc.perform(delete("/doctor/1/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor deleted successfully."));
    }
}
