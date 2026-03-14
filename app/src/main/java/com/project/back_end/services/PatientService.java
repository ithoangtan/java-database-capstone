package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /** Creates a patient. Returns 1 on success, 0 on failure. */
    @Transactional
    public int createPatient(Patient patient) {
        if (patient == null) return 0;
        if (patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) != null) {
            return 0;
        }
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        String email = tokenService.extractSubject(token);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token", "appointments", List.<AppointmentDTO>of()));
        }
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null || !patient.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied", "appointments", List.<AppointmentDTO>of()));
        }
        List<Appointment> appointments = appointmentRepository.findByPatient_IdOrderByAppointmentTimeDesc(id);
        List<AppointmentDTO> dtos = toAppointmentDTOs(appointments);
        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        List<Appointment> appointments;
        if ("past".equalsIgnoreCase(condition)) {
            appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 1);
        } else if ("future".equalsIgnoreCase(condition)) {
            appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 0);
        } else {
            appointments = appointmentRepository.findByPatient_IdOrderByAppointmentTimeDesc(id);
        }
        List<AppointmentDTO> dtos = toAppointmentDTOs(appointments);
        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(
                name != null ? name : "", patientId);
        List<AppointmentDTO> dtos = toAppointmentDTOs(appointments);
        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        int status = "past".equalsIgnoreCase(condition) ? 1 : ("future".equalsIgnoreCase(condition) ? 0 : -1);
        List<Appointment> appointments;
        if (status >= 0) {
            appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(
                    name != null ? name : "", patientId, status);
        } else {
            appointments = appointmentRepository.filterByDoctorNameAndPatientId(name != null ? name : "", patientId);
        }
        List<AppointmentDTO> dtos = toAppointmentDTOs(appointments);
        return ResponseEntity.ok(Map.of("appointments", dtos));
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        String email = tokenService.extractSubject(token);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token"));
        }
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found"));
        }
        return ResponseEntity.ok(Map.of(
                "patient", Map.of(
                        "id", patient.getId(),
                        "name", patient.getName(),
                        "email", patient.getEmail(),
                        "phone", patient.getPhone(),
                        "address", patient.getAddress() != null ? patient.getAddress() : ""
                )
        ));
    }

    /**
     * Filter patient appointments by condition and/or doctor name. Token identifies the patient.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        String email = tokenService.extractSubject(token);
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token", "appointments", List.of()));
        }
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired token", "appointments", List.of()));
        }
        Long patientId = patient.getId();
        boolean hasCondition = condition != null && !condition.isBlank() && !"null".equalsIgnoreCase(condition);
        boolean hasName = name != null && !name.isBlank() && !"null".equalsIgnoreCase(name);
        if (hasCondition && hasName) {
            return filterByDoctorAndCondition(condition, name, patientId);
        }
        if (hasCondition) {
            return filterByCondition(condition, patientId);
        }
        if (hasName) {
            return filterByDoctor(name, patientId);
        }
        return getPatientAppointment(patientId, token);
    }

    private List<AppointmentDTO> toAppointmentDTOs(List<Appointment> appointments) {
        if (appointments == null) return List.of();
        List<AppointmentDTO> list = new ArrayList<>();
        for (Appointment a : appointments) {
            list.add(toDTO(a));
        }
        return list;
    }

    private static AppointmentDTO toDTO(Appointment a) {
        if (a == null) return null;
        return new AppointmentDTO(
                a.getId(),
                a.getDoctor() != null ? a.getDoctor().getId() : null,
                a.getDoctor() != null ? a.getDoctor().getName() : null,
                a.getPatient() != null ? a.getPatient().getId() : null,
                a.getPatient() != null ? a.getPatient().getName() : null,
                a.getPatient() != null ? a.getPatient().getEmail() : null,
                a.getPatient() != null ? a.getPatient().getPhone() : null,
                a.getPatient() != null ? a.getPatient().getAddress() : null,
                a.getAppointmentTime(),
                a.getStatus()
        );
    }
}
