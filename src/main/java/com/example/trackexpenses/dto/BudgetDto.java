package com.example.trackexpenses.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BudgetDto {
    private Integer id;
    private BigDecimal amount;
    private CategoryDto category;
    private Integer month;
    private Integer year;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
