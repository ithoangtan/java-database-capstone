package com.project.back_end.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.models.Prescription;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrescriptionController.class)
@Import(ValidationFailed.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private Service service;

    @MockBean
    private AppointmentService appointmentService;

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
    private PrescriptionRepository prescriptionRepository;
    @MockBean
    private TokenService tokenService;

    @Test
    void savePrescription_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("doctor"))).thenReturn(Map.of("error", "Invalid"));
        Prescription prescription = validPrescription();
        prescription.setAppointmentId(1L);

        mockMvc.perform(post("/prescription/bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void savePrescription_returns400_whenAppointmentIdNull() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        Prescription prescription = validPrescription();
        prescription.setAppointmentId(null);

        mockMvc.perform(post("/prescription/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.appointmentId").exists());
    }

    @Test
    void savePrescription_returns400_whenAlreadyExists() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        Prescription prescription = validPrescription();
        prescription.setAppointmentId(1L);
        when(prescriptionService.savePrescription(any(Prescription.class))).thenReturn(null);

        mockMvc.perform(post("/prescription/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A prescription already exists for this appointment."));
        verify(appointmentService, never()).updateAppointmentStatus(anyLong(), anyInt());
    }

    @Test
    void savePrescription_returns201_whenSuccess() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        Prescription prescription = validPrescription();
        prescription.setAppointmentId(1L);
        Prescription saved = validPrescription();
        saved.setId("pres-1");
        saved.setAppointmentId(1L);
        when(prescriptionService.savePrescription(any(Prescription.class))).thenReturn(saved);

        mockMvc.perform(post("/prescription/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Prescription saved successfully."))
                .andExpect(jsonPath("$.id").value("pres-1"));
        verify(appointmentService).updateAppointmentStatus(1L, 1);
    }

    @Test
    void updatePrescription_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("doctor"))).thenReturn(Map.of("error", "Invalid"));
        Prescription prescription = validPrescription();
        prescription.setId("pres-1");
        prescription.setAppointmentId(1L);

        mockMvc.perform(put("/prescription/bad-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePrescription_returns400_whenIdMissing() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        Prescription prescription = validPrescription();
        prescription.setId(null);
        prescription.setAppointmentId(1L);

        mockMvc.perform(put("/prescription/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Prescription id is required for update."));
    }

    @Test
    void updatePrescription_returns404_whenNotFound() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        Prescription prescription = validPrescription();
        prescription.setId("pres-1");
        prescription.setAppointmentId(1L);
        when(prescriptionService.updatePrescription(any(Prescription.class))).thenReturn(null);

        mockMvc.perform(put("/prescription/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Prescription not found."));
    }

    @Test
    void updatePrescription_returns200_whenSuccess() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        Prescription prescription = validPrescription();
        prescription.setId("pres-1");
        prescription.setAppointmentId(1L);
        when(prescriptionService.updatePrescription(any(Prescription.class))).thenReturn(prescription);

        mockMvc.perform(put("/prescription/valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Prescription updated successfully."))
                .andExpect(jsonPath("$.id").value("pres-1"));
    }

    @Test
    void getPrescription_returns401_whenTokenInvalid() throws Exception {
        when(service.validateToken(eq("bad-token"), eq("doctor"))).thenReturn(Map.of("error", "Invalid"));

        mockMvc.perform(get("/prescription/1/bad-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPrescription_returns200_withList() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        when(prescriptionService.getPrescriptionByAppointmentId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/prescription/1/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prescription").isArray());
    }

    @Test
    void getPrescription_returns500_whenServiceThrows() throws Exception {
        when(service.validateToken(eq("valid-token"), eq("doctor"))).thenReturn(Map.of());
        when(prescriptionService.getPrescriptionByAppointmentId(1L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/prescription/1/valid-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to fetch prescription."));
    }

    private static Prescription validPrescription() {
        Prescription p = new Prescription();
        p.setPatientName("John Doe");
        p.setMedication("Aspirin");
        p.setDosage("100mg");
        p.setDoctorNotes("Take daily");
        return p;
    }
}
