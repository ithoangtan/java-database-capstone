# Lab: Creating Frontend Pages

**Estimated time:** 90 minutes

Welcome to this lab on **Creating Frontend Pages**.

## Learning objectives

After completing this lab, you will be able to:

- Structure HTML, CSS and JS files for a large frontend project
- Design responsive and interactive user interfaces
- Implement reusable UI components (for example, Header, Footer, Cards)
- Trigger modals and manage page-level state using vanilla JavaScript

## Prerequisites

- Ensure you have cloned your GitHub repo `java-database-capstone`
- Ensure you have generated a **GitHub Personal Access Token** to push your work back into your GitHub repository for peer review
- This lab builds on previous labs, so ensure you have completed the previous labs before starting this lab

## Introduction

This lab is designed to help you lay the foundation of a fully functional and scalable frontend for a Clinic management system. You'll structure your HTML, CSS, and JavaScript files, and build modular components that are clean, reusable, and maintainable.

To perform the exercises given in this lab, you'll use the Cloud IDE lab environment. This is where all of your development will take place.

## About Cloud IDE lab

It is important to understand that the lab environment is **ephemeral**. It only lives for a short while and then it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time later.

Also, this environment is shared. It is recommended not to store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

> **Note:** If you haven't generated a GitHub Personal Access Token, you should do so now. You will need it to push code back to your repository. It should have `repo` and `write` permissions, and set to expire in **60 days**. When Git prompts you for a password in the Cloud IDE environment, use your Personal Access Token instead. The environment may be recreated at any time so you may find that you have to perform the **Initialize Development Environment** step each time the environment is created.

## Key terms

- **Component:** A reusable piece of UI (header, footer, card)
- **Page-Specific JS:** JavaScript files that control behavior specific to individual pages
- **Modularity:** Organizing code into self-contained units
- **Separation of Concerns:** Dividing a program into distinct sections with minimal overlap
- **Local Storage:** Storage space of the browser

## Project Structure

Here is the directory structure shown for the frontend:

```text
app/src/main/resources
├── application.properties
├── static
│   ├── index.html
│   ├── assets
│   │   ├── css
│   │   │   ├── addPrescription.css
│   │   │   ├── adminDashboard.css
│   │   │   ├── doctorDashboard.css
│   │   │   ├── index.css
│   │   │   ├── patientDashboard.css
│   │   │   ├── style.css
│   │   │   └── updateAppointment.css
│   │   └── images
│   │       ├── addPrescriptionIcon/
│   │       │   └── addPrescription.png
│   │       ├── edit/
│   │       │   └── edit.png
│   │       ├── defineRole/
│   │       │   └── index.png
│   │       └── logo/
│   │           └── logo.png
│   ├── js
│   │   ├── components
│   │   │   ├── appointmentRow.js
│   │   │   ├── doctorCard.js
│   │   │   ├── footer.js
│   │   │   ├── header.js
│   │   │   ├── modals.js
│   │   │   ├── patientRecordRow.js
│   │   │   └── patientRows.js
│   │   ├── config
│   │   │   └── index.js
│   │   ├── services
│   │   │   ├── appointmentRecordService.js
│   │   │   ├── doctorServices.js
│   │   │   ├── index.js
│   │   │   ├── patientServices.js
│   │   │   └── prescriptionServices.js
│   │   ├── addPrescription.js
│   │   ├── adminDashboard.js
│   │   ├── appointmentRecord.js
│   │   ├── doctorDashboard.js
│   │   ├── loggedPatient.js
│   │   ├── patientAppointment.js
│   │   ├── patientDashboard.js
│   │   ├── patientRecordServices.js
│   │   ├── render.js
│   │   ├── updateAppointment.js
│   │   └── util.js
│   ├── pages
│   │   ├── addPrescription.html
│   │   ├── loggedPatientDashboard.html
│   │   ├── patientAppointments.html
│   │   ├── patientDashboard.html
│   │   ├── patientRecord.html
│   │   └── updateAppointment.html
│   └── templates
│       ├── admin
│       │   └── adminDashboard.html
│       └── doctor
│           └── doctorDashboard.html
```

