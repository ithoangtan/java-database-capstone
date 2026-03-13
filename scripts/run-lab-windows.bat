@echo off
REM Lab: Adding Databases and Tables - Run in terminal where mysql and mongosh are available
REM Step 1: Create database cms
echo === Step 1: Creating MySQL database cms ===
mysql -u root -e "CREATE DATABASE IF NOT EXISTS cms;"
if errorlevel 1 (
  echo If mysql asks for password, run: mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS cms;"
  pause
  exit /b 1
)

echo.
echo === Step 2: Run Spring Boot to create tables ===
echo Open another terminal and run:
echo   cd app
echo   mvn spring-boot:run
echo Wait until the app has started and tables are created, then press any key here to continue...
pause

echo.
echo === Step 3: Insert data into MySQL tables ===
mysql -u root cms < "%~dp0insert_data.sql"
if errorlevel 1 (
  echo Try: mysql -u root -p cms ^< scripts/insert_data.sql
  pause
  exit /b 1
)
echo MySQL data inserted.

echo.
echo === Step 4: Insert prescriptions into MongoDB ===
mongosh --file "%~dp0insert_prescriptions.js"
if errorlevel 1 (
  echo If mongosh needs connection string: mongosh "mongodb://localhost:27017" --file scripts/insert_prescriptions.js
  pause
  exit /b 1
)
echo MongoDB prescriptions inserted.

echo.
echo === Step 5: Verify (optional) ===
echo Run these in MySQL: use cms; SELECT * FROM doctor LIMIT 5;
echo Run in mongosh: use prescriptions; db.prescriptions.find().limit(5).pretty();
echo.
echo Done.
pause
