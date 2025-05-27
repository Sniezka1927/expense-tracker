package com.example.trackexpenses.dto;

import com.example.trackexpenses.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Integer id;
    private String username;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
