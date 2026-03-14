/**
 * Demo accounts (match insert_data.sql). One random account is filled when opening the login form.
 */
export const ADMIN_ACCOUNTS = [
  { username: "admin", password: "admin@1234" },
];

export const DOCTOR_ACCOUNTS = [
  { email: "dr.adams@example.com", password: "pass12345" },
  { email: "dr.johnson@example.com", password: "secure4567" },
  { email: "dr.lee@example.com", password: "leePass987" },
  { email: "dr.wilson@example.com", password: "w!ls0nPwd" },
  { email: "dr.brown@example.com", password: "brownie123" },
  { email: "dr.taylor@example.com", password: "taylor321" },
  { email: "dr.white@example.com", password: "whiteSecure1" },
  { email: "dr.clark@example.com", password: "clarkPass456" },
  { email: "dr.davis@example.com", password: "davis789" },
  { email: "dr.miller@example.com", password: "millertime!" },
  { email: "dr.moore@example.com", password: "ellapass33" },
  { email: "dr.martin@example.com", password: "martinpass" },
  { email: "dr.jackson@example.com", password: "jackson11" },
  { email: "dr.thomas@example.com", password: "thomasPWD" },
  { email: "dr.hall@example.com", password: "hallhall" },
  { email: "dr.green@example.com", password: "greenleaf" },
  { email: "dr.baker@example.com", password: "bakeitup" },
  { email: "dr.walker@example.com", password: "walkpass12" },
  { email: "dr.young@example.com", password: "young123" },
  { email: "dr.king@example.com", password: "kingkong1" },
  { email: "dr.scott@example.com", password: "scottish" },
  { email: "dr.evans@example.com", password: "evansEv1" },
  { email: "dr.turner@example.com", password: "turnerBurner" },
  { email: "dr.hill@example.com", password: "hillclimb" },
  { email: "dr.ward@example.com", password: "wardWard" },
];

export const PATIENT_ACCOUNTS = [
  { email: "jane.doe@example.com", password: "passJane1" },
  { email: "john.smith@example.com", password: "smithSecure" },
  { email: "emily.rose@example.com", password: "emilyPass99" },
  { email: "michael.j@example.com", password: "airmj23" },
  { email: "olivia.m@example.com", password: "moonshine12" },
  { email: "liam.k@example.com", password: "king321" },
  { email: "sophia.l@example.com", password: "sophieLane" },
  { email: "noah.b@example.com", password: "noahBest!" },
  { email: "ava.d@example.com", password: "avaSecure8" },
  { email: "william.h@example.com", password: "willH2025" },
  { email: "mia.g@example.com", password: "miagreen1" },
  { email: "james.b@example.com", password: "jamiebrown" },
  { email: "amelia.c@example.com", password: "ameliacool" },
  { email: "ben.j@example.com", password: "bennyJ" },
  { email: "ella.m@example.com", password: "ellam123" },
  { email: "lucas.t@example.com", password: "lucasTurn" },
  { email: "grace.s@example.com", password: "graceful" },
  { email: "ethan.h@example.com", password: "hill2025" },
  { email: "ruby.w@example.com", password: "rubypass" },
  { email: "jack.b@example.com", password: "bakerjack" },
  { email: "mia.h@example.com", password: "hallMia" },
  { email: "owen.t@example.com", password: "owen123" },
  { email: "ivy.j@example.com", password: "ivyIvy" },
  { email: "leo.m@example.com", password: "leopass" },
  { email: "ella.moore@example.com", password: "ellamoore" },
];

function pickRandom(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

export function getRandomAdminAccount() {
  return pickRandom(ADMIN_ACCOUNTS);
}

export function getRandomDoctorAccount() {
  return pickRandom(DOCTOR_ACCOUNTS);
}

export function getRandomPatientAccount() {
  return pickRandom(PATIENT_ACCOUNTS);
}
