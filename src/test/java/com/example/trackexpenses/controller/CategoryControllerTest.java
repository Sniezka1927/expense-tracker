package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    private CategoryDto testCategory;
    private CategoryDto defaultCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();

        testCategory = new CategoryDto();
        testCategory.setId(1);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setColorCode("#007bff");
        testCategory.setIsDefault(false);

        defaultCategory = new CategoryDto();
        defaultCategory.setId(2);
        defaultCategory.setName("Default Category");
        defaultCategory.setDescription("Default Description");
        defaultCategory.setColorCode("#28a745");
        defaultCategory.setIsDefault(true);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        List<CategoryDto> categories = Arrays.asList(testCategory, defaultCategory);
        when(categoryService.findAllCategories()).thenReturn(categories);

        
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(testCategory.getId())))
                .andExpect(jsonPath("$[0].name", is(testCategory.getName())))
                .andExpect(jsonPath("$[1].id", is(defaultCategory.getId())))
                .andExpect(jsonPath("$[1].name", is(defaultCategory.getName())));
    }

    @Test
    void getCategoryById_ShouldReturnCategoryWhenExists() throws Exception {
        when(categoryService.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        
        mockMvc.perform(get("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCategory.getId())))
                .andExpect(jsonPath("$.name", is(testCategory.getName())))
                .andExpect(jsonPath("$.description", is(testCategory.getDescription())))
                .andExpect(jsonPath("$.colorCode", is(testCategory.getColorCode())))
                .andExpect(jsonPath("$.isDefault", is(testCategory.getIsDefault())));
    }

    @Test
    void getCategoryById_ShouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(categoryService.findById(999)).thenReturn(Optional.empty());

        
        mockMvc.perform(get("/api/categories/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_ShouldCreateAndReturnCategory() throws Exception {
        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(testCategory);

        
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(testCategory.getId())))
                .andExpect(jsonPath("$.name", is(testCategory.getName())));
    }

    @Test
    void createCategory_ShouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        when(categoryService.createCategory(any(CategoryDto.class))).thenThrow(new RuntimeException("Category name already exists"));

        
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_ShouldUpdateAndReturnCategory() throws Exception {
        when(categoryService.updateCategory(eq(testCategory.getId()), any(CategoryDto.class))).thenReturn(testCategory);

        
        mockMvc.perform(put("/api/categories/{id}", testCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCategory.getId())))
                .andExpect(jsonPath("$.name", is(testCategory.getName())));
    }

    @Test
    void updateCategory_ShouldReturnNotFoundWhenServiceThrowsException() throws Exception {
        when(categoryService.updateCategory(eq(999), any(CategoryDto.class))).thenThrow(new RuntimeException("Category not found"));

        
        mockMvc.perform(put("/api/categories/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_ShouldDeleteCategory() throws Exception {
        doNothing().when(categoryService).deleteCategory(testCategory.getId());

        
        mockMvc.perform(delete("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(testCategory.getId());
    }

    @Test
    void deleteCategory_ShouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        doThrow(new RuntimeException("Cannot delete default category")).when(categoryService).deleteCategory(defaultCategory.getId());

        
        mockMvc.perform(delete("/api/categories/{id}", defaultCategory.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDefaultCategories_ShouldReturnOnlyDefaultCategories() throws Exception {
        List<CategoryDto> allCategories = Arrays.asList(testCategory, defaultCategory);
        when(categoryService.findAllCategories()).thenReturn(allCategories);

        
        mockMvc.perform(get("/api/categories/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(defaultCategory.getId())))
                .andExpect(jsonPath("$[0].name", is(defaultCategory.getName())))
                .andExpect(jsonPath("$[0].isDefault", is(true)));
    }

    @Test
    void getCustomCategories_ShouldReturnOnlyCustomCategories() throws Exception {
        List<CategoryDto> allCategories = Arrays.asList(testCategory, defaultCategory);
        when(categoryService.findAllCategories()).thenReturn(allCategories);

        
        mockMvc.perform(get("/api/categories/custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testCategory.getId())))
                .andExpect(jsonPath("$[0].name", is(testCategory.getName())))
                .andExpect(jsonPath("$[0].isDefault", is(false)));
    }

    @Test
    void getCategoryStats_ShouldReturnCategoryStatistics() throws Exception {
        List<CategoryDto> allCategories = Arrays.asList(testCategory, defaultCategory);
        when(categoryService.findAllCategories()).thenReturn(allCategories);

        
        mockMvc.perform(get("/api/categories/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCategories", is(2)))
                .andExpect(jsonPath("$.defaultCategories", is(1)))
                .andExpect(jsonPath("$.customCategories", is(1)));
    }

    @Test
    void initializeDefaultCategories_ShouldInitializeCategories() throws Exception {
        doNothing().when(categoryService).initializeDefaultCategories();

        
        mockMvc.perform(post("/api/categories/initialize-defaults"))
                .andExpect(status().isOk());

        verify(categoryService).initializeDefaultCategories();
    }
}