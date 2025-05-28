package com.example.trackexpenses.dto;

import com.example.trackexpenses.entity.Role;
import lombok.Data;

@Data
public class LoginResponseDto {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String email;
    private Role role;

    public LoginResponseDto(String token, Integer id, String username, String email, Role role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}