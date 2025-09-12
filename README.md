# Bank Cards Management System

## Prerequisites
- Java 17
- Maven
- Docker
- PostgreSQL

## Setup
1. Clone the repository: `git clone https://github.com/PaatoM/Bank_REST`
2. Configure `application.yml` with your database settings.
3. Run `docker-compose.yml` to start PostgreSQL and pgAdmin: `docker-compose up -d`
4. Build and run the application:
`mvn clean package && java -jar target/bankcards-0.0.1-SNAPSHOT.jar`
5. Access Swagger UI at `http://localhost:8181/swagger-ui.html`

## Endpoints
- `/api/auth/login` - Login and get JWT
- `/api/admin/**` - Admin endpoints (require ADMIN role)
- `/api/user/**` - User endpoints (require USER role)

## FullStack