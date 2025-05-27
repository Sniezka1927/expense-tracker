package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Integer id) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(category))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        // TODO: Add admin authorization check
        try {
            CategoryDto createdCategory = categoryService.createCategory(categoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Integer id, @RequestBody CategoryDto categoryDto) {
        // TODO: Add admin authorization check
        try {
            CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        // TODO: Add admin authorization check
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/initialize-defaults")
    public ResponseEntity<Void> initializeDefaultCategories() {
        // TODO: Add admin authorization check
        categoryService.initializeDefaultCategories();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/default")
    public ResponseEntity<List<CategoryDto>> getDefaultCategories() {
        List<CategoryDto> allCategories = categoryService.findAllCategories();
        List<CategoryDto> defaultCategories = allCategories.stream()
                .filter(CategoryDto::getIsDefault)
                .collect(Collectors.toList());
        return ResponseEntity.ok(defaultCategories);
    }

    @GetMapping("/custom")
    public ResponseEntity<List<CategoryDto>> getCustomCategories() {
        List<CategoryDto> allCategories = categoryService.findAllCategories();
        List<CategoryDto> customCategories = allCategories.stream()
                .filter(cat -> !cat.getIsDefault())
                .collect(Collectors.toList());
        return ResponseEntity.ok(customCategories);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats() {
        Map<String, Object> stats = new HashMap<>();
        List<CategoryDto> allCategories = categoryService.findAllCategories();

        stats.put("totalCategories", allCategories.size());
        stats.put("defaultCategories", allCategories.stream().filter(CategoryDto::getIsDefault).count());
        stats.put("customCategories", allCategories.stream().filter(cat -> !cat.getIsDefault()).count());

        return ResponseEntity.ok(stats);
    }
}
