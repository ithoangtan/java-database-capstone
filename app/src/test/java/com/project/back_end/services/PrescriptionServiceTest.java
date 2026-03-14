package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    private PrescriptionService prescriptionService;

    @BeforeEach
    void setUp() {
        prescriptionService = new PrescriptionService(prescriptionRepository);
    }

    @Test
    void savePrescription_throws_whenAppointmentIdIsNull() {
        Prescription prescription = new Prescription();
        prescription.setAppointmentId(null);
        prescription.setPatientName("Patient");
        prescription.setMedication("Med");
        prescription.setDosage("1mg");

        assertThatThrownBy(() -> prescriptionService.savePrescription(prescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("appointmentId is required");
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void savePrescription_returnsNull_whenPrescriptionAlreadyExistsForAppointment() {
        Prescription prescription = new Prescription();
        prescription.setAppointmentId(1L);
        prescription.setPatientName("Patient");
        prescription.setMedication("Med");
        prescription.setDosage("1mg");
        when(prescriptionRepository.findByAppointmentId(1L)).thenReturn(List.of(new Prescription()));

        Prescription result = prescriptionService.savePrescription(prescription);

        assertThat(result).isNull();
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void savePrescription_returnsSaved_whenValid() {
        Prescription prescription = new Prescription();
        prescription.setAppointmentId(1L);
        prescription.setPatientName("Patient");
        prescription.setMedication("Med");
        prescription.setDosage("1mg");
        Prescription saved = new Prescription();
        saved.setId("pres-1");
        when(prescriptionRepository.findByAppointmentId(1L)).thenReturn(List.of());
        when(prescriptionRepository.save(prescription)).thenReturn(saved);

        Prescription result = prescriptionService.savePrescription(prescription);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("pres-1");
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    void updatePrescription_throws_whenIdIsNull() {
        Prescription prescription = new Prescription();
        prescription.setId(null);
        prescription.setAppointmentId(1L);

        assertThatThrownBy(() -> prescriptionService.updatePrescription(prescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prescription id is required");
    }

    @Test
    void updatePrescription_throws_whenIdIsBlank() {
        Prescription prescription = new Prescription();
        prescription.setId("");

        assertThatThrownBy(() -> prescriptionService.updatePrescription(prescription))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePrescription_returnsNull_whenPrescriptionNotFound() {
        Prescription prescription = new Prescription();
        prescription.setId("pres-1");
        when(prescriptionRepository.existsById("pres-1")).thenReturn(false);

        Prescription result = prescriptionService.updatePrescription(prescription);

        assertThat(result).isNull();
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void updatePrescription_returnsUpdated_whenFound() {
        Prescription prescription = new Prescription();
        prescription.setId("pres-1");
        prescription.setPatientName("Patient");
        prescription.setMedication("Med");
        prescription.setDosage("2mg");
        when(prescriptionRepository.existsById("pres-1")).thenReturn(true);
        when(prescriptionRepository.save(prescription)).thenReturn(prescription);

        Prescription result = prescriptionService.updatePrescription(prescription);

        assertThat(result).isNotNull();
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    void getPrescriptionByAppointmentId_returnsList_fromRepository() {
        List<Prescription> list = List.of(new Prescription());
        when(prescriptionRepository.findByAppointmentId(1L)).thenReturn(list);

        List<Prescription> result = prescriptionService.getPrescriptionByAppointmentId(1L);

        assertThat(result).hasSize(1);
        verify(prescriptionRepository).findByAppointmentId(1L);
    }

    @Test
    void getPrescriptionByAppointmentId_throws_whenRepositoryThrows() {
        when(prescriptionRepository.findByAppointmentId(1L)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> prescriptionService.getPrescriptionByAppointmentId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}
