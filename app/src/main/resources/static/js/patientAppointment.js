// patientAppointment.js
import { getPatientAppointments, getPatientData } from "./services/patientServices.js";

const tableBody = document.getElementById("patientTableBody");
const token = localStorage.getItem("token");

let allAppointments = [];
let filteredAppointments = [];
let patientId = null;

document.addEventListener("DOMContentLoaded", initializePage);

async function initializePage() {
  try {
    if (!token) throw new Error("No token found");

    const patient = await getPatientData(token);
    if (!patient) throw new Error("Failed to fetch patient details");

    patientId = Number(patient.id);

    const appointmentData = await getPatientAppointments(patientId, token, "patient") || [];
    // Backend returns { id, doctor: { id, name, specialty }, patient: { id, name }, appointmentTime, status }
    // Normalize to flat shape expected by render (patientId, doctorName, appointmentDate, appointmentTimeOnly)
    allAppointments = appointmentData.map(app => {
      const timeStr = app.appointmentTime || "";
      const [datePart, timePart] = timeStr.split("T");
      return {
        id: app.id,
        patientId: app.patient?.id ?? app.patientId,
        patientName: app.patient?.name ?? app.patientName,
        doctorId: app.doctor?.id ?? app.doctorId,
        doctorName: app.doctor?.name ?? app.doctorName,
        appointmentTime: app.appointmentTime,
        appointmentDate: datePart || "",
        appointmentTimeOnly: timePart ? timePart.substring(0, 5) : "",
        status: app.status,
      };
    }).filter(app => app.patientId === patientId);

    renderAppointments(allAppointments);
  } catch (error) {
    console.error("Error loading appointments:", error);
    alert("❌ Failed to load your appointments.");
  }
}

function getStatusLabel(status) {
  if (status === 0) return { text: "Scheduled", class: "status-scheduled" };
  if (status === 1) return { text: "Completed", class: "status-completed" };
  return { text: "Cancelled", class: "status-cancelled" };
}

function renderAppointments(appointments) {
  tableBody.innerHTML = "";

  const actionTh = document.querySelector("#patientTable thead tr th:last-child");
  if (actionTh) {
    actionTh.style.display = "table-cell";
  }

  if (!appointments.length) {
    tableBody.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="empty-state">
            <div class="empty-state-icon">📅</div>
            <div class="empty-state-title">No appointments yet</div>
            <div class="empty-state-text">Your scheduled appointments will appear here.</div>
          </div>
        </td>
      </tr>`;
    return;
  }

  appointments.forEach(appointment => {
    const statusInfo = getStatusLabel(appointment.status);
    const tr = document.createElement("tr");
    const actionCell =
      appointment.status === 0
        ? `<button type="button" class="btn-edit-appointment" title="Edit appointment" aria-label="Edit">
             <img src="../assets/images/edit/edit.png" alt="" />
           </button>`
        : '<span class="cell-actions-empty">—</span>';
    tr.innerHTML = `
      <td>${escapeHtml(appointment.patientName || "You")}</td>
      <td>${escapeHtml(appointment.doctorName || "—")}</td>
      <td>${escapeHtml(appointment.appointmentDate || "—")}</td>
      <td>${escapeHtml(appointment.appointmentTimeOnly || "—")}</td>
      <td><span class="status-badge ${statusInfo.class}">${escapeHtml(statusInfo.text)}</span></td>
      <td>${actionCell}</td>
    `;

    if (appointment.status === 0) {
      const actionBtn = tr.querySelector(".btn-edit-appointment");
      actionBtn?.addEventListener("click", () => redirectToUpdatePage(appointment));
    }

    tableBody.appendChild(tr);
  });
}

function escapeHtml(str) {
  if (str == null) return "";
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

function redirectToUpdatePage(appointment) {
  // Prepare the query parameters
  const queryString = new URLSearchParams({
    appointmentId: appointment.id,
    patientId: appointment.patientId,
    patientName: appointment.patientName || "You",
    doctorName: appointment.doctorName,
    doctorId: appointment.doctorId,
    appointmentDate: appointment.appointmentDate,
    appointmentTime: appointment.appointmentTimeOnly,
  }).toString();

  // Redirect to the update page with the query string
  setTimeout(() => {
    window.location.href = `/pages/updateAppointment.html?${queryString}`;
  }, 100);
}


// Search and Filter Listeners – client-side filter from allAppointments
document.getElementById("searchBar").addEventListener("input", handleFilterChange);
document.getElementById("appointmentFilter").addEventListener("change", handleFilterChange);

function handleFilterChange() {
  const searchBarValue = document.getElementById("searchBar").value.trim().toLowerCase();
  const filterValue = document.getElementById("appointmentFilter").value;
  const today = new Date().toISOString().slice(0, 10);

  let list = allAppointments;

  if (filterValue === "future") {
    list = list.filter(app => (app.appointmentDate || "") >= today);
  } else if (filterValue === "past") {
    list = list.filter(app => (app.appointmentDate || "") < today);
  }

  if (searchBarValue) {
    list = list.filter(
      app => (app.doctorName || "").toLowerCase().includes(searchBarValue)
    );
  }

  filteredAppointments = list;
  renderAppointments(filteredAppointments);
}

