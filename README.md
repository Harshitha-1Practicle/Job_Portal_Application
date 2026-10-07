# Job Portal

The Spring Boot application serves the React frontend and REST API from one origin. Sign-in, registration, and dashboard routes are handled by the same application on port 8080.

## Requirements

- JDK 25
- Maven 3.9 or newer
- MySQL with a `job_portal` database
- Internet access for the first build so Maven can install the pinned Node/npm versions and download frontend dependencies

## Run on Windows

Set the MySQL password in the current PowerShell session, then start the application from the backend directory:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = 'your-local-mysql-password'
Set-Location backend
mvn spring-boot:run
```

Open <http://localhost:8080/sign-in> or <http://localhost:8080/register>. The first Maven run builds the frontend and serves it from Spring Boot; there is no separate Vite port in this flow.

To build and run the packaged application:

```powershell
mvn clean package
java -jar target/job-portal-0.0.1-SNAPSHOT.jar
```

The Vite dev server is still available for frontend hot reload during development. Its `/api` proxy targets the Spring Boot server on port 8080."# Job_Portal_Application" 
