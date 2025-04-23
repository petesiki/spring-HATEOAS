package com.example.hateoas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for handling HTTP forwarded headers.
 * This class sets up filters to process both standard X-Forwarded-* headers
 * and custom headers that may be used by different proxy servers.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${custom.header.host:X-Custom-Host}")
    private String customHostHeader;
    
    @Value("${custom.header.proto:X-Custom-Proto}")
    private String customProtoHeader;
    
    @Value("${custom.header.port:X-Custom-Port}")
    private String customPortHeader;
    
    /**
     * Configure our custom filter to run with highest precedence
     */
    @Bean
    public FilterRegistrationBean<Filter> customHeaderTranslationFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(customHeaderTranslationFilter());
        registration.addUrlPatterns("/*");
        registration.setName("customHeaderTranslationFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
    
    /**
     * Configure Spring's forwarded header filter to run after our custom filter
     */
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilterRegistration() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ForwardedHeaderFilter());
        registration.addUrlPatterns("/*");
        registration.setName("forwardedHeaderFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
    
    /**
     * Custom filter that translates custom headers to standard X-Forwarded-* headers
     * This filter must run before Spring's ForwardedHeaderFilter
     */
    @Bean
    public Filter customHeaderTranslationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // Wrap request to translate custom headers to standard headers
                HttpServletRequest wrappedRequest = new CustomHeaderRequestWrapper(request, 
                        customHostHeader, customProtoHeader, customPortHeader);
                
                // Continue with the filter chain using our wrapped request
                filterChain.doFilter(wrappedRequest, response);
            }
        };
    }
    
    /**
     * Request wrapper that adds standard X-Forwarded-* headers based on our custom headers
     */
    private static class CustomHeaderRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders = new HashMap<>();
        private final Set<String> headerNames = new HashSet<>();
        
        public CustomHeaderRequestWrapper(HttpServletRequest request,
                                         String customHostHeader,
                                         String customProtoHeader,
                                         String customPortHeader) {
            super(request);
            
            // Add all existing header names
            Enumeration<String> headerEnum = request.getHeaderNames();
            while (headerEnum.hasMoreElements()) {
                headerNames.add(headerEnum.nextElement());
            }
            
            // Map custom headers to standard forwarded headers
            mapCustomHeader(request, customHostHeader, "X-Forwarded-Host");
            mapCustomHeader(request, customProtoHeader, "X-Forwarded-Proto");
            
            // We don't map port explicitly as it's typically included in host or derived from proto
        }
        
        private void mapCustomHeader(HttpServletRequest request, String customHeader, String standardHeader) {
            String customValue = request.getHeader(customHeader);
            if (customValue != null && !customValue.isEmpty()) {
                customHeaders.put(standardHeader, customValue);
                headerNames.add(standardHeader);
            }
        }
        
        @Override
        public String getHeader(String name) {
            // Check our custom headers first
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            // Fall back to the original request
            return super.getHeader(name);
        }
        
        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headerNames);
        }
        
        @Override
        public Enumeration<String> getHeaders(String name) {
            if (customHeaders.containsKey(name)) {
                return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
            }
            return super.getHeaders(name);
        }
    }
}