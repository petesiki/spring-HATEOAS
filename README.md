# Spring HATEOAS Demo with X-Forwarded-For Header Support

This demo application showcases Spring HATEOAS with proper handling of `X-Forwarded-For` headers.

## Features

- Spring Boot 3.2.5 with Spring HATEOAS
- RESTful endpoints with HATEOAS links
- Proper handling of X-Forwarded-For headers for proxy-aware links
- Docker and Docker Compose setup for development
- VS Code development container configuration

## Getting Started

### Prerequisites

- Docker and Docker Compose
- VS Code with Remote Containers extension (optional for VS Code development)

### Running the Application

1. Build and run using Docker Compose:

```bash
docker-compose up -d
```

2. The application will be available at:
   - Spring Boot API: http://localhost:8080/api/employees
   - Through Nginx proxy: http://localhost/api/employees

### Development with VS Code

1. Open the project in VS Code
2. When prompted, click "Reopen in Container" to develop inside the Docker container
3. The Java extensions will be installed automatically in the container

## API Endpoints

- `GET /api/employees` - List all employees with HATEOAS links
- `GET /api/employees/{id}` - Get a specific employee with HATEOAS links
- `GET /api/proxy-info` - Get information about proxy headers in the current request

## Testing X-Forwarded-For

To test the X-Forwarded-For handling:

1. Access the API through the Nginx proxy (http://localhost/api/employees)
2. Notice the proxy-aware links in the HATEOAS response
3. Check the proxy information at http://localhost/api/proxy-info