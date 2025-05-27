package com.example.trackexpenses.dto;

import lombok.Data;

@Data
public class CategoryDto {
    private Integer id;
    private String name;
    private String description;
    private String colorCode;
    private Boolean isDefault;
}