### Folder purpose

- `assets/css`: CSS files for styling individual pages and shared components like buttons, headers, and modals.
- `assets/images`: Icons, logos, and illustrations used throughout the UI. Organized into folders such as `logo`, `edit`, and `index`.
- `pages`: Standalone HTML files for different user roles and screens, such as `patientDashboard.html` or `addPrescription.html`. These are dynamically updated by JavaScript.
- `js`: JavaScript logic, broken into:
  - `services/`: API communication logic for doctors, patients, prescriptions, and appointments. Handles fetch and CRUD operations.
  - `components/`: Reusable UI components like `header.js`, `doctorCard.js`, and `modals.js`.
  - `config/`: Stores shared constants and settings such as API base URLs or environment values.
  - Page-specific files like `adminDashboard.js`, `updateAppointment.js`, or `patientDashboard.js` for handling user interaction, rendering, and calling services.
  - `util.js`: Helper functions shared across pages, such as token handling, date formatting, or alerts.
- `templates`: Thymeleaf-based HTML templates rendered by the backend for authenticated users. Split into folders like `admin/` and `doctor/` to serve role-specific views.
- `application.properties`: Central configuration file for Spring Boot that sets up server ports, database access, and environment properties.

## HTML Pages

## 1. index.html

This is the initial landing page where users can choose their role in the Clinic system: **Admin**, **Doctor**, or **Patient**. This page sets the stage for a personalized user experience by allowing the user to select their identity, which will be used to tailor the dashboard and access permissions.

You will include buttons for each role and store the selected role in the browser's `localStorage` so it can be accessed on subsequent pages. Later, you'll also trigger login modals based on the selected role.

### HTML code

1. Open `index.html` at `app/src/main/resources/static/index.html`
2. Add the basic HTML structure:
   - `<!DOCTYPE html>` and `<html lang="en">`
   - In `<head>`, include:
     - `<meta charset="UTF-8">`
     - Favicon
     - CSS links such as `index.css` and `style.css`
   - Add JS links for utility and rendering with `defer`:
     - `../js/util.js`
     - `../js/render.js`
   - Add JS links for components such as `header.js` and `footer.js`
3. In `<body>`, add content inside a `<main>` tag:
   - Wrap the content in a `container` and `wrapper`
   - Add placeholders for Header (`<div id="header"></div>`) and Footer (`<div id="footer"></div>`)
   - In `<main class="main-content">`, create:
     - An `h2` heading: `"Select Your Role"`
     - Buttons for Admin, Doctor, Patient
     - Use `onclick` for dynamic role selection
     - Assign unique ids for DOM access in JavaScript
4. Add a modal hidden by default:
   - Create `<div id="modal" class="modal">`
   - Close button `id="closeModal"`
   - Dynamic content area `id="modal-body"`
5. Include the JavaScript file near the end of the page:
   - Use a module script for role selection and modal behavior:
     ```html
     <script type="module" src="../js/services/index.js" defer></script>
     ```

### CSS code

1. Open `index.css`
2. Add global styling:
   - `* { margin: 0; padding: 0; box-sizing: border-box; }`
   - Style `body` and `html` for full height and font usage
3. Style the main wrapper:
   - `.wrapper` as a flex column so the main content can grow
4. Style heading and buttons:
   - Large bold heading
   - Centered buttons with background color, padding, rounded corners, and hover effects
5. Style the main content area:
   - `.main-content` with a background image and centered content
6. Style modal and button interactions:
   - Include close button `.close`, hover effects, and modal positioning
7. Style footer and header:
   - `.header` for logo and navigation layout
8. Add interactive styles:
   - Buttons, inputs, and focus/hover effects
   - Reuse classes like `.dashboard-btn`, `.input-field`, `.select-dropdown`, `.checkbox-group`

## 2. adminDashboard.html (Thymeleaf Code)

