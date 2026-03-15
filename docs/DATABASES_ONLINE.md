# MySQL & MongoDB Online — Lightweight Deployment (Free Tier)

When you deploy your app to the cloud (Railway, Render, Fly.io, etc.), there is no MySQL or MongoDB running by default. You can use free or low-cost online database services and configure your app via **environment variables**.

---

## 1. MongoDB Online (Recommended: MongoDB Atlas — Free)

**MongoDB Atlas** offers a free tier (M0); no credit card required.

1. Sign up: https://www.mongodb.com/cloud/atlas/register  
2. Create an **Organization** and **Project** (any names).  
3. Create a **Cluster** and select the **FREE** (M0) tier.  
4. In **Database Access** → **Add New Database User**: create a user and password (save them).  
5. In **Network Access** → **Add IP Address** → **Allow Access from Anywhere** (`0.0.0.0/0`) for simplicity (in production, restrict by IP).  
6. In the cluster → **Connect** → **Drivers** → copy the **connection string**, e.g.:
   ```text
   mongodb+srv://<user>:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
7. Replace `<password>` with your password and add the database name (this app uses `prescriptions`):
   ```text
   mongodb+srv://user:password@cluster0.xxxxx.mongodb.net/prescriptions?retryWrites=true&w=majority
   ```

**Environment variable when deploying:**

- `SPRING_DATA_MONGODB_URI=<the string above>`

### Result
https://cloud.mongodb.com/v2/5de01895014b7608bc604897#/clusters/connect?clusterId=Cluster0 

---

## 2. MySQL Online (Lightweight Options)

The app expects **MySQL 8** (or compatible). Choose one of the options below.

### A. Railway (Easy, Free Tier)

1. Go to https://railway.app and sign in (e.g. with GitHub).  
2. **New Project** → **Add MySQL** (or **Add Plugin** → MySQL).  
3. Railway creates a MySQL instance and shows the **connection URL** (or host, port, user, password).  
4. The URL is usually like: `mysql://user:password@host:port/railway`  
   → JDBC URL: `jdbc:mysql://host:port/railway?useSSL=true` (adjust the database name if Railway uses a different one).

**Environment variables:**

- `SPRING_DATASOURCE_URL=jdbc:mysql://<host>:<port>/<database>?useSSL=true`
- `SPRING_DATASOURCE_USERNAME=<user>`
- `SPRING_DATASOURCE_PASSWORD=<password>`

### Result
https://railway.com/project/a2a68241-f159-49c3-88c7-9136d4f15a62/service/14d4ef25-ec28-4d0a-8958-f64e246625f4/variables?environmentId=560f319c-e0a6-468f-9820-0570f8fbcf12

### B. PlanetScale (MySQL-Compatible, Limited Free Tier)

1. Go to https://planetscale.com and sign up (e.g. with GitHub).  
2. Create a **Database** and choose a region.  
3. Go to **Connect** → **Connect with** → **General** → copy host, user, and password.  
4. PlanetScale uses the **MySQL protocol**; the connection string looks like:
   ```text
   jdbc:mysql://<host>/<database>?sslMode=VERIFY_IDENTITY
   ```
   (check the exact format in PlanetScale’s Connect screen.)

**Environment variables:** same as above with `SPRING_DATASOURCE_*`.

### C. Free MySQL Hosting (Dev/Test)

- **RemoteMySQL**: https://remotemysql.com — create a free DB and get host, user, password.  
- **FreeDB**: https://freedb.tech — free 25MB.  

Both speak standard MySQL; plug the values into `SPRING_DATASOURCE_URL`, `USERNAME`, and `PASSWORD`.

---

## 3. Configuring the App When Deploying

Spring Boot reads the following environment variables (they override `application.properties`):

| Environment variable       | Meaning |
|---------------------------|---------|
| `SPRING_DATASOURCE_URL`   | MySQL JDBC URL (starts with `jdbc:mysql://...`) |
| `SPRING_DATASOURCE_USERNAME` | MySQL user |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password |
| `SPRING_DATA_MONGODB_URI` | MongoDB URI (Atlas format: `mongodb+srv://...`) |

**Example** (Railway / Render / Fly.io / any host):

- In the host’s dashboard → **Variables** / **Environment**, add:
  - `SPRING_DATASOURCE_URL=jdbc:mysql://...`
  - `SPRING_DATASOURCE_USERNAME=...`
  - `SPRING_DATASOURCE_PASSWORD=...`
  - `SPRING_DATA_MONGODB_URI=mongodb+srv://...`

After deployment, the app will connect to your online MySQL and MongoDB; you do not need MySQL or MongoDB running on your machine or on GitHub.

---

## 4. Quick Recommendations

- **MongoDB**: use **MongoDB Atlas** (free M0).  
- **MySQL**: for quick dev/demo → **Railway** or **RemoteMySQL**; for a managed MySQL-compatible service → **PlanetScale**.  
- Always configure via **environment variables** on your deployment platform; do not hardcode URLs or passwords in code.

If you deploy on a specific platform (e.g. Railway, Render), you can add a step-by-step guide for that platform in the repo.
