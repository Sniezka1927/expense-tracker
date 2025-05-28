package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setColorCode("#007bff");
        testCategory.setIsDefault(false);

        
        testCategoryDto = new CategoryDto();
        testCategoryDto.setId(1);
        testCategoryDto.setName("Test Category");
        testCategoryDto.setDescription("Test Description");
        testCategoryDto.setColorCode("#007bff");
        testCategoryDto.setIsDefault(false);
    }

    @Test
    void findAllCategories_ShouldReturnAllCategories() {
        
        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Category 2");
        category2.setDescription("Description 2");
        category2.setColorCode("#28a745");
        category2.setIsDefault(true);

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory, category2));

        
        List<CategoryDto> result = categoryService.findAllCategories();

        
        assertEquals(2, result.size());
        assertEquals(testCategory.getId(), result.get(0).getId());
        assertEquals(testCategory.getName(), result.get(0).getName());
        assertEquals(category2.getId(), result.get(1).getId());
        assertEquals(category2.getName(), result.get(1).getName());
    }

    @Test
    void findById_ShouldReturnCategoryWhenExists() {
        
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        
        Optional<CategoryDto> result = categoryService.findById(testCategory.getId());

        
        assertTrue(result.isPresent());
        assertEquals(testCategory.getId(), result.get().getId());
        assertEquals(testCategory.getName(), result.get().getName());
        assertEquals(testCategory.getDescription(), result.get().getDescription());
        assertEquals(testCategory.getColorCode(), result.get().getColorCode());
        assertEquals(testCategory.getIsDefault(), result.get().getIsDefault());
    }

    @Test
    void findById_ShouldReturnEmptyWhenCategoryDoesNotExist() {
        
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        
        Optional<CategoryDto> result = categoryService.findById(999);

        
        assertFalse(result.isPresent());
    }

    @Test
    void createCategory_ShouldCreateAndReturnCategory() {
        
        when(categoryRepository.findByName(testCategoryDto.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            savedCategory.setId(1);
            return savedCategory;
        });

        
        CategoryDto result = categoryService.createCategory(testCategoryDto);

        
        assertNotNull(result);
        assertEquals(testCategoryDto.getName(), result.getName());
        assertEquals(testCategoryDto.getDescription(), result.getDescription());
        assertEquals(testCategoryDto.getColorCode(), result.getColorCode());
        assertFalse(result.getIsDefault());

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertEquals(testCategoryDto.getName(), savedCategory.getName());
        assertEquals(testCategoryDto.getDescription(), savedCategory.getDescription());
        assertEquals(testCategoryDto.getColorCode(), savedCategory.getColorCode());
        assertFalse(savedCategory.getIsDefault());
    }

    @Test
    void createCategory_ShouldUseDefaultColorCodeWhenNull() {
        
        testCategoryDto.setColorCode(null);
        when(categoryRepository.findByName(testCategoryDto.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            savedCategory.setId(1);
            return savedCategory;
        });

        
        CategoryDto result = categoryService.createCategory(testCategoryDto);

        
        assertNotNull(result);
        assertEquals("#007bff", result.getColorCode());

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertEquals("#007bff", savedCategory.getColorCode());
    }

    @Test
    void createCategory_ShouldThrowExceptionWhenNameExists() {
        
        when(categoryRepository.findByName(testCategoryDto.getName())).thenReturn(Optional.of(testCategory));

        
        assertThrows(RuntimeException.class, () -> categoryService.createCategory(testCategoryDto));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldUpdateAndReturnCategory() {
        
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Updated Category");
        updateDto.setDescription("Updated Description");
        updateDto.setColorCode("#dc3545");

        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName(updateDto.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        
        CategoryDto result = categoryService.updateCategory(testCategory.getId(), updateDto);

        
        assertNotNull(result);
        assertEquals(updateDto.getName(), result.getName());
        assertEquals(updateDto.getDescription(), result.getDescription());
        assertEquals(updateDto.getColorCode(), result.getColorCode());

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertEquals(updateDto.getName(), savedCategory.getName());
        assertEquals(updateDto.getDescription(), savedCategory.getDescription());
        assertEquals(updateDto.getColorCode(), savedCategory.getColorCode());
    }

    @Test
    void updateCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(999, testCategoryDto));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowExceptionWhenNameExistsForDifferentCategory() {
        
        Category existingCategory = new Category();
        existingCategory.setId(2);
        existingCategory.setName("Existing Category");

        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Existing Category");

        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName(updateDto.getName())).thenReturn(Optional.of(existingCategory));

        
        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(testCategory.getId(), updateDto));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldAllowSameNameForSameCategory() {
        
        CategoryDto updateDto = new CategoryDto();
        updateDto.setName(testCategory.getName());
        updateDto.setDescription("Updated Description");
        updateDto.setColorCode("#dc3545");

        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByName(updateDto.getName())).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        
        CategoryDto result = categoryService.updateCategory(testCategory.getId(), updateDto);

        
        assertNotNull(result);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldDeleteCategoryWhenExists() {
        
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).deleteById(testCategory.getId());

        
        categoryService.deleteCategory(testCategory.getId());

        
        verify(categoryRepository).deleteById(testCategory.getId());
    }

    @Test
    void deleteCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(999));
        verify(categoryRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteCategory_ShouldThrowExceptionWhenDeletingDefaultCategory() {
        
        testCategory.setIsDefault(true);
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(testCategory.getId()));
        verify(categoryRepository, never()).deleteById(anyInt());
    }

    @Test
    void initializeDefaultCategories_ShouldCreateDefaultCategoriesWhenNotExist() {
        
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            savedCategory.setId(1);
            return savedCategory;
        });

        
        categoryService.initializeDefaultCategories();

        
        verify(categoryRepository, times(6)).save(any(Category.class));
    }

    @Test
    void initializeDefaultCategories_ShouldNotCreateCategoriesWhenAlreadyExist() {
        
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(testCategory));

        
        categoryService.initializeDefaultCategories();

        
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void findCategoryById_ShouldReturnCategoryWhenExists() {
        
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        
        Category result = categoryService.findCategoryById(testCategory.getId());

        
        assertNotNull(result);
        assertEquals(testCategory.getId(), result.getId());
        assertEquals(testCategory.getName(), result.getName());
    }

    @Test
    void findCategoryById_ShouldThrowExceptionWhenCategoryNotFound() {
        
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> categoryService.findCategoryById(999));
    }
}