In this section, you'll build the main dashboard for Admin users. This page serves as the control panel for managing doctors, allowing admins to view, add, delete, and filter doctor profiles.

The page should include a search bar, dropdown filters (by specialty or time), and a dynamic list of doctor cards. You'll also add a modal popup for adding new doctors, which will be triggered by a button click.

This layout will eventually be connected to your backend and populated using JavaScript services.

### Tips

- The admin is validated first using token and `userRole`
- Include modal forms for adding new doctors

### HTML code

1. Open `adminDashboard.html` located at `app/src/main/resources/templates/admin/adminDashboard.html`
2. Add basic HTML structure:
   - `<!DOCTYPE html>` and `<html lang="en">`
   - In `<head>`, include:
     - `<meta charset="UTF-8">`
     - A `<title>` for the page (`Admin Dashboard`)
     - CSS links:
       ```html
       <link rel="stylesheet" th:href="@{/assets/css/adminDashboard.css}" />
       <link rel="stylesheet" th:href="@{/assets/css/style.css}" />
       ```
     - JavaScript files with `defer`:
       - Utility and rendering logic:
         ```html
         <script th:src="@{/js/render.js}" defer></script>
         <script th:src="@{/js/util.js}" defer></script>
         ```
       - Layout components:
         ```html
         <script th:src="@{/js/components/header.js}" defer></script>
         <script th:src="@{/js/components/footer.js}" defer></script>
         ```
3. Create body layout:
   - Wrap everything in `container` then `wrapper`
   - Insert `<div id="header"></div>` for the dynamic header
   - Create `<main class="main-content">` containing:
     - Search bar:
       ```html
       <input type="text" id="searchBar" placeholder="search by doctor name" />
       ```
     - Filter section with two `<select>` dropdowns for:
       - Sorting by time
       - Filtering by specialty
     - A card container:
       ```html
       <div id="content"></div>
       ```
   - Add `<div id="footer"></div>` for the dynamic footer
4. Add modal for adding doctor:
   ```html
   <div id="modal" class="modal">
     <span id="closeModal" class="close">&times;</span>
     <div id="modal-body"></div>
   </div>
   ```
5. Include JavaScript files before `</body>`:
   ```html
   <script type="module" src="../js/services/adminDashboard.js" defer></script>
   <script type="module" src="../js/components/doctorCard.js" defer></script>
   ```

### CSS code

1. Open `adminDashboard.css`
2. Add universal styling:
   ```css
   * { margin: 0; padding: 0; box-sizing: border-box; }
   html, body { height: 100%; font-family: Arial, sans-serif; }
   ```
3. Layout and wrappers:
   - Use `.wrapper` and `.main-content` for vertical flex layout, background image settings, alignment, and padding
4. Interactive elements:
   - Style the search bar, filter dropdowns, and Add Doctor button
   - Reuse classes like `.button`, `.adminBtn`, plus hover states
5. Modal styling:
   - Center the modal, hide it by default, add smooth transitions
   - Style modal form inputs with padding and focus effects
6. Doctor cards and content area:
   - Use flexbox for doctor card display
   - Ensure responsive behavior on smaller devices

## 3. doctorDashboard.html (Thymeleaf Code)

Next, you'll create the dashboard for Doctor users. This page is focused on patient management and appointment tracking.

You'll build a clean interface that includes a table to list patient records, filters for dates or appointment status, and buttons for adding prescriptions. This layout should be easy to scan and interact with, enabling doctors to manage their workload efficiently.

### HTML code

1. Open `doctorDashboard.html` located at `app/src/main/resources/templates/doctor/doctorDashboard.html`
2. HTML structure:
   - Start with `<!DOCTYPE html>` and `<html lang="en">`
   - In `<head>`, include:
     - Standard meta tags and page title `"DoctorDashboard"`
     - Favicon link
     - Stylesheets:
       - `adminDashboard.css`
       - `doctorDashboard.css`
       - `style.css`
     - Scripts:
       - Utility files: `render.js`, `util.js`
       - Components: `header.js`, `footer.js`, `patientRows.js`
