package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.UserDto;
import com.example.trackexpenses.dto.UserRegistrationDto;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        try {
            UserDto createdUser = userService.registerUser(registrationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID") @PathVariable Integer id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get user by username")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get current user")
    @GetMapping("/current")
    public ResponseEntity<UserDto> getCurrentUser() {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                UserDto userDto = userService.findById(currentUser.getId()).orElse(null);
                return ResponseEntity.ok(userDto);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Update user status")
    @PutMapping("/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Integer id,
            @Parameter(description = "Active status") @RequestParam Boolean isActive) {
        try {
            UserDto updatedUser = userService.updateUserStatus(id, isActive);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get user statistics")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.findAllUsers().size());
        stats.put("activeUsers", userService.findAllUsers().stream()
                .filter(user -> user.getIsActive()).count());
        return ResponseEntity.ok(stats);
    }
}