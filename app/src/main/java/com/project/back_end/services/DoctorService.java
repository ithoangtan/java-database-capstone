package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /** Returns list of available time slot strings for the doctor on the given date (slots not yet booked). */
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getAvailableTimes() == null || doctor.getAvailableTimes().isEmpty()) {
            return List.of();
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Appointment> booked = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        List<String> bookedSlots = booked.stream()
                .map(a -> a.getAppointmentTime() != null ? a.getAppointmentTime().toLocalTime().toString() : null)
                .filter(t -> t != null)
                .map(t -> t.length() >= 5 ? t.substring(0, 5) : t)
                .collect(Collectors.toList());
        List<String> available = new ArrayList<>();
        for (String slot : doctor.getAvailableTimes()) {
            String slotStart = slot.split("-")[0].trim();
            if (slotStart.length() >= 5) slotStart = slotStart.substring(0, 5);
            if (!bookedSlots.contains(slotStart)) {
                available.add(slot);
            }
        }
        return available;
    }

    /** Force-load lazy collections so Jackson can serialize after transaction closes. */
    private void initializeForSerialization(List<Doctor> doctors) {
        if (doctors == null) return;
        for (Doctor d : doctors) {
            if (d.getAvailableTimes() != null) {
                d.getAvailableTimes().size();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        initializeForSerialization(doctors);
        return doctors;
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctors(String name, String time, String specialty) {
        List<Doctor> doctors;
        if (name != null && !name.isBlank() && specialty != null && !specialty.isBlank()) {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name.trim(), specialty.trim());
        } else if (name != null && !name.isBlank()) {
            doctors = doctorRepository.findByNameLike(name.trim());
        } else if (specialty != null && !specialty.isBlank()) {
            doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty.trim());
        } else {
            doctors = doctorRepository.findAll();
        }
        if (time != null && !time.isBlank() && ("AM".equalsIgnoreCase(time) || "PM".equalsIgnoreCase(time))) {
            doctors = doctors.stream()
                    .filter(d -> hasSlotInPeriod(d, time))
                    .collect(Collectors.toList());
        }
        initializeForSerialization(doctors);
        return doctors;
    }

    private boolean hasSlotInPeriod(Doctor doctor, String period) {
        if (doctor.getAvailableTimes() == null || doctor.getAvailableTimes().isEmpty()) {
            return false;
        }
        for (String slot : doctor.getAvailableTimes()) {
            String start = slot.split("-")[0].trim();
            if ("AM".equalsIgnoreCase(period) && start.compareTo("12:00") < 0) {
                return true;
            }
            if ("PM".equalsIgnoreCase(period) && start.compareTo("12:00") >= 0) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        if (doctor.getEmail() == null || doctor.getEmail().isBlank()) {
            return 0;
        }
        if (doctorRepository.findByEmail(doctor.getEmail().trim()) != null) {
            return -1;
        }
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        if (doctor == null || doctor.getId() == null) return -1;
        if (!doctorRepository.existsById(doctor.getId())) return -1;
        Doctor existing = doctorRepository.findById(doctor.getId()).orElse(null);
        if (existing == null) return -1;
        if (doctor.getEmail() != null && !doctor.getEmail().equals(existing.getEmail())
                && doctorRepository.findByEmail(doctor.getEmail()) != null) {
            return -2; // duplicate email
        }
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public int deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            return -1;
        }
        try {
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Validates doctor login; returns ResponseEntity with token on success or error message. */
    public ResponseEntity<java.util.Map<String, String>> validateDoctor(Login login) {
        if (login == null || login.getIdentifier() == null || login.getIdentifier().isBlank()
                || login.getPassword() == null || login.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Email and password are required."));
        }
        Doctor doctor = doctorRepository.findByEmail(login.getIdentifier().trim());
        if (doctor == null || !login.getPassword().equals(doctor.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Invalid credentials. Please try again."));
        }
        String token = tokenService.generateToken(doctor.getEmail());
        return ResponseEntity.ok(java.util.Map.of("token", token));
    }
}
