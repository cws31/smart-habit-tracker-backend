# Smart Habit Tracker Backend

A robust Spring Boot application for tracking and managing habits with features like streak tracking, certificates, and email notifications.

## Features

- User Authentication with JWT
- Habit Management
- Progress Tracking
- Streak Calculation
- Certificate Generation
- Email Notifications
  - Welcome Emails
  - Daily Reminders
  - Challenge Completion Notifications

## Tech Stack

- Java 21
- Spring Boot 3.5.6
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Maven
- Docker Support
- Thymeleaf (Email Templates)

## Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Maven 3.x

## Configuration

### Database Configuration

Configure your MySQL database connection in `application.properties`:

```properties
spring.datasource.url=${DATABASE_URL:jdbc:mysql://localhost:3306/SHT}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

### Email Configuration

Configure email settings in `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

## Running the Application

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Using Docker

```bash
docker build -t smart-habit-tracker .
docker run -p 9090:9090 smart-habit-tracker
```

## API Endpoints

### Authentication
- POST `/api/auth/login` - User login
- POST `/api/auth/register` - User registration
- POST `/api/auth/logout` - User logout

### Habits
- GET `/api/habits` - List all habits
- POST `/api/habits` - Create new habit
- PUT `/api/habits/{id}` - Update habit
- DELETE `/api/habits/{id}` - Delete habit

### Progress
- GET `/api/progress` - Get habit progress
- POST `/api/logs` - Log habit completion
- GET `/api/certificates` - Get earned certificates

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.