package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.LoginDto;
import com.example.trackexpenses.dto.LoginResponseDto;
import com.example.trackexpenses.dto.UserRegistrationDto;
import com.example.trackexpenses.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication and authorization - No authentication required")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User login", description = "Login with username and password to get JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        try {
            LoginResponseDto response = authService.login(loginDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "User registration", description = "Register new user account")
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(@RequestBody UserRegistrationDto registrationDto) {
        try {
            LoginResponseDto response = authService.register(registrationDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}