3. Body layout:
   - Use `<div class="container">` and `<div class="wrapper">`
   - Header placeholder via `<div id="header"></div>`
   - Main dashboard includes:
     - Search bar:
       ```html
       <input type="text" id="searchBar" />
       ```
     - Filter section:
       - "Today's Appointments" button
       - Date input for selecting other dates
     - Patient records table:
       - Use `<table id="patientTable">`
       - Columns: Patient ID, Name, Phone, Email, Prescription
       - Table body populated dynamically
   - Footer via `<div id="footer"></div>`

### CSS code

1. Open `doctorDashboard.css`
2. Reset and base styles:
   - Apply global reset: `* { margin: 0; padding: 0; box-sizing: border-box; }`
3. Layout:
   - Use `.wrapper` as a vertical flex container
   - Add spacing and alignment for search, filters, and table
4. Search and filter styling:
   - Inputs and buttons padded, rounded, and spaced
   - Button hover colors are highlighted in the lab as examples such as `#015c5d` and `#017d7e`
5. Table styling:
   - `table`: full width, border-collapse
   - `thead`: dark semi-transparent background
   - `tbody tr`: alternating row colors and hover highlight
6. Extras:
   - `.prescription-btn`: scale/brightness hover effect
   - `.noPatientRecord`: italic gray text when no records exist
7. Responsive design:
   - Use flexible widths and media queries for smaller screens

## 4. patientDashboard.html (Static HTML Page)

Next, you'll create the Patient Dashboard, a central page where patients can browse and filter doctor profiles based on their selected filters or search input.

You'll also integrate modals to allow appointment booking and provide a rich UI experience using reusable components and state management in JavaScript.

### HTML code

1. Open `patientDashboard.html` located at `app/src/main/resources/static/pages/patientDashboard.html`
2. Set up the head section:
   - Include metadata and page title
   - Favicon for branding
   - Link CSS files such as:
     - `adminDashboard.css`
     - `style.css`
     - `patientDashboard.css`
   - Add utility and component scripts with `defer`:
     ```html
     <script src="../js/render.js" defer></script>
     <script src="../js/util.js" defer></script>
     <script src="../js/components/header.js" defer></script>
     <script src="../js/components/footer.js" defer></script>
     <script type="module" src="../js/components/modals.js"></script>
     ```
3. Create the body structure:
   - Wrap all content inside `container` and `wrapper`
   - Insert dynamic `<div id="header"></div>`
   - Inside `<main class="main-content">`, add:
     - Search bar:
       ```html
       <input type="text" id="searchBar" class="searchBar" placeholder="Search bar for custom output" />
       ```
     - Filter section:
       ```html
       <div class="filter-wrapper">
         <select class="filter-select" id="filterTime"></select>
         <select class="filter-select" id="filterSpecialty"></select>
       </div>
       ```
     - Content container for doctor cards:
       ```html
       <div id="content"></div>
       ```
   - Include `<div id="footer"></div>` for the common footer
   - Add reusable modal markup:
     ```html
     <div id="modal" class="modal">
       <div class="modal-content">
         <span class="close" id="closeModal">&times;</span>
         <div id="modal-body"></div>
       </div>
     </div>
     ```
4. Load page logic:
   - Add a JavaScript module to handle patient-specific rendering:
     ```html
     <script type="module" src="../js/patientDashboard.js" defer></script>
     ```
   - Also call `renderContent()` on body load:
     ```html
     <body onload="renderContent()">
     ```

### CSS code

1. Open `patientDashboard.css`
2. Style doctor card actions:
   - Add a `card-actions` class for the section where action buttons like "Book Now" appear
   - Use a dark background with centered text
3. Ripple overlay for visual feedback:
   - Create a circular animation that expands outward to simulate a click ripple when booking is confirmed
   - Use fixed positioning so the effect appears anywhere on screen
   - Use CSS transitions and transform: `scale(...)`
