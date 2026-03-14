package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
                             PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
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

    /** Ngày mới nhất có ít nhất một appointment của doctor (theo DB). */
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
}
