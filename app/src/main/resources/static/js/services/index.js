/*
 * services/index.js - Index page: role selection and admin/doctor login handlers.
 */

import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = API_BASE_URL + "/admin";
const DOCTOR_API = API_BASE_URL + "/doctor";

if (typeof window !== "undefined") {
  window.openModal = openModal;
}

window.handleRoleSelect = function (role) {
  setRole(role);
  if (role === "admin") {
    openModal("adminLogin");
  } else if (role === "doctor") {
    openModal("doctorLogin");
  } else if (role === "patient") {
    window.location.href = "/pages/patientDashboard.html";
  }
};

window.adminLoginHandler = async function () {
  try {
    const username = document.getElementById("username")?.value?.trim();
    const password = document.getElementById("password")?.value;
    if (!username || !password) {
      alert("Please enter username and password.");
      return;
    }
    const response = await fetch(`${ADMIN_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    const data = await response.json().catch(() => ({}));
    if (response.ok && data.token) {
      localStorage.setItem("token", data.token);
      selectRole("admin");
      window.location.href = `/adminDashboard/${data.token}`;
    } else {
      alert(data.message || "Invalid credentials. Please try again.");
    }
  } catch (error) {
    console.error("Admin login error:", error);
    alert("Login failed. Please try again.");
  }
};

const EMAIL_REGEX = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
window.doctorLoginHandler = async function () {
  try {
    const email = document.getElementById("email")?.value?.trim();
    const password = document.getElementById("password")?.value;
    if (!email || !password) {
      alert("Please enter email and password.");
      return;
    }
    if (!EMAIL_REGEX.test(email)) {
      alert("Please enter a valid email address.");
      return;
    }
    const response = await fetch(`${DOCTOR_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    const data = await response.json().catch(() => ({}));
    if (response.ok && data.token) {
      localStorage.setItem("token", data.token);
      selectRole("doctor");
      window.location.href = `/doctorDashboard/${data.token}`;
    } else {
      alert(data.message || "Invalid credentials. Please try again.");
    }
  } catch (error) {
    console.error("Doctor login error:", error);
    alert("Login failed. Please try again.");
  }
};
