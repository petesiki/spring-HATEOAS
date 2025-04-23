package com.example.hateoas.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hateoas.model.Employee;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    // Simple in-memory repository simulation
    private List<Employee> getEmployees() {
        return IntStream.range(1, 4)
                .mapToObj(i -> new Employee((long) i, "Employee " + i, "Role " + i))
                .collect(Collectors.toList());
    }

    @GetMapping("/employees")
    public ResponseEntity<CollectionModel<EntityModel<Employee>>> getAllEmployees(HttpServletRequest request) {
        // Get the client's IP, using X-Forwarded-For header if available
        String clientIp = getClientIp(request);
        
        List<EntityModel<Employee>> employees = getEmployees().stream()
                .map(employee -> EntityModel.of(employee,
                        linkTo(methodOn(EmployeeController.class).getEmployee(employee.getId(), request)).withSelfRel(),
                        linkTo(methodOn(EmployeeController.class).getAllEmployees(request)).withRel("employees")))
                .collect(Collectors.toList());

        Link link = linkTo(methodOn(EmployeeController.class).getAllEmployees(request)).withSelfRel();
        
        // Add a custom link that includes client IP information
        Link proxyAwareLink = Link.of(link.getHref() + "/proxy-info").withRel("proxy-info");
        
        return ResponseEntity.ok(CollectionModel.of(employees, link, proxyAwareLink));
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<EntityModel<Employee>> getEmployee(@PathVariable Long id, HttpServletRequest request) {
        // Find the employee by ID (in a real app, you'd use a repository)
        Employee employee = getEmployees().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        String clientIp = getClientIp(request);
        
        // Add a custom link that includes client IP information
        Link proxyInfoLink = Link.of(
                linkTo(methodOn(EmployeeController.class).getEmployee(id, request)).withSelfRel().getHref() + "/proxy-info")
                .withRel("proxy-info");
        
        EntityModel<Employee> resource = EntityModel.of(employee,
                linkTo(methodOn(EmployeeController.class).getEmployee(id, request)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).getAllEmployees(request)).withRel("employees"),
                proxyInfoLink);
        
        return ResponseEntity.ok(resource);
    }
    
    @GetMapping("/proxy-info")
    public ResponseEntity<String> getProxyInfo(HttpServletRequest request) {
        StringBuilder info = new StringBuilder();
        info.append("IP Information:\n");
        info.append("Remote Address: ").append(request.getRemoteAddr()).append("\n");
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            info.append("X-Forwarded-For: ").append(xForwardedFor).append("\n");
        }
        
        String xForwardedHost = request.getHeader("X-Forwarded-Host");
        if (xForwardedHost != null) {
            info.append("X-Forwarded-Host: ").append(xForwardedHost).append("\n");
        }
        
        String xForwardedProto = request.getHeader("X-Forwarded-Proto");
        if (xForwardedProto != null) {
            info.append("X-Forwarded-Proto: ").append(xForwardedProto).append("\n");
        }
        
        return ResponseEntity.ok(info.toString());
    }
    
    // Helper method to extract the client IP address considering X-Forwarded-For
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2, ...)
            // The first one is the original client IP
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}