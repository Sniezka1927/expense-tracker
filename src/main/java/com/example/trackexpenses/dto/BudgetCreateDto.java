package com.example.trackexpenses.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetCreateDto {
    private BigDecimal amount;
    private Integer categoryId;
    private Integer month;
    private Integer year;
}