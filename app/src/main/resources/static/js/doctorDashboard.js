/*
 * doctorDashboard.js - Doctor dashboard: load appointments, search, date filter.
 */

import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

/** Mặc định null để lần đầu vào hiển thị toàn bộ danh sách (all). */
let selectedDate = "";
let patientName = null;

/** Format appointment time for display: "YYYY-MM-DD HH:mm" or date only. */
function formatAppointmentDateTime(dateTime) {
  if (!dateTime) return "";
  const s = String(dateTime);
  if (s.length >= 16) return s.slice(0, 10) + " " + s.slice(11, 16);
  if (s.length >= 10) return s.slice(0, 10);
  return s;
}

/** Lấy token: ưu tiên từ URL (/doctorDashboard/TOKEN), không có thì dùng localStorage. Đồng bộ token từ URL vào localStorage. */
function getToken() {
  const pathParts = window.location.pathname.split("/").filter(Boolean);
  if (pathParts[0] === "doctorDashboard" && pathParts[1] && pathParts[1].length > 50) {
    const tokenFromUrl = pathParts[1];
    localStorage.setItem("token", tokenFromUrl);
    return tokenFromUrl;
  }
  return localStorage.getItem("token");
}

function loadAppointments() {
  const token = getToken();
  if (!token) {
    window.location.href = "/";
    return;
  }

  const tbody = document.getElementById("patientTableBody");
  const noRecord = document.getElementById("noPatientRecord");
  if (!tbody) return;

  tbody.innerHTML = "<tr><td colspan=\"6\" class=\"loading-cell\">Loading appointments…</td></tr>";
  if (noRecord) noRecord.style.display = "none";

  const dateParam = (selectedDate && String(selectedDate).trim()) ? selectedDate : "null";
  getAllAppointments(dateParam, patientName || "null", token)
    .then((data) => {
      tbody.innerHTML = "";
      const appointments = data?.appointments || data || [];
      if (appointments.length === 0) {
        if (noRecord) {
          noRecord.style.display = "block";
          noRecord.textContent = dateParam === "null"
            ? "No appointments found."
            : "No appointments found for the selected date.";
        }
        return;
      }
      if (noRecord) noRecord.style.display = "none";
      // Mỗi bệnh nhân chỉ hiển thị một dòng (lấy appointment mới nhất vì list đã sort DESC)
      const seenPatientIds = new Set();
      const uniqueByPatient = appointments.filter((apt) => {
        const patient = apt.patient || apt;
        const patientId = patient?.id ?? apt?.patientId;
        if (patientId == null) return true;
        if (seenPatientIds.has(patientId)) return false;
        seenPatientIds.add(patientId);
        return true;
      });
      uniqueByPatient.forEach((apt) => {
        const patient = apt.patient || apt;
        const appointmentId = apt.id || apt.appointmentId;
        const doctorId = apt.doctor?.id || apt.doctorId;
        const appointmentDateTime = formatAppointmentDateTime(apt.appointmentTime || apt.appointmentDate);
        const row = createPatientRow(patient, appointmentId, doctorId, appointmentDateTime);
        tbody.appendChild(row);
      });
    })
    .catch((err) => {
      console.error("Failed to load appointments:", err);
      tbody.innerHTML = "";
      if (noRecord) {
        noRecord.style.display = "block";
        noRecord.textContent = err && err.message === "Unauthorized"
          ? "Session expired. Redirecting to login…"
          : "Error loading appointments. Try again later.";
      }
      if (err && err.message === "Unauthorized") {
        setTimeout(() => { window.location.href = "/"; }, 1500);
      }
    });
}

document.addEventListener("DOMContentLoaded", async () => {
  const datePicker = document.getElementById("datePicker");
  const token = getToken();
  if (!token) {
    window.location.href = "/";
    return;
  }

  // Mặc định ô ngày để trống → gửi "null" → API trả về toàn bộ appointments, sort mới nhất trên cùng
  if (datePicker) {
    datePicker.value = selectedDate;
    const onDateInputOrChange = () => {
      selectedDate = datePicker.value || "";
      loadAppointments();
    };
    datePicker.addEventListener("change", onDateInputOrChange);
    datePicker.addEventListener("input", onDateInputOrChange);
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
