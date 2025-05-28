package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.UserDto;
import com.example.trackexpenses.entity.Role;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDto testUserDto;
    private User testUser;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(now);

        
        testUserDto = new UserDto();
        testUserDto.setId(1);
        testUserDto.setUsername("testuser");
        testUserDto.setEmail("testuser@example.com");
        testUserDto.setRole(Role.USER);
        testUserDto.setIsActive(true);
        testUserDto.setCreatedAt(now);
    }

    private void setupAdminSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupUserSecurityContext() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        
        setupAdminSecurityContext();

        
        UserDto user2 = new UserDto();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRole(Role.USER);
        user2.setIsActive(true);

        List<UserDto> users = Arrays.asList(testUserDto, user2);
        when(userService.findAllUsers()).thenReturn(users);

        
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(testUserDto.getId())))
                .andExpect(jsonPath("$[0].username", is(testUserDto.getUsername())))
                .andExpect(jsonPath("$[1].id", is(user2.getId())))
                .andExpect(jsonPath("$[1].username", is(user2.getUsername())));
    }

    @Test
    void getUserById_ShouldReturnUserWhenExists() throws Exception {
        
        setupAdminSecurityContext();

        
        when(userService.findById(testUserDto.getId())).thenReturn(Optional.of(testUserDto));

        
        mockMvc.perform(get("/api/users/{id}", testUserDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUserDto.getId())))
                .andExpect(jsonPath("$.username", is(testUserDto.getUsername())))
                .andExpect(jsonPath("$.email", is(testUserDto.getEmail())))
                .andExpect(jsonPath("$.role", is(testUserDto.getRole().toString())));
    }

    @Test
    void getUserById_ShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        
        setupAdminSecurityContext();

        
        when(userService.findById(999)).thenReturn(Optional.empty());

        
        mockMvc.perform(get("/api/users/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_ShouldReturnUserWhenExists() throws Exception {
        
        setupAdminSecurityContext();

        
        when(userService.findByUsername(testUserDto.getUsername())).thenReturn(Optional.of(testUserDto));

        
        mockMvc.perform(get("/api/users/username/{username}", testUserDto.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUserDto.getId())))
                .andExpect(jsonPath("$.username", is(testUserDto.getUsername())))
                .andExpect(jsonPath("$.email", is(testUserDto.getEmail())));
    }

    @Test
    void getUserByUsername_ShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        
        setupAdminSecurityContext();

        
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        
        mockMvc.perform(get("/api/users/username/{username}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUserProfile_ShouldReturnCurrentUserProfile() throws Exception {
        
        setupUserSecurityContext();

        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userService.findById(testUser.getId())).thenReturn(Optional.of(testUserDto));

        
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUserDto.getId())))
                .andExpect(jsonPath("$.username", is(testUserDto.getUsername())))
                .andExpect(jsonPath("$.email", is(testUserDto.getEmail())));
    }

    @Test
    void getCurrentUserProfile_ShouldReturnNotFoundWhenNoCurrentUser() throws Exception {
        
        setupUserSecurityContext();

        
        when(userService.getCurrentUser()).thenReturn(null);

        
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUserProfile_ShouldReturnServerErrorWhenExceptionOccurs() throws Exception {
        
        setupUserSecurityContext();

        
        when(userService.getCurrentUser()).thenThrow(new RuntimeException("Test exception"));

        
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateUserStatus_ShouldUpdateAndReturnUser() throws Exception {
        
        setupAdminSecurityContext();

        
        testUserDto.setIsActive(false);
        when(userService.updateUserStatus(testUserDto.getId(), false)).thenReturn(testUserDto);

        
        mockMvc.perform(put("/api/users/{id}/status", testUserDto.getId())
                .param("isActive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUserDto.getId())))
                .andExpect(jsonPath("$.username", is(testUserDto.getUsername())))
                .andExpect(jsonPath("$.isActive", is(false)));
    }

    @Test
    void updateUserStatus_ShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        
        setupAdminSecurityContext();

        
        when(userService.updateUserStatus(999, false)).thenThrow(new RuntimeException("User not found"));

        
        mockMvc.perform(put("/api/users/{id}/status", 999)
                .param("isActive", "false"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldDeleteUser() throws Exception {
        
        setupAdminSecurityContext();

        
        doNothing().when(userService).deleteUser(testUserDto.getId());

        
        mockMvc.perform(delete("/api/users/{id}", testUserDto.getId()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(testUserDto.getId());
    }

    @Test
    void deleteUser_ShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        
        setupAdminSecurityContext();

        
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(999);

        
        mockMvc.perform(delete("/api/users/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserStats_ShouldReturnUserStatistics() throws Exception {
        
        setupAdminSecurityContext();

        
        UserDto user2 = new UserDto();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRole(Role.USER);
        user2.setIsActive(false);

        List<UserDto> users = Arrays.asList(testUserDto, user2);
        when(userService.findAllUsers()).thenReturn(users);

        
        mockMvc.perform(get("/api/users/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", is(2)))
                .andExpect(jsonPath("$.activeUsers", is(1)));
    }
}
