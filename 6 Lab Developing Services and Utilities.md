# Lab: Developing Services and Utilities

**Estimated time:** 90 minutes

Welcome to this lab on **Developing Services and Utilities**.

## Learning Objectives

After completing this lab, you will be able to:

- Create service modules to manage API interactions
- Implement role-based login logic
- Load and filter data dynamically using JavaScript
- Use modals and document object model (DOM) manipulation for dynamic interactions

## Prerequisites

- Ensure you have finished all the labs so far.

## Overview

This lab demonstrates how to build the user role and admin and doctor dashboard pages.

To perform the exercises given in this lab, you'll use Cloud IDE lab environment. This is where all of your development will take place.

## About Cloud IDE lab

It is important to understand that the lab environment is **ephemeral**. It only lives for a short while and then it will be destroyed. This makes it imperative that you push all changes made to your own GitHub repository so that it can be recreated in a new lab environment any time later.

Also, this environment is shared. It is recommended not to store any personal information, usernames, passwords, or access tokens in this environment for any purpose.

> **Note:** If you haven't generated a GitHub Personal Access Token, you should do so now. You will need it to push code back to your repository. It should have `repo` and `write` permissions, and set to expire in **60 days**. When Git prompts you for a password in the Cloud IDE environment, use your Personal Access Token instead. The environment may be recreated at any time so you may find that you have to perform the **Initialize Development Environment** step each time the environment is created. Create a repository from the GitHub template provided for this lab in the next step.

## Services (API/Logic Handlers)

Interact with backend APIs and improve code organization.

## index.js - Role-Based Login Handling

1. Open the `index.js` file: `app/src/main/resources/static/js/services/index.js`

2. Import required modules:
   - The `openModal` function from the modal component file `../components/modals.js`
   - The `API_BASE_URL` constant from your configuration file `../config/config.js`

   Then, define two constants:
   - One for the Admin login endpoint (append `/admin`)
   - One for the Doctor login endpoint (append `/doctor/login`)

```js
const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login';
```

3. Setup button event listeners:
   - Use `window.onload` to ensure the script runs after the page is fully loaded.
   - Select the Admin and Doctor login buttons by their `id` attributes.
   - Attach `click` event listeners to these buttons.

```js
window.onload = function () {
  const adminBtn = document.getElementById("adminLogin");
  if (adminBtn) {
    adminBtn.addEventListener("click", () => {
      openModal('adminLogin');
    });
  }
};
```

When clicked:
- The Admin button should open the Admin login modal.
- The Doctor button should open the Doctor login modal.

4. Implement Admin login handler:
   - Define an asynchronous function named `adminLoginHandler` and make it accessible globally.
   - Inside this function:
     1. Read the values entered for `username` and `password`.
     2. Create an `admin` object containing these credentials.

```js
const admin = { username, password };
```

     3. Use the `fetch()` function to make a POST request to the Admin login API.

Example:

```js
fetch(YOUR_API_URL, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data)
});
```

In the request:
- Set the method to POST
- Set headers to indicate JSON content
- Send the `admin` object as a JSON string in the body

After receiving the response:
- If successful, extract the response JSON
- Store the received token in `localStorage`
- Call a helper function `selectRole()` with `"admin"` to save the selected role
- If it fails, display an alert saying `"Invalid credentials!"`

Wrap the request in a `try-catch` block to catch and alert any unexpected errors.

5. Implement Doctor login handler:
   - Similarly, define an asynchronous function named `doctorLoginHandler` and make it globally accessible.
   - Inside this function:
     1. Read the `email` and `password` values entered by the user.
     2. Create a `doctor` object with these values.
     3. Send a POST request to the Doctor login endpoint using `fetch()`.
     4. Follow the same process as the Admin handler:
        - On success: store the received token in `localStorage`, call `selectRole()` with `"doctor"` to save the selected role
        - On failure: alert the user about invalid credentials
        - Handle unexpected issues using `try-catch`

