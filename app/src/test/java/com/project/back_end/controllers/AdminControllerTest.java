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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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
        Map<String, String> body = Map.of("password", "pass123");

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username and password are required."));
    }

    @Test
    void adminLogin_returns401_whenPasswordMissing() throws Exception {
        Map<String, String> body = Map.of("username", "admin1");

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username and password are required."));
    }

    @Test
    void adminLogin_returns401_whenCredentialsInvalid() throws Exception {
        Map<String, String> body = Map.of("username", "admin1", "password", "wrong");
        when(adminRepository.findByUsername("admin1")).thenReturn(null);

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials. Please try again."));
    }

    @Test
    void adminLogin_returns401_whenPasswordWrong() throws Exception {
        Map<String, String> body = Map.of("username", "admin1", "password", "wrong");
        Admin admin = new Admin();
        admin.setUsername("admin1");
        admin.setPassword("correct");
        when(adminRepository.findByUsername("admin1")).thenReturn(admin);

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials. Please try again."));
    }

    @Test
    void adminLogin_returns200_andToken_whenCredentialsValid() throws Exception {
        Map<String, String> body = Map.of("username", "admin1", "password", "secret");
        Admin admin = new Admin();
        admin.setUsername("admin1");
        admin.setPassword("secret");
        when(adminRepository.findByUsername("admin1")).thenReturn(admin);
        when(tokenService.generateToken("admin1")).thenReturn("jwt-token-here");

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"));
    }
}
