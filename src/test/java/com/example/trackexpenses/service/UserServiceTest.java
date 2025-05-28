package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.UserDto;
import com.example.trackexpenses.dto.UserRegistrationDto;
import com.example.trackexpenses.entity.Role;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationDto registrationDto;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(now);

        
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("newuser");
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword("encodedPassword");
    }

    @Test
    void registerUser_ShouldCreateAndReturnUser() {
        
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1);
            return savedUser;
        });

        
        UserDto result = userService.registerUser(registrationDto);

        
        assertNotNull(result);
        assertEquals(registrationDto.getUsername(), result.getUsername());
        assertEquals(registrationDto.getEmail(), result.getEmail());
        assertEquals(Role.USER, result.getRole());
        assertTrue(result.getIsActive());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(registrationDto.getUsername(), savedUser.getUsername());
        assertEquals(registrationDto.getEmail(), savedUser.getEmail());
        assertEquals(registrationDto.getPassword(), savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertTrue(savedUser.getIsActive());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void registerUser_ShouldThrowExceptionWhenUsernameExists() {
        
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.of(testUser));

        
        assertThrows(RuntimeException.class, () -> userService.registerUser(registrationDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowExceptionWhenEmailExists() {
        
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.of(testUser));

        
        assertThrows(RuntimeException.class, () -> userService.registerUser(registrationDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_ShouldReturnUserWhenExists() {
        
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        
        Optional<UserDto> result = userService.findByUsername(testUser.getUsername());

        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        assertEquals(testUser.getRole(), result.get().getRole());
        assertEquals(testUser.getIsActive(), result.get().getIsActive());
        assertEquals(testUser.getCreatedAt(), result.get().getCreatedAt());
    }

    @Test
    void findByUsername_ShouldReturnEmptyWhenUserDoesNotExist() {
        
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        
        Optional<UserDto> result = userService.findByUsername("nonexistent");

        
        assertFalse(result.isPresent());
    }

    @Test
    void findById_ShouldReturnUserWhenExists() {
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        
        Optional<UserDto> result = userService.findById(testUser.getId());

        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        assertEquals(testUser.getRole(), result.get().getRole());
        assertEquals(testUser.getIsActive(), result.get().getIsActive());
        assertEquals(testUser.getCreatedAt(), result.get().getCreatedAt());
    }

    @Test
    void findById_ShouldReturnEmptyWhenUserDoesNotExist() {
        
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        
        Optional<UserDto> result = userService.findById(999);

        
        assertFalse(result.isPresent());
    }

    @Test
    void findAllUsers_ShouldReturnAllUsers() {
        
        User user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setRole(Role.USER);
        user2.setIsActive(true);
        user2.setCreatedAt(now);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        
        List<UserDto> result = userService.findAllUsers();

        
        assertEquals(2, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        assertEquals(user2.getId(), result.get(1).getId());
        assertEquals(user2.getUsername(), result.get(1).getUsername());
    }

    @Test
    void updateUserStatus_ShouldUpdateAndReturnUser() {
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        
        UserDto result = userService.updateUserStatus(testUser.getId(), false);

        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertFalse(result.getIsActive());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(testUser.getId(), savedUser.getId());
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        assertFalse(savedUser.getIsActive());
    }

    @Test
    void updateUserStatus_ShouldThrowExceptionWhenUserNotFound() {
        
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> userService.updateUserStatus(999, false));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUserWhenExists() {
        
        when(userRepository.existsById(testUser.getId())).thenReturn(true);
        doNothing().when(userRepository).deleteById(testUser.getId());

        
        userService.deleteUser(testUser.getId());

        
        verify(userRepository).deleteById(testUser.getId());
    }

    @Test
    void deleteUser_ShouldThrowExceptionWhenUserNotFound() {
        
        when(userRepository.existsById(999)).thenReturn(false);

        
        assertThrows(RuntimeException.class, () -> userService.deleteUser(999));
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUser() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        
        User result = userService.getCurrentUser();

        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getCurrentUser_ShouldReturnNullWhenNotAuthenticated() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        
        User result = userService.getCurrentUser();

        
        assertNull(result);
    }

    @Test
    void getCurrentUser_ShouldReturnNullWhenAnonymousUser() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        
        User result = userService.getCurrentUser();

        
        assertNull(result);
    }

    @Test
    void getUserById_ShouldReturnUserWhenIdProvided() {
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        
        User result = userService.getUserById(testUser.getId());

        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_ShouldReturnCurrentUserWhenIdNotFound() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        
        User result = userService.getUserById(999);

        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_ShouldReturnCurrentUserWhenIdIsNull() {
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testUser.getUsername());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        
        User result = userService.getUserById(null);

        
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }
}