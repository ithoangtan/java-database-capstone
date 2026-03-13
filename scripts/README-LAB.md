# Lab: Adding Databases and Tables – Commands to run

Chạy các lệnh sau **trong terminal của bạn** (nơi đã có `mysql` và `mongosh`).

## 1. Tạo database MySQL

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS cms;"
```

(Nếu MySQL yêu cầu mật khẩu: `mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS cms;"` — và nhớ sửa `spring.datasource.password` trong `app/src/main/resources/application.properties` cho đúng.)

## 2. Cấu hình và chạy Spring Boot để tạo bảng

Đã cấu hình `app/src/main/resources/application.properties` với:
- MySQL: `localhost:3306`, user `root`, password rỗng (sửa nếu bạn dùng password).
- MongoDB: `mongodb://localhost:27017/prescriptions`.

Chạy ứng dụng (từ thư mục project):

```bash
cd app
mvn spring-boot:run
```

Chờ đến khi ứng dụng chạy xong (thấy dòng kiểu "Started ... Application"). Khi đó các bảng trong `cms` đã được tạo.

## 3. Insert dữ liệu vào MySQL

Mở terminal mới (giữ Spring Boot chạy ở terminal cũ), chạy:

```bash
cd java-database-capstone
mysql -u root cms < scripts/insert_data.sql
```

Hoặc (nếu có password):

```bash
mysql -u root -p cms < scripts/insert_data.sql
```

## 4. Insert prescriptions vào MongoDB

```bash
mongosh --file scripts/insert_prescriptions.js
```

Hoặc (nếu cần chỉ định URI):

```bash
mongosh "mongodb://localhost:27017" --file scripts/insert_prescriptions.js
```

## 5. Kiểm tra dữ liệu

**MySQL:**

```sql
use cms;
SHOW TABLES;
SELECT * FROM doctor LIMIT 5;
SELECT * FROM doctor_available_times LIMIT 5;
SELECT * FROM patient LIMIT 5;
SELECT * FROM appointment ORDER BY appointment_time LIMIT 5;
SELECT * FROM admin;
```

**MongoDB (mongosh):**

```javascript
use prescriptions;
db.prescriptions.find().limit(5).pretty();
```

## Ghi chú

- Database **cms** được tạo bằng lệnh MySQL (bước 1), **không** tạo trong MongoDB CLI (câu hỏi 2 trong lab có thể nhầm MongoDB/MySQL).
- Nếu MySQL của bạn có password, sửa `spring.datasource.password=` trong `application.properties` và dùng `-p` khi gọi `mysql`.
