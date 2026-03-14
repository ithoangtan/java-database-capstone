// loggedPatient.js 
import { getDoctors } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { filterDoctors } from './services/doctorServices.js';
import { bookAppointment } from './services/appointmentRecordService.js';


document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});

function loadDoctorCards() {
  getDoctors()
    .then(doctors => {
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
      });
    })
    .catch(error => {
      console.error("Failed to load doctors:", error);
    });
}

export function showBookingOverlay(e, doctor, patient) {
  const button = e.target;
  const rect = button.getBoundingClientRect();
  console.log(patient.name)
  console.log(patient)
  const ripple = document.createElement("div");
  ripple.classList.add("ripple-overlay");
  ripple.style.left = `${e.clientX}px`;
  ripple.style.top = `${e.clientY}px`;
  document.body.appendChild(ripple);

  setTimeout(() => ripple.classList.add("active"), 50);

  const today = new Date().toISOString().slice(0, 10);

  const modalApp = document.createElement("div");
  modalApp.classList.add("modalApp");

  modalApp.innerHTML = `
    <div class="modalApp-header">
      <h2>Book Appointment</h2>
      <button type="button" class="modalApp-close" aria-label="Close">×</button>
    </div>
    <input class="input-field" type="text" value="${escapeHtml(patient.name)}" disabled />
    <input class="input-field" type="text" value="${escapeHtml(doctor.name)}" disabled />
    <input class="input-field" type="text" value="${escapeHtml(doctor.specialty || '')}" disabled/>
    <input class="input-field" type="email" value="${escapeHtml(doctor.email || '')}" disabled/>
    <input class="input-field" type="date" id="appointment-date" min="${today}" required />
    <select class="input-field" id="appointment-time" required>
      <option value="">Select time</option>
      ${(doctor.availableTimes || []).map(t => `<option value="${escapeHtml(t)}">${escapeHtml(t)}</option>`).join('')}
    </select>
    <p id="booking-error" class="booking-error" role="alert" aria-live="polite"></p>
    <button type="button" class="confirm-booking">Confirm Booking</button>
  `;

  document.body.appendChild(modalApp);

  const dateInput = modalApp.querySelector("#appointment-date");
  dateInput.value = today;

  setTimeout(() => modalApp.classList.add("active"), 600);

  function closeModal() {
    modalApp.classList.remove("active");
    setTimeout(() => {
      modalApp.remove();
      ripple.remove();
    }, 300);
  }

  modalApp.querySelector(".modalApp-close").addEventListener("click", closeModal);

  modalApp.querySelector(".confirm-booking").addEventListener("click", async () => {
    const errorEl = modalApp.querySelector("#booking-error");
    errorEl.textContent = "";

    const date = (modalApp.querySelector("#appointment-date").value || "").trim();
    const time = (modalApp.querySelector("#appointment-time").value || "").trim();

    if (!date) {
      errorEl.textContent = "Vui lòng chọn ngày hẹn.";
      return;
    }
    if (!time) {
      errorEl.textContent = "Vui lòng chọn khung giờ.";
      return;
    }

    const selectedDate = new Date(date + "T00:00:00");
    const todayStart = new Date();
    todayStart.setHours(0, 0, 0, 0);
    if (selectedDate < todayStart) {
      errorEl.textContent = "Không thể đặt lịch trong quá khứ. Vui lòng chọn ngày hôm nay hoặc sau.";
      return;
    }

    const startTime = time.split("-")[0].trim();
    if (!startTime) {
      errorEl.textContent = "Vui lòng chọn khung giờ hợp lệ.";
      return;
    }

    const doctorId = doctor.id != null ? Number(doctor.id) : null;
    const patientId = patient.id != null ? Number(patient.id) : null;
    if (doctorId == null || isNaN(doctorId) || patientId == null || isNaN(patientId)) {
      errorEl.textContent = "Dữ liệu bác sĩ hoặc bệnh nhân không hợp lệ. Vui lòng tải lại trang.";
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      errorEl.textContent = "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.";
      return;
    }

    const appointmentTimeStr = startTime.length <= 5 ? `${date}T${startTime}:00` : `${date}T${startTime}`;
    const appointment = {
      doctor: { id: doctorId },
      patient: { id: patientId },
      appointmentTime: appointmentTimeStr,
      status: 0
    };

    const { success, message } = await bookAppointment(appointment, token);

    if (success) {
      alert("Đặt lịch thành công.");
      ripple.remove();
      modalApp.remove();
    } else {
      errorEl.textContent = message || "Đặt lịch thất bại. Vui lòng thử lại.";
    }
  });
}

function escapeHtml(str) {
  if (str == null) return "";
  const s = String(str);
  const div = document.createElement("div");
  div.textContent = s;
  return div.innerHTML;
}



// Filter Input
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);



function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar").value.trim();
  const filterTime = document.getElementById("filterTime").value;
  const filterSpecialty = document.getElementById("filterSpecialty").value;


  const name = searchBar.length > 0 ? searchBar : null;
  const time = filterTime.length > 0 ? filterTime : null;
  const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

  filterDoctors(name, time, specialty)
    .then(response => {
      const doctors = response.doctors;
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      if (doctors.length > 0) {
        console.log(doctors);
        doctors.forEach(doctor => {
          const card = createDoctorCard(doctor);
          contentDiv.appendChild(card);
        });
      } else {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        console.log("Nothing");
      }
    })
    .catch(error => {
      console.error("Failed to filter doctors:", error);
      alert("❌ An error occurred while filtering doctors.");
    });
}

export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });

}
