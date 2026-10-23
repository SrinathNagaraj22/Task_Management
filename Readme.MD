# TaskFlow – Task Management System

A REST API application built using Java, Spring Boot, Spring Security, JWT, and MySQL to manage tasks and team collaboration.

## Technologies

* Java 17
* Spring Boot
* Spring Security & JWT
* Spring Data JPA / Hibernate
* MySQL
* Maven
* Postman

## Features

* User registration and login with JWT authentication
* Create, update, delete, and search tasks
* Assign tasks and update their status
* Team creation and member management
* Task comments
* User profile management
* Task attachment upload, download, and deletion

## Database

The application uses MySQL with the following tables:

* `users`
* `tasks`
* `teams`
* `team_members`
* `comments`
* `task_attachments`

Refer to the accompanying Word document for the database schema, entity relationships, and API documentation.

## Setup

1. Create a MySQL database named `taskflow_db`.

2. Configure the database credentials and JWT secret using environment variables.

3. Build the application:

   ```bash
   ./mvnw clean package
   ```

   On Windows:

   ```powershell
   .\mvnw.cmd clean package
   ```

4. Run the application:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

The application runs at `http://localhost:8080` by default.

## API

All endpoints use the `/api` prefix. Authentication endpoints are public; protected endpoints require a valid JWT.

Main endpoints:

* `/api/auth` – Registration and login
* `/api/users/me` – User profile
* `/api/tasks` – Task management
* `/api/teams` – Team management
* `/api/tasks/{taskId}/comments` – Task comments
* `/api/tasks/{taskId}/attachments` – Task attachments

## Documentation

Refer to the accompanying Word document for detailed database tables, primary and foreign keys, entity relationships, and API endpoints.
