package com.example.trackexpenses.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseDto {
    private Integer id;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private CategoryDto category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
