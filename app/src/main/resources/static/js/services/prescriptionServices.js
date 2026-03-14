// prescriptionServices.js
import { API_BASE_URL } from "../config/config.js";

const PRESCRIPTION_API = API_BASE_URL + "/prescription";

export async function savePrescription(prescription, token) {
  try {
    const response = await fetch(`${PRESCRIPTION_API}/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(prescription),
    });
    let message = "Failed to save prescription.";
    try {
      const result = await response.json();
      message = result.message != null ? result.message : message;
      return { success: response.ok, message };
    } catch (_) {
      if (!response.ok) {
        message = response.status === 401 ? "Invalid or expired token." : message;
      }
      return { success: false, message };
    }
  } catch (error) {
    console.error("Error :: savePrescription ::", error);
    return { success: false, message: error.message || "Network or server error." };
  }
}

export async function updatePrescription(prescription, token) {
  try {
    const response = await fetch(`${PRESCRIPTION_API}/${token}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(prescription),
    });
    let message = "Failed to update prescription.";
    try {
      const result = await response.json();
      message = result.message != null ? result.message : message;
      return { success: response.ok, message };
    } catch (_) {
      if (!response.ok) {
        message = response.status === 401 ? "Invalid or expired token." : message;
      }
      return { success: false, message };
    }
  } catch (error) {
    console.error("Error :: updatePrescription ::", error);
    return { success: false, message: error.message || "Network or server error." };
  }
}

export async function getPrescription(appointmentId, token) {
  const response = await fetch(`${PRESCRIPTION_API}/${appointmentId}/${token}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    let errorData = {};
    try {
      errorData = await response.json();
    } catch (_) {}
    const msg = errorData.message || "Unable to fetch prescription.";
    throw new Error(msg);
  }

  const result = await response.json();
  return result;
}
