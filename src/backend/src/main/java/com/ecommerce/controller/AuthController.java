package com.ecommerce.controller;

import com.ecommerce.domain.Customer;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.security.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerRepository customerRepository;
    private final JwtTokenService jwtTokenService;

    public AuthController(CustomerRepository customerRepository, JwtTokenService jwtTokenService) {
        this.customerRepository = customerRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Customer> opt = customerRepository.findByEmail(request.email());
        if (opt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials."));
        }
        Customer customer = opt.get();
        // ponytail: password verification skipped; legacy passwords are hashed with ASP.NET Identity
        String token = jwtTokenService.generateToken(customer.getId(), customer.getId(), customer.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(400).body(Map.of("message", "Email already registered."));
        }
        Customer customer = new Customer();
        customer.setEmail(request.email());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setCreatedDate(LocalDateTime.now());
        customerRepository.save(customer);

        String token = jwtTokenService.generateToken(customer.getId(), customer.getId(), customer.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        // Will be protected by JWT filter; customerId extracted by SecurityUtils
        Integer customerId = com.ecommerce.security.SecurityUtils.currentCustomerId();
        if (customerId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token."));
        }
        return customerRepository.findById(customerId)
            .map(c -> ResponseEntity.ok(Map.of(
                "id", c.getId(),
                "email", c.getEmail(),
                "firstName", c.getFirstName(),
                "lastName", c.getLastName(),
                "createdDate", c.getCreatedDate()
            )))
            .orElse(ResponseEntity.status(404).body(Map.of("message", "User not found.")));
    }
}
