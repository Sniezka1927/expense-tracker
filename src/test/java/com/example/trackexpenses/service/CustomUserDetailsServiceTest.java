package com.example.trackexpenses.service;

import com.example.trackexpenses.entity.Role;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.repository.UserRepository;
import com.example.trackexpenses.service.CustomUserDetailsService.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private final String testUsername = "testuser";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername(testUsername);
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails() {
        
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

        
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUsername);

        
        assertNotNull(userDetails);
        assertEquals(testUsername, userDetails.getUsername());
        assertEquals(testUser.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_ShouldThrowExceptionWhenUserNotFound() {
        
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        
        assertThrows(UsernameNotFoundException.class, () -> 
                userDetailsService.loadUserByUsername("nonexistent"));
    }

    @Test
    void userPrincipal_Create_ShouldCreateUserPrincipalFromUser() {
        
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        
        assertNotNull(userPrincipal);
        assertEquals(testUser.getId(), userPrincipal.getId());
        assertEquals(testUser.getUsername(), userPrincipal.getUsername());
        assertEquals(testUser.getEmail(), userPrincipal.getEmail());
        assertEquals(testUser.getPassword(), userPrincipal.getPassword());
        assertTrue(userPrincipal.isEnabled());
        assertTrue(userPrincipal.isAccountNonExpired());
        assertTrue(userPrincipal.isAccountNonLocked());
        assertTrue(userPrincipal.isCredentialsNonExpired());
        
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void userPrincipal_Create_ShouldCreateDisabledUserPrincipalForInactiveUser() {
        
        testUser.setIsActive(false);

        
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        
        assertNotNull(userPrincipal);
        assertFalse(userPrincipal.isEnabled());
    }

    @Test
    void userPrincipal_Create_ShouldCreateAdminUserPrincipal() {
        
        testUser.setRole(Role.ADMIN);

        
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        
        assertNotNull(userPrincipal);
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}