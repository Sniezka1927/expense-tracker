package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Categories", description = "Category management")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all categories")
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Integer id) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new category")
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto createdCategory = categoryService.createCategory(categoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update category")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Integer id,
            @RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get default categories")
    @GetMapping("/default")
    public ResponseEntity<List<CategoryDto>> getDefaultCategories() {
        List<CategoryDto> allCategories = categoryService.findAllCategories();
        List<CategoryDto> defaultCategories = allCategories.stream()
                .filter(CategoryDto::getIsDefault)
                .collect(Collectors.toList());
        return ResponseEntity.ok(defaultCategories);
    }

    @Operation(summary = "Get custom categories")
    @GetMapping("/custom")
    public ResponseEntity<List<CategoryDto>> getCustomCategories() {
        List<CategoryDto> allCategories = categoryService.findAllCategories();
        List<CategoryDto> customCategories = allCategories.stream()
                .filter(cat -> !cat.getIsDefault())
                .collect(Collectors.toList());
        return ResponseEntity.ok(customCategories);
    }

    @Operation(summary = "Get category statistics")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats() {
        Map<String, Object> stats = new HashMap<>();
        List<CategoryDto> allCategories = categoryService.findAllCategories();

        stats.put("totalCategories", allCategories.size());
        stats.put("defaultCategories", allCategories.stream().filter(CategoryDto::getIsDefault).count());
        stats.put("customCategories", allCategories.stream().filter(cat -> !cat.getIsDefault()).count());

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Initialize default categories")
    @PostMapping("/initialize-defaults")
    public ResponseEntity<Void> initializeDefaultCategories() {
        categoryService.initializeDefaultCategories();
        return ResponseEntity.ok().build();
    }
}