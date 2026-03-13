/*
 * doctorDashboard.js - Doctor dashboard: load appointments, search, date filter.
 */

import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

let selectedDate = new Date().toISOString().slice(0, 10);
let patientName = null;

function loadAppointments() {
  const token = localStorage.getItem("token");
  if (!token) {
    window.location.href = "/";
    return;
  }

  const tbody = document.getElementById("patientTableBody");
  const noRecord = document.getElementById("noPatientRecord");
  if (!tbody) return;

  getAllAppointments(selectedDate, patientName || "null", token)
    .then((data) => {
      tbody.innerHTML = "";
      const appointments = data?.appointments || data || [];
      if (appointments.length === 0) {
        if (noRecord) {
          noRecord.style.display = "block";
          noRecord.textContent = "No appointments found for the selected date.";
        }
        return;
      }
      if (noRecord) noRecord.style.display = "none";
      appointments.forEach((apt) => {
        const patient = apt.patient || apt;
        const appointmentId = apt.id || apt.appointmentId;
        const doctorId = apt.doctor?.id || apt.doctorId;
        const row = createPatientRow(patient, appointmentId, doctorId);
        tbody.appendChild(row);
      });
    })
    .catch((err) => {
      console.error("Failed to load appointments:", err);
      tbody.innerHTML = "";
      if (noRecord) {
        noRecord.style.display = "block";
        noRecord.textContent = "Error loading appointments. Try again later.";
      }
    });
}

document.addEventListener("DOMContentLoaded", () => {
  const datePicker = document.getElementById("datePicker");
  if (datePicker) {
    datePicker.value = selectedDate;
    datePicker.addEventListener("change", () => {
      selectedDate = datePicker.value;
      loadAppointments();
    });
  }

  const todayBtn = document.getElementById("todayButton");
  if (todayBtn) {
    todayBtn.addEventListener("click", () => {
      selectedDate = new Date().toISOString().slice(0, 10);
      if (datePicker) datePicker.value = selectedDate;
      loadAppointments();
    });
  }

  const searchBar = document.getElementById("searchBar");
  if (searchBar) {
    searchBar.addEventListener("input", () => {
      const val = searchBar.value?.trim();
      patientName = val.length > 0 ? val : null;
      loadAppointments();
    });
  }

  loadAppointments();
});
