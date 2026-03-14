// appointmentRecordService.js
import { API_BASE_URL } from "../config/config.js";
const APPOINTMENT_API = `${API_BASE_URL}/appointments`;

/** Lấy ngày mới nhất có lịch của doctor (để dashboard tự chọn ngày khi load). Trả về chuỗi YYYY-MM-DD hoặc null. */
export async function getLatestAppointmentDate(token) {
  const response = await fetch(`${APPOINTMENT_API}/latestDate/${token}`);
  if (!response.ok) return null;
  const data = await response.json();
  const date = data?.date;
  return date && String(date).trim() ? String(date).trim() : null;
}

//This is for the doctor to get all the patient Appointments. Pass date "null" or empty to get all appointments.
export async function getAllAppointments(date, patientName, token) {
  const dateSegment = (date != null && String(date).trim() !== "") ? date : "null";
  const nameSegment = (patientName != null && String(patientName).trim() !== "") ? patientName : "null";
  const response = await fetch(`${APPOINTMENT_API}/${dateSegment}/${nameSegment}/${token}`);
  if (response.status === 401) {
    throw new Error("Unauthorized");
  }
  if (!response.ok) {
    throw new Error("Failed to fetch appointments");
  }
  return await response.json();
}

export async function bookAppointment(appointment, token) {
  try {
    const response = await fetch(`${APPOINTMENT_API}/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while booking appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}

export async function updateAppointment(appointment, token) {
  try {
    const response = await fetch(`${APPOINTMENT_API}/${token}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while booking appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}
