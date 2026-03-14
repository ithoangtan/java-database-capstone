package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private static final int STATUS_CANCELLED = 2;

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TokenService tokenService;

    public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
                             PatientRepository patientRepository, TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsForDoctor(String doctorEmail, LocalDate date, String patientName) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail);
        if (doctor == null) {
            return List.of();
        }
        if (date == null) {
            if (patientName != null && !patientName.isBlank() && !"null".equalsIgnoreCase(patientName)) {
                return appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseOrderByAppointmentTimeDesc(
                        doctor.getId(), patientName.trim());
            }
            return appointmentRepository.findByDoctorIdOrderByAppointmentTimeDesc(doctor.getId());
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        if (patientName != null && !patientName.isBlank() && !"null".equalsIgnoreCase(patientName)) {
            return appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctor.getId(), patientName.trim(), start, end);
        }
        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
    }

    /** Latest date that has at least one appointment for the doctor (from DB). */
    @Transactional(readOnly = true)
    public Optional<LocalDate> getLatestAppointmentDateForDoctor(String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail);
        if (doctor == null) return Optional.empty();
        return appointmentRepository.findFirstByDoctor_IdOrderByAppointmentTimeDesc(doctor.getId())
                .map(a -> a.getAppointmentTime().toLocalDate());
    }

    /** Updates the status of an appointment by id (e.g. set to 1 when prescription is added). */
    @Transactional
    public boolean updateAppointmentStatus(Long appointmentId, int status) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) return false;
        appointmentRepository.updateStatus(status, appointmentId);
        return true;
    }

    /**
     * Book a new appointment. Patient id must not be duplicated for the same doctor and time:
     * no existing appointment with same (doctor_id, patient_id, appointment_time).
     * @return the saved appointment, or null if doctor/patient not found or duplicate exists
     */
    @Transactional
    public Appointment bookAppointment(Long doctorId, Long patientId, LocalDateTime appointmentTime, int status) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (doctor == null || patient == null) {
            return null;
        }
        if (appointmentRepository.existsByDoctor_IdAndPatient_IdAndAppointmentTime(doctorId, patientId, appointmentTime)) {
            return null; // duplicate: same doctor, same patient, same time
        }
        Appointment appointment = new Appointment(doctor, patient, appointmentTime, status);
        return appointmentRepository.save(appointment);
    }

    /** True if an appointment already exists for this (doctor, patient, time). */
    @Transactional(readOnly = true)
    public boolean hasExistingAppointment(Long doctorId, Long patientId, LocalDateTime appointmentTime) {
        return appointmentRepository.existsByDoctor_IdAndPatient_IdAndAppointmentTime(doctorId, patientId, appointmentTime);
    }

    /**
     * Update an existing appointment (date/time). Only the patient who owns the appointment may update.
     * Rejects if another appointment already exists for same (doctor, patient, time) with a different id.
     */
    @Transactional
    public Appointment updateAppointment(Long appointmentId, Long patientId, Long doctorId, LocalDateTime appointmentTime, int status) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) return null;
        Appointment appointment = opt.get();
        if (!appointment.getPatient().getId().equals(patientId)) {
            return null; // not allowed to update another patient's appointment
        }
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (doctor == null || patient == null) return null;
        if (appointmentRepository.countByDoctor_IdAndPatient_IdAndAppointmentTimeAndIdNot(doctorId, patientId, appointmentTime, appointmentId) > 0) {
            return null; // duplicate slot
        }
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    /**
     * Cancel an appointment. Validates that the token belongs to the patient who owns the appointment.
     * Sets status to cancelled (2).
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        String email = tokenService.extractSubject(token);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token"));
        }
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token"));
        }
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Appointment not found"));
        }
        Appointment appointment = opt.get();
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Not authorized to cancel this appointment"));
        }
        appointment.setStatus(STATUS_CANCELLED);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
    }
}
