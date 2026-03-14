/*
 * doctorCard.js - Creates a reusable doctor card with role-based actions.
 */

import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";
import { showBookingOverlay } from "../loggedPatient.js";
import { openModal } from "./modals.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");

  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name || "N/A";

  const specialization = document.createElement("p");
  specialization.textContent = "Specialty: " + (doctor.specialty || doctor.specialization || "N/A");

  const email = document.createElement("p");
  email.textContent = "Email: " + (doctor.email || "N/A");

  const availability = document.createElement("p");
  const times = doctor.availableTimes || doctor.availability || [];
  availability.textContent = "Availability: " + (Array.isArray(times) ? times.join(", ") : (times || "N/A"));

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
    removeBtn.addEventListener("click", async () => {
      if (!confirm("Are you sure you want to delete this doctor?")) return;
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Session expired. Please log in again.");
        return;
      }
      try {
        const result = await deleteDoctor(doctor.id, token);
        if (result && result.success) {
          card.remove();
          if (typeof alert === "function") alert(result.message || "Doctor deleted.");
        } else {
          alert(result?.message || "Failed to delete doctor.");
        }
      } catch (err) {
        console.error("Delete doctor error:", err);
        alert("Failed to delete doctor.");
      }
    });
    actionsDiv.appendChild(removeBtn);
  } else if (role === "patient" || !role) {
    // Guest / chưa đăng nhập: bấm Book Now → mở modal Login để đăng nhập hoặc đăng ký
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", () => {
      openModal("patientLogin");
    });
    actionsDiv.appendChild(bookNow);
  } else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", async (e) => {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Session expired. Please log in again.");
        return;
      }
      const patientData = await getPatientData(token);
      if (!patientData) {
        alert("Could not load patient data.");
        return;
      }
      showBookingOverlay(e, doctor, patientData);
    });
    actionsDiv.appendChild(bookNow);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}
