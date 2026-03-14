// modals.js
import {
  getRandomAdminAccount,
  getRandomDoctorAccount,
  getRandomPatientAccount,
} from "../config/demoAccounts.js";

export function openModal(type) {
  let modalContent = '';
  if (type === 'addDoctor') {
    modalContent = `
         <h2>Add Doctor</h2>
         <div id="addDoctorErrorMessage" class="error-message" role="alert" aria-live="polite"></div>
         <input type="text" id="doctorName" placeholder="Doctor Name" class="input-field">
         <select id="specialization" class="input-field select-dropdown">
             <option value="">Specialization</option>
                        <option value="Cardiologist">Cardiologist</option>
                        <option value="Dermatologist">Dermatologist</option>
                        <option value="Neurologist">Neurologist</option>
                        <option value="Pediatrician">Pediatrician</option>
                        <option value="Orthopedist">Orthopedist</option>
                        <option value="Gynecologist">Gynecologist</option>
                        <option value="Psychiatrist">Psychiatrist</option>
                        <option value="Dentist">Dentist</option>
                        <option value="Ophthalmologist">Ophthalmologist</option>
                        <option value="Ent">ENT Specialist</option>
                        <option value="Urologist">Urologist</option>
                        <option value="Oncologist">Oncologist</option>
                        <option value="Gastroenterologist">Gastroenterologist</option>
                        <option value="General">General Physician</option>

        </select>
        <input type="email" id="doctorEmail" placeholder="Email" class="input-field">
        <input type="password" id="doctorPassword" placeholder="Password" class="input-field">
        <input type="text" id="doctorPhone" placeholder="Mobile No." class="input-field">
        <div class="availability-container">
        <label class="availabilityLabel">Select Availability:</label>
          <div class="checkbox-group">
              <label><input type="checkbox" name="availability" value="09:00-10:00"> 9:00 AM - 10:00 AM</label>
              <label><input type="checkbox" name="availability" value="10:00-11:00"> 10:00 AM - 11:00 AM</label>
              <label><input type="checkbox" name="availability" value="11:00-12:00"> 11:00 AM - 12:00 PM</label>
              <label><input type="checkbox" name="availability" value="12:00-13:00"> 12:00 PM - 1:00 PM</label>
          </div>
        </div>
        <button class="dashboard-btn" id="saveDoctorBtn">Save</button>
      `;
  } else if (type === 'patientLogin') {
    modalContent = `
        <h2>Patient Login</h2>
        <input type="text" id="email" placeholder="Email" class="input-field">
        <input type="password" id="password" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="loginBtn">Login</button>
      `;
  }
  else if (type === "patientSignup") {
    modalContent = `
      <h2>Patient Signup</h2>
      <input type="text" id="name" placeholder="Name" class="input-field">
      <input type="email" id="email" placeholder="Email" class="input-field">
      <input type="password" id="password" placeholder="Password" class="input-field">
      <input type="text" id="phone" placeholder="Phone" class="input-field">
      <input type="text" id="address" placeholder="Address" class="input-field">
      <button class="dashboard-btn" id="signupBtn">Signup</button>
    `;

  } else if (type === 'adminLogin') {
    modalContent = `
        <h2>Admin Login</h2>
        <label for="username" class="modal-label">Username</label>
        <input type="text" id="username" name="username" placeholder="Username" class="input-field" required>
        <label for="password" class="modal-label">Password</label>
        <input type="password" id="password" name="password" placeholder="Password" class="input-field" required>
        <button class="dashboard-btn" id="adminLoginBtn">Login</button>
      `;
  } else if (type === 'doctorLogin') {
    modalContent = `
        <h2>Doctor Login</h2>
        <label for="email" class="modal-label">Email</label>
        <input type="email" id="email" placeholder="Email" class="input-field" required>
        <label for="password" class="modal-label">Password</label>
        <input type="password" id="password" placeholder="Password" class="input-field" required>
        <button class="dashboard-btn" id="doctorLoginBtn">Login</button>
      `;
  }

  document.getElementById('modal-body').innerHTML = modalContent;
  document.getElementById('modal').style.display = 'block';

  // Mỗi lần mở form login: điền random 1 tài khoản từ mảng (admin / doctor / patient)
  if (type === "adminLogin") {
    const acc = getRandomAdminAccount();
    const u = document.getElementById("username");
    const p = document.getElementById("password");
    if (u) u.value = acc.username;
    if (p) p.value = acc.password;
  } else if (type === "doctorLogin") {
    const acc = getRandomDoctorAccount();
    const e = document.getElementById("email");
    const p = document.getElementById("password");
    if (e) e.value = acc.email;
    if (p) p.value = acc.password;
  } else if (type === "patientLogin") {
    const acc = getRandomPatientAccount();
    const e = document.getElementById("email");
    const p = document.getElementById("password");
    if (e) e.value = acc.email;
    if (p) p.value = acc.password;
  }

  document.getElementById('closeModal').onclick = () => {
    document.getElementById('modal').style.display = 'none';
  };

  if (type === "patientSignup") {
    document.getElementById("signupBtn").addEventListener("click", signupPatient);
  }

  if (type === "patientLogin") {
    document.getElementById("loginBtn").addEventListener("click", loginPatient);
  }

  if (type === 'addDoctor') {
    document.getElementById('saveDoctorBtn').addEventListener('click', adminAddDoctor);
  }

  if (type === 'adminLogin') {
    document.getElementById('adminLoginBtn').addEventListener('click', adminLoginHandler);
  }

  if (type === 'doctorLogin') {
    document.getElementById('doctorLoginBtn').addEventListener('click', doctorLoginHandler);
  }
}

if (typeof window !== 'undefined') {
  window.openModal = openModal;
}
