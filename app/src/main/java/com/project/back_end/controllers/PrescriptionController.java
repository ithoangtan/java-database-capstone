package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;
    private final AppointmentService appointmentService;

    public PrescriptionController(PrescriptionService prescriptionService, Service service,
                                  AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.service = service;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> savePrescription(
            @Valid @RequestBody Prescription prescription,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tr = service.validateToken(token, "doctor");
        if (tr.getStatusCode().isError()) {
            return ResponseEntity.status(tr.getStatusCode()).body(new HashMap<>(tr.getBody() != null ? tr.getBody() : Map.of()));
        }
        if (prescription.getAppointmentId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "appointmentId is required."));
        }
        Prescription saved = prescriptionService.savePrescription(prescription);
        if (saved == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "A prescription already exists for this appointment."));
        }
        appointmentService.updateAppointmentStatus(prescription.getAppointmentId(), 1);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Prescription saved successfully.", "id", saved.getId()));
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updatePrescription(
            @Valid @RequestBody Prescription prescription,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tr = service.validateToken(token, "doctor");
        if (tr.getStatusCode().isError()) {
            return ResponseEntity.status(tr.getStatusCode()).body(new HashMap<>(tr.getBody() != null ? tr.getBody() : Map.of()));
        }
        if (prescription.getId() == null || prescription.getId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Prescription id is required for update."));
        }
        Prescription updated = prescriptionService.updatePrescription(prescription);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Prescription not found."));
        }
        return ResponseEntity.ok(Map.of("message", "Prescription updated successfully.", "id", updated.getId()));
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tr = service.validateToken(token, "doctor");
        if (tr.getStatusCode().isError()) {
            Map<String, Object> err = new HashMap<>(tr.getBody() != null ? tr.getBody() : Map.of());
            err.put("prescription", List.of());
            return ResponseEntity.status(tr.getStatusCode()).body(err);
        }
        try {
            List<Prescription> list = prescriptionService.getPrescriptionByAppointmentId(appointmentId);
            return ResponseEntity.ok(Map.of("prescription", list != null ? list : List.of()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch prescription.", "prescription", List.of()));
        }
    }
}
