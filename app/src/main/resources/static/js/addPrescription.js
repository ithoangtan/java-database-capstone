import { savePrescription, updatePrescription, getPrescription } from "./services/prescriptionServices.js";

const MIN_MEDICATION_LENGTH = 3;
const MAX_MEDICATION_LENGTH = 100;
const MAX_NOTES_LENGTH = 200;

document.addEventListener("DOMContentLoaded", async () => {
  const form = document.getElementById("prescriptionForm");
  const formErrorEl = document.getElementById("formError");
  const savePrescriptionBtn = document.getElementById("savePrescription");
  const patientNameInput = document.getElementById("patientName");
  const medicinesInput = document.getElementById("medicines");
  const dosageInput = document.getElementById("dosage");
  const notesInput = document.getElementById("notes");
  const heading = document.getElementById("heading");

  const urlParams = new URLSearchParams(window.location.search);
  const appointmentIdParam = urlParams.get("appointmentId");
  const appointmentId = appointmentIdParam ? Number(appointmentIdParam) : null;
  const mode = urlParams.get("mode");
  const token = localStorage.getItem("token");
  const patientName = urlParams.get("patientName");

  let isEditMode = false;
  let existingPrescriptionId = null;

  if (patientNameInput && patientName) {
    patientNameInput.value = patientName;
  }

  if (appointmentId && token) {
    try {
      const response = await getPrescription(appointmentId, token);
      if (response.prescription && response.prescription.length > 0) {
        const existing = response.prescription[0];
        existingPrescriptionId = existing.id || null;
        isEditMode = !!existingPrescriptionId;
        if (patientNameInput) patientNameInput.value = existing.patientName || patientName || "";
        if (medicinesInput) medicinesInput.value = existing.medication || "";
        if (dosageInput) dosageInput.value = existing.dosage || "";
        if (notesInput) notesInput.value = existing.doctorNotes || "";
      }
    } catch (err) {
      console.warn("No existing prescription or load failed:", err);
    }
  }

  if (heading) {
    if (mode === "view") {
      heading.innerHTML = `View <span>Prescription</span>`;
    } else if (isEditMode) {
      heading.innerHTML = `Edit <span>Prescription</span>`;
    } else {
      heading.innerHTML = `Add <span>Prescription</span>`;
    }
  }
  if (savePrescriptionBtn) {
    savePrescriptionBtn.textContent = isEditMode ? "Update Prescription" : "Add Prescription";
  }

  if (mode === "view") {
    if (patientNameInput) patientNameInput.disabled = true;
    if (medicinesInput) medicinesInput.disabled = true;
    if (dosageInput) dosageInput.disabled = true;
    if (notesInput) notesInput.disabled = true;
    if (savePrescriptionBtn) savePrescriptionBtn.style.display = "none";
  }

  function showFormError(msg) {
    if (!formErrorEl) return;
    formErrorEl.textContent = msg || "";
    formErrorEl.classList.toggle("visible", !!msg);
  }

  function showFieldError(id, msg) {
    const el = document.getElementById(id);
    if (el) {
      el.textContent = msg || "";
    }
  }

  function validateForm() {
    showFormError("");
    showFieldError("medicinesError", "");
    showFieldError("dosageError", "");

    const medication = (medicinesInput?.value || "").trim();
    const dosage = (dosageInput?.value || "").trim();
    const patient = (patientNameInput?.value || "").trim();

    const errors = [];

    if (!appointmentId || Number.isNaN(appointmentId)) {
      errors.push("Invalid appointment. Please go back and open this page from an appointment.");
    }
    if (!token) {
      errors.push("You must be logged in as a doctor.");
    }
    if (patient.length < 3) {
      errors.push("Patient name is required (min 3 characters).");
    }
    if (medication.length < MIN_MEDICATION_LENGTH) {
      errors.push(`Medication must be at least ${MIN_MEDICATION_LENGTH} characters.`);
      showFieldError("medicinesError", `At least ${MIN_MEDICATION_LENGTH} characters required.`);
    }
    if (medication.length > MAX_MEDICATION_LENGTH) {
      errors.push(`Medication must be at most ${MAX_MEDICATION_LENGTH} characters.`);
      showFieldError("medicinesError", `Maximum ${MAX_MEDICATION_LENGTH} characters.`);
    }
    if (!dosage.trim()) {
      errors.push("Dosage instructions are required.");
      showFieldError("dosageError", "Dosage is required.");
    }
    const notes = (notesInput?.value || "").trim();
    if (notes.length > MAX_NOTES_LENGTH) {
      errors.push(`Additional notes must be at most ${MAX_NOTES_LENGTH} characters.`);
    }

    if (errors.length > 0) {
      showFormError(errors.join(" "));
      medicinesInput?.classList.toggle("invalid", medication.length > 0 && (medication.length < MIN_MEDICATION_LENGTH || medication.length > MAX_MEDICATION_LENGTH));
      dosageInput?.classList.toggle("invalid", !dosage.trim());
      return false;
    }

    medicinesInput?.classList.remove("invalid");
    dosageInput?.classList.remove("invalid");
    return true;
  }

  form?.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (mode === "view") return;

    if (!validateForm()) return;

    const prescription = {
      patientName: (patientNameInput?.value || "").trim(),
      medication: (medicinesInput?.value || "").trim(),
      dosage: (dosageInput?.value || "").trim(),
      doctorNotes: (notesInput?.value || "").trim() || null,
      appointmentId: appointmentId,
    };
    if (isEditMode && existingPrescriptionId) {
      prescription.id = existingPrescriptionId;
    }

    const { success, message } = isEditMode && existingPrescriptionId
      ? await updatePrescription(prescription, token)
      : await savePrescription(prescription, token);

    if (success) {
      alert(isEditMode ? "Prescription updated successfully." : "Prescription saved successfully.");
      const t = localStorage.getItem("token");
      if (t) window.location.href = `/doctorDashboard/${t}`;
      else if (typeof selectRole === "function") selectRole("doctor");
      else window.location.href = "/";
    } else {
      showFormError(message || "Failed to save prescription.");
    }
  });

  medicinesInput?.addEventListener("input", () => {
    showFieldError("medicinesError", "");
    showFormError("");
  });
  dosageInput?.addEventListener("input", () => {
    showFieldError("dosageError", "");
    showFormError("");
  });
});
