package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
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
}
