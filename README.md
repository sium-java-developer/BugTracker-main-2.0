Title:
    
    Bug Tracker
# Bug Tracker

A full-stack, monolithic web application for tracking software bugs, built with the Spring ecosystem. This project demonstrates a classic N-tier architecture and is containerized with Docker for easy deployment.

---

## ✨ Key Features

- **User Authentication & Authorization**: Secure login and registration system powered by Spring Security.
- **Role-Based Access Control**: Distinct permissions for `ADMIN` and `USER` roles.
- **Full CRUD Functionality**: Complete Create, Read, Update, and Delete operations for both Bugs and Users.
- **Bug Management**: Assign bugs to developers and track their status (e.g., Open, In Progress, Resolved).
- **Responsive UI**: A clean and modern user interface built with Thymeleaf and Bootstrap 5.
- **Containerized**: Includes a multi-stage `Dockerfile` for building a lightweight, production-ready image.
- **Comprehensive Testing**: Robust unit and integration tests for controllers and services using JUnit, Mockito, and REST Assured.

## 🏛️ Architectural Design

This application follows a classic **3-Tier Architecture**, promoting separation of concerns and maintainability.

1.  **Presentation Layer (UI)**
    -   **Thymeleaf**: Renders the dynamic server-side HTML.
    -   **Spring MVC Controllers**: Handle incoming HTTP requests, process user input, and return the appropriate views or data.

2.  **Business Logic Layer (Service)**
    -   **Service Classes**: Contain the core application logic, orchestrating operations between the controllers and the data access layer. This is where business rules are enforced.

3.  **Data Access Layer (DAL)**
    -   **Spring Data JPA Repositories**: Abstract the underlying data storage, providing a clean API for database operations.
    -   **Hibernate**: The JPA implementation (ORM) that maps Java objects to database tables.
    -   **H2 In-Memory Database**: Used for both local development and the deployed demo. The database is seeded with sample data on startup via a `CommandLineRunner` bean active in the `dev` profile.

**Cross-Cutting Concerns** are managed by Spring's AOP capabilities, with **Spring Security** handling authentication, authorization, and protection against common vulnerabilities like CSRF.

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5
- **Database**: H2 In-Memory Database
- **Build Tool**: Apache Maven
- **Server**: Embedded Tomcat
- **Containerization**: Docker
- **Testing**: JUnit 5, Mockito, REST Assured

## 🚀 How to Run Locally

1.  **Prerequisites**:
    - Java 17 or later
    - Apache Maven

2.  **Clone the repository**:
    ```bash
    git clone https://github.com/sium-java-developer/BugTracker.git
    cd BugTracker
    ```

3.  **Run the application using the Maven Wrapper**:
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on `http://localhost:8081`.

4.  **Access the application**:
    - Open your web browser and navigate to `http://localhost:8081`.
    - You can log in with the pre-seeded admin user:
      - **Username**: `jdoe`
      - **Password**: `password`
 
## 🐳 How to Deploy with Docker

The project includes a multi-stage `Dockerfile` to create an optimized and lightweight container.

1.  **Build the Docker image**:
    ```bash
    docker build -t bug-tracker-app -f docker-build/Dockerfile .
    ```

2.  **Run the Docker container**:
    ```bash
    docker run -p 8081:8081 bug-tracker-app
    ```
    The application will be accessible at `http://localhost:8081`.

### Deploying to Render.com

This application is configured to be deployed on Render's free tier using its in-memory database.

1.  Create a new **Web Service** on Render and connect your GitHub repository.
2.  Select **Docker** as the environment.
3.  Set the **Dockerfile Path** to `./docker-build/Dockerfile`.
4.  Deploy! No environment variables or persistent disks are needed. The application will start, and the `CommandLineRunner` will seed the in-memory database with sample data.
