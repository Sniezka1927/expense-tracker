package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.LoginDto;
import com.example.trackexpenses.dto.LoginResponseDto;
import com.example.trackexpenses.dto.UserRegistrationDto;
import com.example.trackexpenses.entity.Role;
import com.example.trackexpenses.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    private LoginDto loginDto;
    private UserRegistrationDto registrationDto;
    private LoginResponseDto loginResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        loginDto = new LoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("password");

        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("newuser");
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword("password");

        loginResponseDto = new LoginResponseDto("test.jwt.token", 1, "testuser", "testuser@example.com", Role.USER);
    }

    @Test
    void login_ShouldReturnLoginResponseWhenSuccessful() throws Exception {
        when(authService.login(any(LoginDto.class))).thenReturn(loginResponseDto);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(loginResponseDto.getToken()))
                .andExpect(jsonPath("$.id").value(loginResponseDto.getId()))
                .andExpect(jsonPath("$.username").value(loginResponseDto.getUsername()))
                .andExpect(jsonPath("$.email").value(loginResponseDto.getEmail()))
                .andExpect(jsonPath("$.role").value(loginResponseDto.getRole().toString()));
    }

    @Test
    void login_ShouldReturnBadRequestWhenAuthServiceThrowsException() throws Exception {
        when(authService.login(any(LoginDto.class))).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturnLoginResponseWhenSuccessful() throws Exception {
        when(authService.register(any(UserRegistrationDto.class))).thenReturn(loginResponseDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(loginResponseDto.getToken()))
                .andExpect(jsonPath("$.id").value(loginResponseDto.getId()))
                .andExpect(jsonPath("$.username").value(loginResponseDto.getUsername()))
                .andExpect(jsonPath("$.email").value(loginResponseDto.getEmail()))
                .andExpect(jsonPath("$.role").value(loginResponseDto.getRole().toString()));
    }

    @Test
    void register_ShouldReturnBadRequestWhenAuthServiceThrowsException() throws Exception {
        when(authService.register(any(UserRegistrationDto.class))).thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }
}
