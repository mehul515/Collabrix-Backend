# Collabrix Backend

This is the backend service for **Collabrix**, a project management platform. It is built with **Spring Boot** and connects to a **MySQL database** hosted on **Railway**. This service handles user authentication, project and task management, and other core functionalities.

---

## 🚀 Tech Stack

- Java 17
- Spring Boot 3
- Spring Data JPA
- Spring Security + JWT
- MySQL (Hosted on [Railway](https://railway.app))
- Maven

---

## ✅ Features

* ✅ User Registration and Authentication
* ✅ JWT-based Authorization
* ✅ Project & Task Management
* ✅ Role-Based Access Control
* ✅ Email Notifications

---

## ⚙️ Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/collabrix-backend.git
cd collabrix-backend
````

### 2. Update `application.properties`

Make sure your `src/main/resources/application.properties` contains:

```properties
spring.config.import=optional:file:.env[.properties]

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

server.port=8080

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

---

## 📂 Project Structure

```
src/
 ├── main/
 │    ├── java/com/collabrix/...
 │    └── resources/
 │         └── application.properties
 └── pom.xml...
```

## 🔗 Related Projects

* 🧠 [Collabrix Frontend (Next JS)](https://github.com/mehul515/Collabrix-Frontend)


---

