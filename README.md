# Job Portal

Spring Boot serves both the REST API and the built React app. The default `dev` profile uses an in-memory H2 database and seeds local demo accounts and jobs. The Vite development server can also run separately and proxies API requests to Spring Boot.

## Requirements

- JDK 17
- Maven 3.9 or newer
- Internet access for the first Maven build so it can install the pinned Node/npm versions and resolve dependencies

## Run Locally on Windows

Start the backend and bundled frontend:

```powershell
Set-Location backend
mvn spring-boot:run
```

Open <http://localhost:8080/sign-in>. If port 8080 is already occupied, set `$env:SERVER_PORT = '8081'` before starting and use port 8081.

For frontend hot reload, keep the backend running, then use another terminal:

```powershell
Set-Location frontend
npm install
$env:VITE_API_PROXY_TARGET = 'http://localhost:8080'
npm run dev
```

When Spring Boot uses another port, set `VITE_API_PROXY_TARGET` to that backend URL. Vite serves the frontend at <http://localhost:5173>.

## Use MySQL

Create the schema once:

```sql
CREATE DATABASE job_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then start the MySQL profile. Supply your own credentials and a signing secret of at least 32 characters; do not commit them:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'your-local-mysql-password'
$env:JWT_SECRET = 'your-own-random-secret-of-at-least-32-characters'
Set-Location backend
mvn spring-boot:run
```

`DB_URL` can override the default `localhost:3306/job_portal` JDBC URL. `CORS_ALLOWED_ORIGINS` can override the Vite origin if your local frontend uses a different host or port. MySQL mode does not create demo users; register a seeker/recruiter, and provision admin accounts separately.

## Local Demo Accounts

These accounts are seeded only by the `dev` profile:

- Admin: `admin@jobconnect.com` / `admin123`
- Recruiter: `recruiter@jobconnect.com` / `recruiter123`
- Job seeker: `seeker@jobconnect.com` / `seeker123`

## Build and Test

```powershell
Set-Location backend
mvn test
mvn package
```

The Maven build runs the frontend production build and packages it in the Spring Boot application.

## Deploy the Frontend to Netlify

The Spring Boot API must be deployed separately because Netlify hosts static frontend assets, not this Spring Boot server. Deploy the backend with the `mysql` profile and configure its database credentials and `JWT_SECRET` first.

Connect this repository to Netlify. The root `netlify.toml` selects `frontend` as the base directory, runs `npm run build:netlify`, and publishes `frontend/dist`. In Netlify site environment variables, set `VITE_API_BASE_URL` to the public backend URL (for example, `https://api.example.com`). On the backend, set `CORS_ALLOWED_ORIGINS` to the Netlify site URL (for example, `https://your-site.netlify.app`). Redeploy after setting these values.

Netlify assigns the public deployment URL after the site is linked and built. This workspace has no Netlify CLI authentication or public backend URL, so a live hosted URL cannot be created from this environment yet.
