/*
 * adminDashboard.js - Admin dashboard: load doctors, filter, add doctor modal.
 */

import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

function openModal(type) {
  if (typeof window.openModal === "function") {
    window.openModal(type);
  }
}

function loadDoctorCards() {
  getDoctors()
    .then((doctors) => {
      const contentDiv = document.getElementById("content");
      if (!contentDiv) return;
      contentDiv.innerHTML = "";
      (doctors || []).forEach((doctor) => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
      });
    })
    .catch((error) => {
      console.error("Failed to load doctors:", error);
    });
}

function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");
  const name = searchBar?.value?.trim() || null;
  const time = filterTime?.value || null;
  const specialty = filterSpecialty?.value || null;

  filterDoctors(name, time, specialty)
    .then((response) => {
      const doctors = response.doctors || [];
      const contentDiv = document.getElementById("content");
      if (!contentDiv) return;
      contentDiv.innerHTML = "";
      if (doctors.length > 0) {
        doctors.forEach((doctor) => {
          contentDiv.appendChild(createDoctorCard(doctor));
        });
      } else {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
      }
    })
    .catch((error) => {
      console.error("Failed to filter doctors:", error);
      alert("An error occurred while filtering doctors.");
    });
}

export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;
  contentDiv.innerHTML = "";
  (doctors || []).forEach((doctor) => {
    contentDiv.appendChild(createDoctorCard(doctor));
  });
}

window.adminAddDoctor = async function () {
  const errorEl = document.getElementById("addDoctorErrorMessage");
  if (errorEl) {
    errorEl.textContent = "";
    errorEl.style.display = "none";
  }

  const name = document.getElementById("doctorName")?.value?.trim();
  const email = document.getElementById("doctorEmail")?.value?.trim();
  const phone = document.getElementById("doctorPhone")?.value?.trim();
  const password = document.getElementById("doctorPassword")?.value;
  const specialty = document.getElementById("specialization")?.value?.trim();
  const checkboxes = document.querySelectorAll('input[name="availability"]:checked');
  const availableTimes = Array.from(checkboxes).map((cb) => cb.value);

  if (!name || !email || !password) {
    showAddDoctorError("Please fill in name, email, and password.");
    return;
  }

  const token = localStorage.getItem("token");
  if (!token) {
    showAddDoctorError("Session expired. Please log in again.");
    return;
  }

  const doctor = {
    name,
    email,
    phone: phone || null,
    password,
    specialty: specialty || "General",
    availableTimes: availableTimes.length ? availableTimes : ["09:00-10:00"],
  };

  const result = await saveDoctor(doctor, token);
  if (result.success) {
    if (errorEl) errorEl.style.display = "none";
    document.getElementById("modal").style.display = "none";
    loadDoctorCards();
  } else {
    showAddDoctorError(result.message || "Failed to add doctor.");
  }
};

function showAddDoctorError(message) {
  const errorEl = document.getElementById("addDoctorErrorMessage");
  if (errorEl) {
    errorEl.textContent = message;
    errorEl.style.display = "block";
  } else {
    alert(message);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");
  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
  if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});
