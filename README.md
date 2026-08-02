# Budget Tracker

This is a personal finance management application built with Spring Boot. It allows users to manage a budget, record expenses, organize them into categories, and keep track of their spending.

I created this project to improve my skills with Spring Boot and learn how to build a complete web application using Java, Spring Security, Thymeleaf, and PostgreSQL.

## Features

- User registration and login
- Secure authentication with Spring Security
- Budget management
- Expense management
- Category management
- Search, sorting, and pagination
- Dashboard with budget information
- User-specific data

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Maven

**Frontend**
- Thymeleaf
- HTML
- CSS
- JavaScript

**Database**
- PostgreSQL

## Running the Project

Clone the repository:

```bash
git clone https://github.com/CCHQSA/Budget-tracker.git
cd Budget-tracker
```

Configure your PostgreSQL database in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/budget_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Run the application:

```bash
mvn spring-boot:run
```

The application will be available at:

```
http://localhost:8080
```

## Project Structure

```
src
├── controller
├── service
├── repository
├── entity
├── dto
├── security
├── config
├── templates
└── static
```

## What I Learned

Working on this project helped me gain experience with:

- Spring Security authentication
- CRUD operations with Spring Data JPA
- Entity relationships
- DTOs and mappers
- Thymeleaf templates
- Form validation
- Exception handling
- Pagination and sorting
- Building a layered Spring Boot application

## Future Improvements

There are still a few things I'd like to add:

- REST API
- Docker support
- Unit and integration tests
- Swagger/OpenAPI
- Charts for spending statistics
- CSV/PDF export

## Author

Mykola Lotockiy

GitHub: https://github.com/CCHQSA
