package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(doctorRepository, appointmentRepository);
    }

    @Test
    void getDoctors_returnsAllFromRepository() {
        List<Doctor> doctors = List.of(new Doctor());
        when(doctorRepository.findAll()).thenReturn(doctors);

        List<Doctor> result = doctorService.getDoctors();

        assertThat(result).hasSize(1);
        verify(doctorRepository).findAll();
    }

    @Test
    void filterDoctors_returnsAll_whenNoFilters() {
        List<Doctor> doctors = List.of(new Doctor());
        when(doctorRepository.findAll()).thenReturn(doctors);

        List<Doctor> result = doctorService.filterDoctors(null, null, null);

        assertThat(result).hasSize(1);
        verify(doctorRepository).findAll();
    }

    @Test
    void filterDoctors_filtersByName_whenOnlyNameProvided() {
        List<Doctor> doctors = List.of(new Doctor());
        when(doctorRepository.findByNameLike("John")).thenReturn(doctors);

        List<Doctor> result = doctorService.filterDoctors("John", null, null);

        assertThat(result).hasSize(1);
        verify(doctorRepository).findByNameLike("John");
    }

    @Test
    void filterDoctors_filtersBySpecialty_whenOnlySpecialtyProvided() {
        List<Doctor> doctors = List.of(new Doctor());
        when(doctorRepository.findBySpecialtyIgnoreCase("Cardiology")).thenReturn(doctors);

        List<Doctor> result = doctorService.filterDoctors(null, null, "Cardiology");

        assertThat(result).hasSize(1);
        verify(doctorRepository).findBySpecialtyIgnoreCase("Cardiology");
    }

    @Test
    void filterDoctors_filtersByNameAndSpecialty_whenBothProvided() {
        List<Doctor> doctors = List.of(new Doctor());
        when(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase("John", "Cardiology")).thenReturn(doctors);

        List<Doctor> result = doctorService.filterDoctors("John", null, "Cardiology");

        assertThat(result).hasSize(1);
        verify(doctorRepository).findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase("John", "Cardiology");
    }

    @Test
    void filterDoctors_filtersByTimeAM_keepsOnlyDoctorsWithAMSlots() {
        Doctor withAM = new Doctor();
        withAM.setAvailableTimes(List.of("09:00-10:00"));
        Doctor withPMOnly = new Doctor();
        withPMOnly.setAvailableTimes(List.of("14:00-15:00"));
        when(doctorRepository.findAll()).thenReturn(List.of(withAM, withPMOnly));

        List<Doctor> result = doctorService.filterDoctors(null, "AM", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailableTimes()).contains("09:00-10:00");
    }

    @Test
    void filterDoctors_filtersByTimePM_keepsOnlyDoctorsWithPMSlots() {
        Doctor withPM = new Doctor();
        withPM.setAvailableTimes(List.of("14:00-15:00"));
        Doctor withAMOnly = new Doctor();
        withAMOnly.setAvailableTimes(List.of("09:00-10:00"));
        when(doctorRepository.findAll()).thenReturn(List.of(withPM, withAMOnly));

        List<Doctor> result = doctorService.filterDoctors(null, "PM", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvailableTimes()).contains("14:00-15:00");
    }

    @Test
    void saveDoctor_returnsZero_whenEmailIsBlank() {
        Doctor doctor = new Doctor();
        doctor.setEmail("");

        int result = doctorService.saveDoctor(doctor);

        assertThat(result).isEqualTo(0);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void saveDoctor_returnsZero_whenEmailIsNull() {
        Doctor doctor = new Doctor();
        doctor.setEmail(null);

        int result = doctorService.saveDoctor(doctor);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void saveDoctor_returnsMinusOne_whenEmailAlreadyExists() {
        Doctor doctor = new Doctor();
        doctor.setEmail("existing@test.com");
        when(doctorRepository.findByEmail("existing@test.com")).thenReturn(new Doctor());

        int result = doctorService.saveDoctor(doctor);

        assertThat(result).isEqualTo(-1);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void saveDoctor_returnsOne_whenSuccess() {
        Doctor doctor = new Doctor();
        doctor.setEmail("new@test.com");
        when(doctorRepository.findByEmail("new@test.com")).thenReturn(null);
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        int result = doctorService.saveDoctor(doctor);

        assertThat(result).isEqualTo(1);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void saveDoctor_returnsZero_whenRepositoryThrows() {
        Doctor doctor = new Doctor();
        doctor.setEmail("new@test.com");
        when(doctorRepository.findByEmail("new@test.com")).thenReturn(null);
        when(doctorRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        int result = doctorService.saveDoctor(doctor);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void deleteDoctor_returnsMinusOne_whenDoctorNotFound() {
        when(doctorRepository.existsById(999L)).thenReturn(false);

        int result = doctorService.deleteDoctor(999L);

        assertThat(result).isEqualTo(-1);
        verify(appointmentRepository, never()).deleteAllByDoctorId(any());
        verify(doctorRepository, never()).deleteById(any());
    }

    @Test
    void deleteDoctor_returnsOne_whenSuccess() {
        when(doctorRepository.existsById(1L)).thenReturn(true);

        int result = doctorService.deleteDoctor(1L);

        assertThat(result).isEqualTo(1);
        verify(appointmentRepository).deleteAllByDoctorId(1L);
        verify(doctorRepository).deleteById(1L);
    }

    @Test
    void deleteDoctor_returnsZero_whenRepositoryThrows() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(appointmentRepository).deleteAllByDoctorId(1L);

        int result = doctorService.deleteDoctor(1L);

        assertThat(result).isEqualTo(0);
    }
}
