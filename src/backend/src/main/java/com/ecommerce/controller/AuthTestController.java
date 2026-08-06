package com.ecommerce.controller;

import com.ecommerce.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/authtest")
public class AuthTestController {

    @GetMapping
    public ResponseEntity<?> get() {
        Integer customerId = SecurityUtils.currentCustomerId();
        if (customerId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized."));
        }
        return ResponseEntity.ok(Map.of("message", "Authenticated"));
    }
}
