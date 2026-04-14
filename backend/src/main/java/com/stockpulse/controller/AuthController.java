package com.stockpulse.controller;

import com.stockpulse.dto.LoginRequest;
import com.stockpulse.dto.SignupRequest;
import com.stockpulse.dto.AuthResponse;
import com.stockpulse.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        AuthResponse resp = authService.signup(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                    "success", true,
                    "message", "Account created successfully!",
                    "data",    resp
                ));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "data",    resp
        ));
    }
}