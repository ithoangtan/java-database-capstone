// patientRows.js
export function createPatientRow(patient, appointmentId, doctorId, appointmentDateOrDateTime = "") {
  const tr = document.createElement("tr");
  const dateDisplay = appointmentDateOrDateTime ? String(appointmentDateOrDateTime) : "";
  tr.innerHTML = `
      <td class="patient-id">${patient.id ?? ""}</td>
      <td>${patient.name ?? ""}</td>
      <td>${dateDisplay}</td>
      <td>${patient.phone ?? ""}</td>
      <td>${patient.email ?? ""}</td>
      <td><img src="/assets/images/addPrescriptionIcon/addPrescription.png" alt="addPrescriptionIcon" class="prescription-btn" data-id="${patient.id}"></img></td>
    `;

  // Attach event listeners
  tr.querySelector(".patient-id").addEventListener("click", () => {
    window.location.href = `/pages/patientRecord.html?id=${patient.id}&doctorId=${doctorId}`;
  });

  tr.querySelector(".prescription-btn").addEventListener("click", () => {
    const name = (patient.name ?? "").toString();
    window.location.href = `/pages/addPrescription.html?appointmentId=${appointmentId}&patientName=${encodeURIComponent(name)}`;
  });

  return tr;
}