This script enables:
- Role-based login selection via buttons
- Modal interaction to enter credentials
- Server communication for authentication
- Local storage of user roles and tokens for later use

`selectRole()` renders or saves the selected role in `localStorage` and helps in rerendering of pages.

This file should be included at the end of the body tag in your `index.html` using:

```html
<script type="module" src="js/services/index.js" defer></script>
```

### Notes

- Make sure the modal component (`openModal`) is properly defined in `js/components/modals.js`
- Ensure that `API_BASE_URL` is correctly set in `config.js`

## Creating Service Modules

## 1. doctorServices.js

This service module is responsible for handling all API interactions related to doctor data. Instead of writing `fetch()` calls directly inside your dashboard pages, organize them here for **modularity**, **reusability**, and cleaner code separation.

Open the `doctorServices.js` file: `app/src/main/resources/static/js/services/doctorServices.js`

### Steps

1. Import API base URL from `../config/config.js`

```js
import { API_BASE_URL } from "../config/config.js";
```

This ensures you're not hardcoding URLs in multiple files.

2. Set Doctor API endpoint

```js
const DOCTOR_API = API_BASE_URL + '/doctor';
```

3. Create a function to get all doctors

Build a function `getDoctors()` that:
- Sends a GET request to the doctor endpoint
- Awaits a response from the server
- Extracts and returns the list of doctors from the response JSON
- Handles errors using a `try-catch` block
- Returns an empty list (`[]`) if something goes wrong to avoid breaking the frontend

This function is what your Admin dashboard and Patient dashboard will use to load all doctors and display them on the page.

4. Create a function to delete a doctor

Build a function `deleteDoctor(id, token)` that:
- Takes the doctor's unique `id` and an authentication `token`
- Constructs the full endpoint URL using the ID and token
- Sends a DELETE request to that endpoint
- Parses the JSON response and returns a success status and message
- Catches and handles errors to prevent frontend crashes

5. Create a function to save (add) a new doctor

This function `saveDoctor(doctor, token)` should:
- Accept a `doctor` object containing all doctor details (like name, email, availability)
- Also take a `token` for Admin authentication
- Send a POST request with headers specifying JSON data
- Include the doctor data in the request body (converted to JSON)
- Return a structured response with `success` and `message`
- Catch and log any errors to help during debugging

It powers the **Add Doctor** modal in the Admin dashboard and saves new doctor records in the database.

6. Create a function to filter doctors

This function `filterDoctors(name, time, specialty)`:
- Accepts parameters like `name`, `time`, and `specialty`
- Constructs a GET request URL by passing these values as route parameters
- Sends a GET request to retrieve matching doctor records
- Returns the filtered list of doctors, or an empty list if none are found
- Handles cases where no filters are applied (pass null or empty strings appropriately)
- Uses error handling to alert the user if something fails

This supports real-time search and filter features in the Admin dashboard.

### Task

- Always use `async/await` for fetch calls for better readability
- Use `try-catch` to gracefully handle network or server errors
- Never hardcode API URLs; instead, use a base config file
- Return consistent data formats (for example, `{ success, message }`) to simplify frontend logic
- Keep your service layer focused on communication logic, not UI logic

By organizing all your doctor-related API functions in `doctorServices.js`, you're:
- Making your code more modular and maintainable
- Avoiding repetition of fetch logic
- Separating responsibilities between UI and backend logic
- Preparing your app to scale with ease

## 2. patientServices.js

This module centralizes all API communication related to **patient** data. It handles sign-up, login, and appointment management. Keeping this logic separated from UI code improves reusability and maintainability.

Open the `patientServices.js` file: `app/src/main/resources/static/js/services/patientServices.js`

### Steps

1. Import the API base URL

```js
import { API_BASE_URL } from "../config/config.js";
```

You'll use this base to construct specific endpoint URLs for patient-related actions.

2. Set the base patient API endpoint

Define a constant (for example, `PATIENT_API`) that represents the base path for all patient-related requests - typically `/api/patient`.

