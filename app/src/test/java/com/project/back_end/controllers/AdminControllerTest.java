package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.models.Admin;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private DoctorRepository doctorRepository;
    @MockBean
    private DoctorService doctorService;
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
    @MockBean
    private Service service;

    @Test
    void adminLogin_returns401_whenUsernameMissing() throws Exception {
        Admin body = new Admin();
        body.setPassword("pass123");
        when(service.validateAdmin(any(Admin.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Username and password are required.")));

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username and password are required."));
    }

    @Test
    void adminLogin_returns401_whenPasswordMissing() throws Exception {
        Admin body = new Admin();
        body.setUsername("admin1");
        when(service.validateAdmin(any(Admin.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Username and password are required.")));

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username and password are required."));
    }

    @Test
    void adminLogin_returns401_whenCredentialsInvalid() throws Exception {
        Admin body = new Admin();
        body.setUsername("admin1");
        body.setPassword("wrong");
        when(service.validateAdmin(any(Admin.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials. Please try again.")));

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials. Please try again."));
    }

    @Test
    void adminLogin_returns401_whenPasswordWrong() throws Exception {
        Admin body = new Admin();
        body.setUsername("admin1");
        body.setPassword("wrong");
        when(service.validateAdmin(any(Admin.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials. Please try again.")));

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials. Please try again."));
    }

    @Test
    void adminLogin_returns200_andToken_whenCredentialsValid() throws Exception {
        Admin body = new Admin();
        body.setUsername("admin1");
        body.setPassword("secret");
        when(service.validateAdmin(any(Admin.class)))
                .thenReturn(ResponseEntity.ok(Map.of("token", "jwt-token-here")));

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"));
    }
}