4. Bottom pop-up modal for booking:
   - The `.modalApp` class defines a container that initially slides in from the bottom of the screen
   - Use fixed positioning and horizontal centering
   - Apply a white background, padding, and rounded top corners
   - Add a smooth slider-in animation using an `.active` class
5. Form fields inside modal:
   - Inputs and dropdowns should:
     - Be centered on the page
     - Have uniform width (about 90% of container)
     - Include padding
     - Be spaced vertically
6. Booking confirmation button:
   - Use a dark-colored background matching the theme
   - Add padding and rounded corners
   - On hover, increase brightness using `filter: brightness(1.2)`

## Components

## 1. Header.js

This file defines a reusable header component that appears at the top of every page. It dynamically changes based on the user's role (`admin`, `doctor`, `patient`) and login state. It improves code reusability and reduces duplication across multiple HTML files.

You will use JavaScript to insert navigation links, role selectors, and logout buttons depending on the context of the current page.

### Task

Build a `renderHeader()` function that:

- Checks the current page. The role-based header should not be shown on the homepage.
- If the current pathname ends with `/`, clear session info:
  ```js
  if (window.location.pathname.endsWith("/")) {
      localStorage.removeItem("userRole");
      localStorage.removeItem("token");
  }
  ```
- Reads the user's role and token from `localStorage`:
  ```js
  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");
  ```
- Checks invalid states and redirects when needed:
  ```js
  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
      localStorage.removeItem("userRole");
      alert("Session expired or invalid login. Please log in again.");
      window.location.href = "/";
      return;
  }
  ```
- Injects the appropriate header HTML depending on role:
  - `admin` -> show **Add Doctor** button and **Logout**
  - `doctor` -> show **Home** and **Logout**
  - `patient` -> show **Login** and **Sign Up**
  - `loggedPatient` -> show **Home**, **Appointments**, and **Logout**
- Sets the generated content into the page and attaches event listeners afterward:
  ```text
  headerDiv.innerHTML = headerContent;
  attachHeaderButtonListeners();
  ```
- Adds logout functionality:
  - `logout()`:
    - remove `token` and `userRole`
    - redirect to homepage with `window.location.href = "/"`
  - `logoutPatient()`:
    - remove `token`
    - set role back to `"patient"`
    - redirect to patient dashboard

## 2. Footer.js

The footer component remains consistent across all pages and includes branding, navigation links, or any additional information you want users to see at the bottom of the screen. Unlike the header, the footer is static and doesn't change based on user role.

It improves modularity and makes layout maintenance easier by separating common footer content into a single file.

### Task

- Create the function:
  ```js
  function renderFooter() {
  }
  ```
- Call this on every page that needs a footer
- Access the footer container:
  ```js
  const footer = document.getElementById("footer");
  ```
- Inject HTML content:
  ```js
  footer.innerHTML = `...`;
  ```
- The footer template includes branding, navigation, and legal information.
- The page notes that sections can be divided into three columns such as:
  - Company (About, Careers, Press)
  - Support (Account, Help Center, Contact)
  - Legals (Terms, Privacy Policy, Licensing)
- Call the function at the bottom of the file:
  ```js
  renderFooter();
  ```

## 3. doctorCard.js

This component creates a dynamic, reusable card for displaying doctor information on the Admin and Patient dashboards. Each card can show a doctor's name, specialty, availability, and contact info, along with action buttons like "Delete" or "Book Appointment."

It improves separation of concerns by encapsulating UI rendering and interactivity in a single module.

### Task

1. Define the function:
   ```js
   export function createDoctorCard(doctor)
   ```
2. Accept one argument: a `doctor` object containing data such as `name`, `specialty`, and more.
3. Create the main card container:
   ```js
   const card = document.createElement("div");
   card.classList.add("doctor-card");
   ```
4. Read the user's role:
   ```js
   const role = localStorage.getItem("userRole");
   ```
5. Create a nested info container:
   ```js
   const infoDiv = document.createElement("div");
   infoDiv.classList.add("doctor-info");
   ```
