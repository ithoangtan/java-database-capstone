/*
 * doctorCard.js - Creates a reusable doctor card with role-based actions.
 * Card shows: name, specialty badge, email, AM/PM availability pills.
 */

import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";
import { showBookingOverlay } from "../loggedPatient.js";
import { openModal } from "./modals.js";

/** Returns { hasAM, hasPM, slotsAM, slotsPM } from availability slots like "09:00-10:00", "12:00-13:00" */
function parseAvailabilityAMPM(times) {
  const list = Array.isArray(times) ? times : (times ? [times] : []);
  const slotsAM = [];
  const slotsPM = [];
  for (const slot of list) {
    const s = String(slot).trim();
    if (!s) continue;
    const start = s.split("-")[0] || s.split(" ")[0] || "";
    const hour = parseInt(start.split(":")[0], 10);
    if (isNaN(hour)) continue;
    if (hour < 12) {
      slotsAM.push(s);
    } else {
      slotsPM.push(s);
    }
  }
  return {
    hasAM: slotsAM.length > 0,
    hasPM: slotsPM.length > 0,
    slotsAM,
    slotsPM,
  };
}

/** Title prefixes to skip when getting avatar initial (Dr., Mr., Ms., ...) */
const TITLE_PREFIXES = /^(Dr\.?|Mr\.?|Mrs\.?|Ms\.?|Prof\.?)\s+/i;

/**
 * Get avatar initial: skip Dr./Mr./Ms. then use first letter of the first remaining word.
 * E.g. "Dr. Mark Johnson" -> "M", "John Smith" -> "J"
 */
function getFamilyNameInitial(name) {
  let n = (name || "").trim();
  if (!n) return "D";
  n = n.replace(TITLE_PREFIXES, "").trim();
  const firstWord = n.split(/\s+/)[0];
  return (firstWord?.charAt(0) || "D").toUpperCase();
}

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");

  const header = document.createElement("div");
  header.classList.add("doctor-card-header");

  const avatar = document.createElement("div");
  avatar.classList.add("doctor-card-avatar");
  avatar.setAttribute("aria-hidden", "true");
  avatar.textContent = getFamilyNameInitial(doctor.name);

  const titleBlock = document.createElement("div");
  titleBlock.classList.add("doctor-card-title-block");

  const name = document.createElement("h3");
  name.classList.add("doctor-card-name");
  name.textContent = doctor.name || "N/A";

  const specialtyLabel = document.createElement("span");
  specialtyLabel.classList.add("doctor-card-specialty");
  specialtyLabel.textContent = doctor.specialty || doctor.specialization || "General";

  titleBlock.appendChild(name);
  titleBlock.appendChild(specialtyLabel);
  header.appendChild(avatar);
  header.appendChild(titleBlock);

  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const email = document.createElement("p");
  email.classList.add("doctor-card-email");
  email.textContent = doctor.email || "—";

  const { slotsAM, slotsPM } = parseAvailabilityAMPM(doctor.availableTimes || doctor.availability);
  const availabilityRow = document.createElement("div");
  availabilityRow.classList.add("doctor-availability");
  availabilityRow.setAttribute("aria-label", "Availability: morning and afternoon");

  const labelAvail = document.createElement("span");
  labelAvail.classList.add("doctor-availability-label");
  labelAvail.textContent = "Available:";

  const wrap = document.createElement("div");
  wrap.classList.add("doctor-availability-groups");

  const groupAM = document.createElement("span");
  groupAM.classList.add("avail-group", "avail-am");
  if (!slotsAM.length) groupAM.classList.add("avail-group--empty");
  groupAM.textContent = slotsAM.length ? slotsAM.join(", ") : "—";
  groupAM.title = "Morning";

  const groupPM = document.createElement("span");
  groupPM.classList.add("avail-group", "avail-pm");
  if (!slotsPM.length) groupPM.classList.add("avail-group--empty");
  groupPM.textContent = slotsPM.length ? slotsPM.join(", ") : "—";
  groupPM.title = "Afternoon";

  wrap.appendChild(groupAM);
  wrap.appendChild(groupPM);
  availabilityRow.appendChild(labelAvail);
  availabilityRow.appendChild(wrap);

  infoDiv.appendChild(header);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availabilityRow);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.classList.add("btn-delete");
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
    // Guest / not logged in: Book Now opens login modal to sign in or sign up
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
