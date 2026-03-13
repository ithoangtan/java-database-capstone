package com.project.back_end.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDTO {

    // 1. 'id' field: unique identifier for the appointment (primary key).
    private Long id;
    // 2. 'doctorId' field: ID of the doctor associated with the appointment (simplified, not full Doctor object).
    private Long doctorId;
    // 3. 'doctorName' field: name of the doctor for display.
    private String doctorName;
    // 4. 'patientId' field: ID of the patient associated with the appointment (simplified, not full Patient object).
    private Long patientId;
    // 5. 'patientName' field: name of the patient for display.
    private String patientName;
    // 6. 'patientEmail' field: email of the patient for display.
    private String patientEmail;
    // 7. 'patientPhone' field: phone number of the patient for display.
    private String patientPhone;
    // 8. 'patientAddress' field: address of the patient for display.
    private String patientAddress;
    // 9. 'appointmentTime' field: scheduled date and time of the appointment (LocalDateTime).
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;
    // 10. 'status' field: status of the appointment (e.g. Scheduled:0, Completed:1, Canceled).
    @NotNull
    private Integer status;

    // 14. Constructor: accepts all relevant fields; custom fields (appointmentDate, appointmentTimeOnly, endTime) are derived from appointmentTime.
    public AppointmentDTO() {
    }

    public AppointmentDTO(Long id, Long doctorId, String doctorName, Long patientId, String patientName,
                          String patientEmail, String patientPhone, String patientAddress,
                          LocalDateTime appointmentTime, int status) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // 11. 'appointmentDate' (Custom Getter): derived field - only the date part of the appointment (from appointmentTime).
    public LocalDate getAppointmentDate() {
        return appointmentTime != null ? appointmentTime.toLocalDate() : null;
    }

    // 12. 'appointmentTimeOnly' (Custom Getter): derived field - only the time part of the appointment (from appointmentTime).
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime != null ? appointmentTime.toLocalTime() : null;
    }

    // 13. 'endTime' (Custom Getter): derived field - end time of the appointment (appointmentTime + 1 hour).
    public LocalDateTime getEndTime() {
        return appointmentTime != null ? appointmentTime.plusHours(1) : null;
    }

    // 15. Getters: standard getters for id, doctorId, doctorName, patientId, patientName, patientEmail, patientPhone, patientAddress, appointmentTime, status, appointmentDate, appointmentTimeOnly, endTime.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
