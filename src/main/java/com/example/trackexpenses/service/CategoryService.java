package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> findAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CategoryDto> findById(Integer id) {
        return categoryRepository.findById(id)
                .map(this::convertToDto);
    }

    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new RuntimeException("Category name already exists");
        }

        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setColorCode(categoryDto.getColorCode() != null ? categoryDto.getColorCode() : "#007bff");
        category.setIsDefault(false);

        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    public CategoryDto updateCategory(Integer id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Optional<Category> existingCategory = categoryRepository.findByName(categoryDto.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new RuntimeException("Category name already exists");
        }

        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setColorCode(categoryDto.getColorCode());

        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getIsDefault()) {
            throw new RuntimeException("Cannot delete default category");
        }

        categoryRepository.deleteById(id);
    }

    public void initializeDefaultCategories() {
        List<String[]> defaultCategories = Arrays.asList(
                new String[]{"Food", "Meals, groceries, dining out", "#28a745"},
                new String[]{"Transportation", "Gas, public transport, car maintenance", "#007bff"},
                new String[]{"Entertainment", "Movies, games, hobbies", "#ffc107"},
                new String[]{"Healthcare", "Medical expenses, pharmacy", "#dc3545"},
                new String[]{"Shopping", "Clothes, electronics, misc items", "#6f42c1"},
                new String[]{"Bills", "Utilities, rent, subscriptions", "#fd7e14"}
        );

        for (String[] categoryData : defaultCategories) {
            String name = categoryData[0];
            if (categoryRepository.findByName(name).isEmpty()) {
                Category category = new Category();
                category.setName(name);
                category.setDescription(categoryData[1]);
                category.setColorCode(categoryData[2]);
                category.setIsDefault(true);
                categoryRepository.save(category);
            }
        }
    }

    @Transactional(readOnly = true)
    public Category findCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setColorCode(category.getColorCode());
        dto.setIsDefault(category.getIsDefault());
        return dto;
    }
}