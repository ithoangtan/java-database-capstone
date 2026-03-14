package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a new prescription. If one already exists for this appointmentId, returns null
     * so controller can respond with 400. Otherwise saves and returns the saved entity.
     */
    public Prescription savePrescription(Prescription prescription) {
        if (prescription.getAppointmentId() == null) {
            throw new IllegalArgumentException("appointmentId is required");
        }
        List<Prescription> existing = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
        if (!existing.isEmpty()) {
            return null; // caller will respond 400
        }
        return prescriptionRepository.save(prescription);
    }

    /**
     * Updates an existing prescription by id. The prescription must have a non-null id.
     * Returns the updated entity, or null if no prescription exists with that id.
     */
    public Prescription updatePrescription(Prescription prescription) {
        if (prescription.getId() == null || prescription.getId().isBlank()) {
            throw new IllegalArgumentException("Prescription id is required for update");
        }
        if (!prescriptionRepository.existsById(prescription.getId())) {
            return null;
        }
        return prescriptionRepository.save(prescription);
    }

    /**
     * Retrieves all prescriptions for the given appointment ID.
     */
    public List<Prescription> getPrescriptionByAppointmentId(Long appointmentId) {
        try {
            return prescriptionRepository.findByAppointmentId(appointmentId);
        } catch (Exception e) {
            log.warn("getPrescription failed for appointmentId={}", appointmentId, e);
            throw e;
        }
    }
}
