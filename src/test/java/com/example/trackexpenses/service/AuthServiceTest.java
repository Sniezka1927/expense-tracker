package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.LoginDto;
import com.example.trackexpenses.dto.LoginResponseDto;
import com.example.trackexpenses.dto.UserDto;
import com.example.trackexpenses.dto.UserRegistrationDto;
import com.example.trackexpenses.entity.Role;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.repository.UserRepository;
import com.example.trackexpenses.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private LoginDto loginDto;
    private UserRegistrationDto registrationDto;
    private User user;
    private UserDto userDto;
    private final String testToken = "test.jwt.token";

    @BeforeEach
    void setUp() {
        
        loginDto = new LoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("password");

        
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("newuser");
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword("password");

        
        user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        
        userDto = new UserDto();
        userDto.setId(1);
        userDto.setUsername("newuser");
        userDto.setEmail("newuser@example.com");
        userDto.setRole(Role.USER);
    }

    @Test
    void login_ShouldReturnLoginResponseWithToken() {
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.of(user));

        
        LoginResponseDto response = authService.login(loginDto);

        
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole(), response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(loginDto.getUsername());
        verify(userRepository).findByUsername(loginDto.getUsername());
    }

    @Test
    void login_ShouldThrowExceptionWhenUserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(anyString())).thenReturn(testToken);
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.empty());


        assertThrows(RuntimeException.class, () -> authService.login(loginDto));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(loginDto.getUsername());
        verify(userRepository).findByUsername(loginDto.getUsername());
    }

    @Test
    void register_ShouldReturnLoginResponseWithToken() {
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(userDto);
        when(jwtUtil.generateToken(userDto.getUsername())).thenReturn(testToken);

        LoginResponseDto response = authService.register(registrationDto);

        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(userDto.getId(), response.getId());
        assertEquals(userDto.getUsername(), response.getUsername());
        assertEquals(userDto.getEmail(), response.getEmail());
        assertEquals(userDto.getRole(), response.getRole());

        verify(userRepository).findByUsername(registrationDto.getUsername());
        verify(userRepository).findByEmail(registrationDto.getEmail());
        verify(passwordEncoder).encode("password");
        verify(userService).registerUser(any(UserRegistrationDto.class));
        verify(jwtUtil).generateToken(userDto.getUsername());
    }

    @Test
    void register_ShouldThrowExceptionWhenUsernameExists() {
        
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.of(user));

        
        assertThrows(RuntimeException.class, () -> authService.register(registrationDto));

        verify(userRepository).findByUsername(registrationDto.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void register_ShouldThrowExceptionWhenEmailExists() {
        
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.of(user));

        
        assertThrows(RuntimeException.class, () -> authService.register(registrationDto));

        verify(userRepository).findByUsername(registrationDto.getUsername());
        verify(userRepository).findByEmail(registrationDto.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).registerUser(any(UserRegistrationDto.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }
}
