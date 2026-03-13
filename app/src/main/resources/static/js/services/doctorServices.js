/*
 * doctorServices.js - API calls for doctor CRUD and filtering.
 */

import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + "/doctor";

export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);
    const data = await response.json();
    return data.doctors || [];
  } catch (error) {
    console.error("Error fetching doctors:", error);
    return [];
  }
}

export async function deleteDoctor(doctorId, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${doctorId}/${token}`, {
      method: "DELETE",
    });
    const data = await response.json().catch(() => ({}));
    return {
      success: response.ok,
      message: data.message || (response.ok ? "Doctor deleted." : "Failed to delete."),
    };
  } catch (error) {
    console.error("Error deleting doctor:", error);
    return { success: false, message: "Network error." };
  }
}

export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${token}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor),
    });
    const data = await response.json().catch(() => ({}));
    return {
      success: response.ok,
      message: data.message || (response.ok ? "Doctor saved." : "Failed to save."),
    };
  } catch (error) {
    console.error("Error saving doctor:", error);
    return { success: false, message: "Network error." };
  }
}

export async function filterDoctors(name, time, specialty) {
  try {
    const params = new URLSearchParams();
    if (name) params.set("name", name);
    if (time) params.set("time", time);
    if (specialty) params.set("specialty", specialty);
    const url = `${DOCTOR_API}/filter?${params.toString()}`;
    const response = await fetch(url);
    if (!response.ok) {
      console.error("Filter doctors error:", response.statusText);
      return { doctors: [] };
    }
    const data = await response.json();
    return { doctors: data.doctors || [] };
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("Something went wrong while filtering.");
    return { doctors: [] };
  }
}