```js
const PATIENT_API = API_BASE_URL + '/patient';
```

This avoids duplicating the path in multiple places and makes future updates easier.

3. Create a function to handle patient signup

This function `patientSignup(data)` will:
- Accept a `data` object with patient details (name, email, password, etc.)
- Send a POST request to the signup endpoint
- Include the patient details as JSON in the request body
- Wait for the response, extract the message, and return a structured object with `success` and `message` properties
- Handle any failures with a `try-catch` block and return an appropriate error message

4. Create a function for patient login

This function `patientLogin(data)`:
- Accepts login credentials (typically email and password)
- Sends a POST request to the login endpoint
- Includes headers indicating JSON content and passes the login data in the body
- Returns the full fetch response so the frontend can check status, extract token, etc.
- Logging the input data can help during development (but should be removed in production)

Used during login to authenticate patients and allow secure access to dashboards or features.

5. Create a function to fetch logged-in patient data

This function `getPatientData(token)`:
- Takes an authentication `token` (from `localStorage`)
- Sends a GET request using this token to retrieve the patient's details (name, id, etc.)
- Returns the `patient` object if successful
- Handles any errors gracefully and returns `null` if the request fails

Used when booking appointments or viewing patient profile information.

6. Create a function to fetch patient appointments

This function `getPatientAppointments(id, token, user)` is a bit more dynamic:
- Accepts three parameters:
  - `token`: Authentication token
  - `user`: String indicating who's requesting (for example, `"patient"` or `"doctor"`)
- Constructs a dynamic API URL that works for both dashboards - doctor and patient
- Sends a GET request and returns the `appointments` array
- If unsuccessful, logs the error and returns `null`

A single, shared API call supports both dashboards with role-based behavior on the backend.

7. Create a function to filter appointments

This function `filterAppointments(condition, name, token)`:
- Accepts `condition` (like `"pending"` or `"consulted"`), `name`, and a `token`
- Sends a GET request to a filtered endpoint
- Returns the list of filtered appointments if the request is successful
- Returns an empty list if something fails, and logs errors for easier debugging
- Alerts the user if the error is unexpected

Helps in real-time filtering and searching of appointments, improving user experience.

### Task

- Use clear function names (`patientSignup`, `getPatientAppointments`) that reflect the purpose
- Wrap all async code in `try-catch` to handle API or network errors
- Avoid repeating base URLs; build them from a central `config.js`
- Use comments inside each function to indicate what step is happening, especially for collaboration or learning teams

By organizing all patient-related API communication in `patientServices.js`, you:
- Keep your UI code cleaner and easier to read
- Make the app easier to debug, extend, and maintain
- Enable code reusability across different user roles
- Reduce the risk of introducing bugs by avoiding repeated logic

## Creating Dashboard

## 1. adminDashboard.js - Managing Doctors

1. Open the JS file: `app/src/main/resources/static/js/adminDashboard.js`

2. Import required modules:
   - The `openModal` function from the modal component file `../components/modals.js`
   - The `getDoctors`, `filterDoctors`, `saveDoctor` functions from the `doctorServices` component file `./services/doctorServices.js`
   - The `createDoctorCard` function from the `doctorCard` component file `../components/doctorCard.js`

3. Event binding

When the admin clicks the **Add Doctor** button, it triggers `openModal('addDoctor')`.

```js
document.getElementById("addDocBtn").addEventListener("click", () => {
  openModal('addDoctor');
});
```

4. Load doctor cards on page load

- Use `DOMContentLoaded` or `window.onload` to trigger `loadDoctorCards()` on page load
- `loadDoctorCards()`:
  - Calls `getDoctors()` to fetch doctor list from backend
  - Clears existing content using `innerHTML = ""`

```js
const contentDiv = document.getElementById("content");
contentDiv.innerHTML = "";
```

  - Iterates through results and injects them using `createDoctorCard()` from `./components/doctorCard.js`
  - Appends each card to the `contentDiv`

