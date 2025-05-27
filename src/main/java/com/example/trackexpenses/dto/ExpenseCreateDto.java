package com.example.trackexpenses.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseCreateDto {
    private BigDecimal amount;
    private String description;
    private LocalDate date;
    private Integer categoryId;
}
