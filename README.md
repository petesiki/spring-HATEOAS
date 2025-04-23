# Spring HATEOAS Demo with Custom Header Support

This demo application showcases Spring HATEOAS with proper handling of both standard `X-Forwarded-*` headers and custom proxy headers.

## Features

- Spring Boot 3.2.5 with Spring HATEOAS
- RESTful endpoints with HATEOAS links
- Proper handling of both standard and custom forwarded headers for proxy-aware links
- Transparent header translation without controller modifications
- Docker and Docker Compose setup for development
- VS Code development container configuration

## Custom Header Support

This application demonstrates a clean approach to supporting custom proxy headers:

- Configurable custom header names via `application.properties`
- Automatic translation of custom headers to standard `X-Forwarded-*` headers
- Controller-agnostic implementation (all header logic confined to `WebConfig.java`)
- Works with any proxy server or API gateway that uses custom headers

The following custom headers are supported by default:
- `X-Custom-Host` → translates to → `X-Forwarded-Host`
- `X-Custom-Proto` → translates to → `X-Forwarded-Proto`
- `X-Custom-Port` → translates to → `X-Forwarded-Port`

You can configure different header names in `application.properties`.

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
   - Spring Boot API: http://localhost:8546/api/employees
   - Through Nginx proxy: http://localhost/api/employees

### Development with VS Code

1. Open the project in VS Code
2. When prompted, click "Reopen in Container" to develop inside the Docker container
3. The Java extensions will be installed automatically in the container

## API Endpoints

- `GET /api/employees` - List all employees with HATEOAS links
- `GET /api/employees/{id}` - Get a specific employee with HATEOAS links
- `GET /api/proxy-info` - Get information about proxy headers in the current request

## Testing Custom Headers

To test the custom header handling:

1. Send a request with custom headers:
   ```bash
   curl -H "X-Custom-Host: example.com" -H "X-Custom-Proto: https" http://localhost:8546/api/employees
   ```

2. Notice how links in the response use `https://example.com` as the base URL, even though your controllers don't need any special code to handle these headers.

3. Check the proxy information endpoint for details about received headers:
   ```bash
   curl -H "X-Custom-Host: example.com" -H "X-Custom-Proto: https" http://localhost:8546/api/proxy-info
   ```

## How It Works

The custom header support is implemented entirely in `WebConfig.java` using a servlet filter chain:

1. The `CustomHeaderRequestWrapper` translates custom headers to standard ones
2. Spring's `ForwardedHeaderFilter` processes these translated headers
3. Spring HATEOAS uses the processed headers when generating links
4. Controllers don't need any special code to work with custom headers

This approach maintains clean separation of concerns and works with any controller in your application.