6. Add child elements for:
   - doctor name
   - specialization
   - email
   - availability (join arrays with `join(", ")` if needed)
7. Append the info nodes:
   ```js
   infoDiv.appendChild(name);
   infoDiv.appendChild(specialization);
   infoDiv.appendChild(email);
   infoDiv.appendChild(availability);
   ```
8. Create a button area:
   ```js
   const actionsDiv = document.createElement("div");
   actionsDiv.classList.add("card-actions");
   ```

### Role-based actions

#### Admin

Create a delete button:

```js
if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
}
```

Attach a click listener that conceptually does:

```js
removeBtn.addEventListener("click", async () => {
    // 1. Confirm deletion
    // 2. Get token from localStorage
    // 3. Call API to delete
    // 4. On success: remove the card from the DOM
});
```

#### Patient (not logged in)

Show a Book Now button, but alert the user that login is required:

```js
else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", () => {
        alert("Patient needs to login first.");
    });
}
```

#### Logged-in patient

Allow real booking:

```js
else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.addEventListener("click", async (e) => {
        const token = localStorage.getItem("token");
        const patientData = await getPatientData(token);
        showBookingOverlay(e, doctor, patientData);
    });
}
```

### Final assembly

Append parts into the card and return it:

```js
card.appendChild(infoDiv);
card.appendChild(actionsDiv);

return card;
```

### Notes

This component uses helper functions imported from service files that will be implemented in the next lab:

- `deleteDoctor()` from `../js/services/doctorServices.js`
- `getPatientData()` from `../js/services/patientServices.js`

These service modules will handle API interactions and are part of the modular architecture designed for better maintainability and code reuse.

## Conclusion and Next Steps

### Summary

In this lab, you have:

- Set up your frontend project structure with HTML, CSS, and JS files
- Created and styled pages for different user roles
- Implemented reusable components like headers, footers, and doctor cards
- Kept code modular and followed clean architecture principles

### Next steps

- In the next lab, you'll focus on implementing and organizing JavaScript service files
- This includes proper API calling, error handling, and modularizing page-specific functionality by importing and using these services
- Make sure all your work is committed and pushed to GitHub

## Save your work

Before you finish this lab, make sure to **add, commit, and push** your updated code to your GitHub repository. You will be asked to provide your public GitHub repo URL for graded evaluation at the end of the capstone.

Follow these steps to push your changes:

1. Stage your changes:
   ```bash
   git add .
   ```
2. Commit your changes with a meaningful message:
   ```bash
   git commit -m "Completed front end pages for Clinic Management System"
   ```
3. Push your changes using your GitHub Personal Access Token:
   ```bash
   git push https://<your_github_username>:<your_personal_access_token>@github.com/<your_github_username>/java-database-capstone.git
   ```

Replace:

- `<your_github_username>` with your GitHub username
- `<your_personal_access_token>` with the token you generated at the start of the lab

### Example

If your GitHub username is `johnsmith` and your token is `ghp_ABC123xyzTOKEN`, your push command would look like:

```bash
git push https://johnsmith:ghp_ABC123xyzTOKEN@github.com/johnsmith/java-database-capstone.git
```

### Remember

- Always push your work after completing each lab
- Save your GitHub URL - you will need to submit it for your final project evaluation

## Author(s)

Skills Network Team  
Upkar Lidder



## Check list
Did you review the directory structures? 
Did you create the 'index.html' file in 'app/src/main/resources/static/index.html'?
Did you build the main dashboard for admin users using  'adminDashboard.html'?
Did you create the dashboard for doctor users, using 'doctorDashboard.html'?
Did you use the 'index.css' stylesheet controls?
Did you style the admin dashboard page using 'adminDashboard.css'?
Did you enhance the doctor dashboard using 'doctorDashboard.css'?
Did you create a dynamic header that changes based on 'userRole' and 'token' stored in localStorage?
Did you build a static footer with clear branding or informational sections?
Did you use the 'doctorCard.js' component to create doctor cards based on API data or user input?



