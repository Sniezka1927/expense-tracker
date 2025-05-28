package com.example.trackexpenses.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testUsername = "testuser";
    private final String testSecret = "testSecretKey12345678901234567890123456789012345678901234567890";
    private final int testExpirationMs = 60000; 

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        
        String token = jwtUtil.generateToken(testUsername);

        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals(testUsername, jwtUtil.getUsernameFromToken(token));
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        
        String token = jwtUtil.generateToken(testUsername);

        
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        
        String token = jwtUtil.generateToken(testUsername);

        
        boolean isValid = jwtUtil.validateToken(token);

        
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForMalformedToken() {
        
        String malformedToken = "malformed.token.value";

        
        boolean isValid = jwtUtil.validateToken(malformedToken);

        
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForEmptyToken() {
        
        String emptyToken = "";

        
        boolean isValid = jwtUtil.validateToken(emptyToken);

        
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalseForExpiredToken() throws Exception {
        
        
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 1); 
        String token = jwtUtil.generateToken(testUsername);
        
        
        Thread.sleep(10);

        
        boolean isValid = jwtUtil.validateToken(token);

        
        assertFalse(isValid);
    }
}