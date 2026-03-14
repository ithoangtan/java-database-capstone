package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    private AppointmentService appointmentService;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, doctorRepository, patientRepository);
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setEmail("doctor@test.com");
        patient = new Patient();
        patient.setId(2L);
    }

    @Test
    void getAppointmentsForDoctor_returnsEmpty_whenDoctorNotFound() {
        when(doctorRepository.findByEmail("unknown@test.com")).thenReturn(null);

        List<Appointment> result = appointmentService.getAppointmentsForDoctor("unknown@test.com", null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getAppointmentsForDoctor_returnsAllByDoctor_whenDateIsNullAndNoPatientName() {
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(1L)).thenReturn(List.of());

        List<Appointment> result = appointmentService.getAppointmentsForDoctor("doctor@test.com", null, null);

        assertThat(result).isEmpty();
        verify(appointmentRepository).findByDoctorIdOrderByAppointmentTimeDesc(1L);
    }

    @Test
    void getAppointmentsForDoctor_returnsFilteredByPatientName_whenDateIsNull() {
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseOrderByAppointmentTimeDesc(1L, "John"))
                .thenReturn(List.of());

        List<Appointment> result = appointmentService.getAppointmentsForDoctor("doctor@test.com", null, "John");

        assertThat(result).isEmpty();
        verify(appointmentRepository).findByDoctorIdAndPatient_NameContainingIgnoreCaseOrderByAppointmentTimeDesc(1L, "John");
    }

    @Test
    void getAppointmentsForDoctor_returnsByDate_whenDateProvided() {
        LocalDate date = LocalDate.of(2025, 3, 14);
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(anyLong(), any(), any())).thenReturn(List.of());

        List<Appointment> result = appointmentService.getAppointmentsForDoctor("doctor@test.com", date, null);

        assertThat(result).isEmpty();
        verify(appointmentRepository).findByDoctorIdAndAppointmentTimeBetween(eq(1L), any(), any());
    }

    @Test
    void getAppointmentsForDoctor_returnsByDateAndPatientName_whenBothProvided() {
        LocalDate date = LocalDate.of(2025, 3, 14);
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                eq(1L), eq("Jane"), any(), any())).thenReturn(List.of());

        List<Appointment> result = appointmentService.getAppointmentsForDoctor("doctor@test.com", date, "Jane");

        assertThat(result).isEmpty();
        verify(appointmentRepository).findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                eq(1L), eq("Jane"), any(), any());
    }

    @Test
    void getLatestAppointmentDateForDoctor_returnsEmpty_whenDoctorNotFound() {
        when(doctorRepository.findByEmail("unknown@test.com")).thenReturn(null);

        assertThat(appointmentService.getLatestAppointmentDateForDoctor("unknown@test.com")).isEmpty();
    }

    @Test
    void getLatestAppointmentDateForDoctor_returnsDate_whenAppointmentExists() {
        Appointment apt = new Appointment();
        apt.setAppointmentTime(LocalDateTime.of(2025, 3, 14, 10, 0));
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);
        when(appointmentRepository.findFirstByDoctor_IdOrderByAppointmentTimeDesc(1L)).thenReturn(Optional.of(apt));

        Optional<LocalDate> result = appointmentService.getLatestAppointmentDateForDoctor("doctor@test.com");

        assertThat(result).hasValue(LocalDate.of(2025, 3, 14));
    }

    @Test
    void updateAppointmentStatus_returnsFalse_whenAppointmentNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = appointmentService.updateAppointmentStatus(999L, 1);

        assertThat(result).isFalse();
    }

    @Test
    void updateAppointmentStatus_returnsTrue_andUpdates_whenFound() {
        Appointment apt = new Appointment();
        apt.setId(1L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));

        boolean result = appointmentService.updateAppointmentStatus(1L, 1);

        assertThat(result).isTrue();
        verify(appointmentRepository).updateStatus(1, 1L);
    }

    @Test
    void bookAppointment_returnsNull_whenDoctorNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

        Appointment result = appointmentService.bookAppointment(1L, 2L, LocalDateTime.now(), 0);

        assertThat(result).isNull();
    }

    @Test
    void bookAppointment_returnsNull_whenPatientNotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.empty());

        Appointment result = appointmentService.bookAppointment(1L, 2L, LocalDateTime.now(), 0);

        assertThat(result).isNull();
    }

    @Test
    void bookAppointment_returnsNull_whenDuplicateExists() {
        LocalDateTime time = LocalDateTime.of(2025, 3, 14, 9, 0);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsByDoctor_IdAndPatient_IdAndAppointmentTime(1L, 2L, time)).thenReturn(true);

        Appointment result = appointmentService.bookAppointment(1L, 2L, time, 0);

        assertThat(result).isNull();
    }

    @Test
    void bookAppointment_returnsSavedAppointment_whenValid() {
        LocalDateTime time = LocalDateTime.of(2025, 3, 14, 9, 0);
        Appointment saved = new Appointment(doctor, patient, time, 0);
        saved.setId(10L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsByDoctor_IdAndPatient_IdAndAppointmentTime(1L, 2L, time)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

        Appointment result = appointmentService.bookAppointment(1L, 2L, time, 0);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void hasExistingAppointment_returnsTrue_whenExists() {
        LocalDateTime time = LocalDateTime.now();
        when(appointmentRepository.existsByDoctor_IdAndPatient_IdAndAppointmentTime(1L, 2L, time)).thenReturn(true);

        boolean result = appointmentService.hasExistingAppointment(1L, 2L, time);

        assertThat(result).isTrue();
    }

    @Test
    void hasExistingAppointment_returnsFalse_whenNotExists() {
        LocalDateTime time = LocalDateTime.now();
        when(appointmentRepository.existsByDoctor_IdAndPatient_IdAndAppointmentTime(1L, 2L, time)).thenReturn(false);

        boolean result = appointmentService.hasExistingAppointment(1L, 2L, time);

        assertThat(result).isFalse();
    }

    @Test
    void updateAppointment_returnsNull_whenAppointmentNotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        Appointment result = appointmentService.updateAppointment(999L, 2L, 1L, LocalDateTime.now(), 0);

        assertThat(result).isNull();
    }

    @Test
    void updateAppointment_returnsNull_whenPatientIdDoesNotMatch() {
        Appointment apt = new Appointment();
        apt.setId(1L);
        Patient otherPatient = new Patient();
        otherPatient.setId(99L);
        apt.setPatient(otherPatient);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));

        Appointment result = appointmentService.updateAppointment(1L, 2L, 1L, LocalDateTime.now(), 0);

        assertThat(result).isNull();
    }

    @Test
    void updateAppointment_returnsNull_whenDuplicateSlotExists() {
        LocalDateTime time = LocalDateTime.of(2025, 3, 15, 10, 0);
        Appointment apt = new Appointment();
        apt.setId(1L);
        apt.setPatient(patient);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.countByDoctor_IdAndPatient_IdAndAppointmentTimeAndIdNot(1L, 2L, time, 1L)).thenReturn(1L);

        Appointment result = appointmentService.updateAppointment(1L, 2L, 1L, time, 0);

        assertThat(result).isNull();
    }

    @Test
    void updateAppointment_returnsSavedAppointment_whenValid() {
        LocalDateTime time = LocalDateTime.of(2025, 3, 15, 10, 0);
        Appointment apt = new Appointment();
        apt.setId(1L);
        apt.setPatient(patient);
        apt.setDoctor(doctor);
        Appointment saved = new Appointment(doctor, patient, time, 0);
        saved.setId(1L);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.countByDoctor_IdAndPatient_IdAndAppointmentTimeAndIdNot(1L, 2L, time, 1L)).thenReturn(0L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

        Appointment result = appointmentService.updateAppointment(1L, 2L, 1L, time, 0);

        assertThat(result).isNotNull();
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