5. Implement search and filter logic

Set up event listeners on:
- `#searchBar`
- Filter dropdowns

```js
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
```

- Gather current filter/search values
- Fetch and display filtered results using `filterDoctors()` from `./services/doctorServices.js`
- If no match, show message `"No doctors found"`.

6. `renderDoctorCards(doctors)`: utility function to render doctor cards when passed a list

- It iterates through the list passed and displays the cards

7. Handle Add Doctor modal

When the **Add Doctor** button is clicked:
- `openModal()` opens the modal
- The modal form is populated with input fields for:
  - Name, specialty, email, password, mobile no, availability time
- Collects any checkbox values for doctor availability
- On form submission:
  - Use `adminAddDoctor()` to collect data
  - Verify that a valid login token exists (to authenticate the admin)
  - Send a POST request using `saveDoctor()` from `./services/doctorServices.js`
  - If successful, close the modal, reload the page or doctor list, and show a success message and refresh the doctor list
  - If it fails, alert the user with an error message

### Notes

- Ensure the `createDoctorCard` function is imported from `./components/doctorCard.js`
- Ensure the `getDoctors`, `filterDoctors`, `saveDoctor` functions are imported from `./services/doctorServices.js`
- Use async/await for API calls

## 2. doctorDashboard.js - Managing Appointments

1. Open the JS file: `app/src/main/resources/static/js/doctorDashboard.js`

2. Import required modules:
   - The `getAllAppointments` function from the service file `./services/appointmentRecordService.js`
   - The `createPatientRow` function from the `patientRows` component file `./components/patientRows.js`

3. Initialize global variables

Define and store:
- A reference to the appointment table body where rows will be rendered (`#patientTableBody`)
- `selectedDate`, initialized to today's date
- `token`, retrieved from `localStorage` (used for authentication)
- `patientName`, initialized as `null`, for search filtering

4. Setup search bar functionality

- On input change, update the `patientName` variable
- If the search input is empty, default `patientName` to `null`
- Call `loadAppointments()` to refresh the list with filtered data

5. Bind event listeners to filter controls

- "Today's Appointments" button `#todayButton`
  - Resets the `selectedDate` to today
  - Updates the date picker field to reflect today's date
  - Calls `loadAppointments()`

- Date picker `#datePicker`
  - Updates the `selectedDate` variable when changed
  - Calls `loadAppointments()` to fetch and display appointments for the selected date

6. Define `loadAppointments()` function

This function:
- Uses `getAllAppointments(selectedDate, patientName, token)` to fetch appointment data
- Clears existing content in the table
- If no appointments are found:
  - Displays a row with a `"No Appointments found for today"` message
- For each appointment:
  - Extract the patient's details
  - Use `createPatientRow()` to create a `<tr>` for each
  - Append each row to the appointment table body

Wrap this logic in a `try-catch` block:
- In case of error, display a fallback error message row in the table

7. Initial render on page load

When the page is loaded (`DOMContentLoaded`):
- Call `renderContent()` if used
- Call `loadAppointments()` to load today's appointments by default

### Notes

- The `getAllAppointments()` function is responsible for backend API calls based on the selected date and search term
- The `createPatientRow()` component is used to dynamically build each row of the appointments table
- All API calls should include the doctor's token for authentication (retrieved from `localStorage`)
- Always use async/await syntax when working with `fetch()` to ensure proper flow control and error handling
- Ensure meaningful fallback messages are shown if no appointments are found or if the API fails

## 3. patientDashboard.js - Viewing & Filtering Doctors

1. Open the JS file: `app/src/main/resources/static/js/patientDashboard.js`

2. Import required modules

At the top of the file, import:
- `createDoctorCard` from `./components/doctorCard.js` for rendering doctor information cards
- `openModal` from `./components/modals.js` for opening login/signup modals
- `getDoctors`, `filterDoctors` from `./services/doctorServices.js` for retrieving doctor data
- `patientLogin`, `patientSignup` from `./services/patientServices.js` for handling patient authentication

3. Load doctor cards on page load

When the page loads:
- Call `loadDoctorCards()` inside a `DOMContentLoaded` event listener
- This function:
  - Calls `getDoctors()` to fetch the list of all available doctors
  - Clears any existing content inside the `#content` div
  - Iterates over the results and renders each doctor using `createDoctorCard()`

```js
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});
```

4. Bind modal triggers for login and signup

Add event listeners on:
- The Signup button (`#patientSignup`) -> opens the `patientSignup` modal
- The Login button (`#patientLogin`) -> opens the `patientLogin` modal

```js
document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("patientSignup");
  if (btn) btn.addEventListener("click", () => openModal('patientSignup'));
});

document.addEventListener("DOMContentLoaded", () => {
  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) loginBtn.addEventListener("click", () => openModal('patientLogin'));
});
```

5. Search and filter logic

Set up listeners for:
- Search bar `#searchBar`
- Availability time filter `#filterTime`
- Specialty filter `#filterSpecialty`

Each change triggers `filterDoctorsOnChange()`.

```js
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
```

`filterDoctorsOnChange()` function:
- Gathers values from all three filter/search inputs
- Uses `filterDoctors(name, time, specialty)` to fetch filtered results
- Clears the existing content
- If doctors are found, renders them using `createDoctorCard()`
- If not, displays a fallback message

```js
function filterDoctorsOnChange() {
  ...
  filterDoctors(name, time, specialty).then(response => {
    ...
    contentDiv.innerHTML = doctors.length > 0 ? renderedCards : "<p>No doctors found with the given filters.</p>";
  });
}
```

6. Render utility

The `renderDoctorCards(doctors)` function is available for rendering a given list of doctors dynamically, used optionally by other modules.

7. Handle patient signup

The `signupPatient()` function is triggered on form submission:
- Collects user inputs (name, email, password, phone, address)
- Sends the data to the backend via `patientSignup()`
- On success:
  - Closes the modal and reloads the page
- On failure:
  - Shows an error message

```js
window.signupPatient = async function () {
  ...
};
```

8. Handle patient login

The `loginPatient()` function is triggered on login form submission:
- Captures login credentials (email, password)
- Calls `patientLogin()` to authenticate
- On success:
  - Stores JWT token in `localStorage`
  - Redirects user to `loggedPatientDashboard.html`
- On failure:
  - Shows error alert

```js
window.loginPatient = async function () {
  ...
};
```

### Notes

- Ensure all modal IDs (`patientSignup`, `patientLogin`) are properly assigned in HTML
- All doctor fetching functions (`getDoctors`, `filterDoctors`) should be async/await-based for clean flow and error handling
- Use `createDoctorCard()` to maintain consistent UI design across dashboards
- Use fallback messages for empty search/filter results or API errors

## Save your work

Before you finish this lab, make sure to **add, commit, and push** your updated code to your GitHub repository. You will be asked to provide your public GitHub repo URL for graded evaluation at the end of the capstone.

Follow these steps to push your changes:

1. Stage your changes:

```bash
git add .
```

2. Commit your changes with a meaningful message:

```bash
git commit -m "Completed Developing Services and Utilities"
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

## Conclusion and Next Steps

- Structure the HTML to support dynamic data rendering
- Use modular JavaScript to fetch, render, and filter patient records
- Apply clean and responsive CSS to support both usability and aesthetics
- Handle real-time filtering and data updates based on date selection or user input
- Maintain separation of concerns by keeping services, rendering, and layout logic in separate files

### Next Steps

- **Extend Filters:** Add dropdowns or checkboxes to filter patients by status ("Consulted" and "pending")
- **Error Handling:** Display user-friendly messages when API calls fail or return unexpected data
- **Mobile Optimization:** Use media queries to fine-tune layouts for phones and tablets
- Add pagination or infinite scroll to handle large patient lists

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
