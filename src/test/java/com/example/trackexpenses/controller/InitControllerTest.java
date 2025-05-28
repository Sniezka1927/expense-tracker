package com.example.trackexpenses.controller;

import com.example.trackexpenses.entity.Budget;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.entity.Expense;
import com.example.trackexpenses.entity.Role;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.repository.BudgetRepository;
import com.example.trackexpenses.repository.CategoryRepository;
import com.example.trackexpenses.repository.ExpenseRepository;
import com.example.trackexpenses.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InitControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InitController initController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(initController).build();
    }

    @Test
    void initializeAllData_ShouldReturnSuccessResponse() throws Exception {
        
        doNothing().when(expenseRepository).deleteAll();
        doNothing().when(budgetRepository).deleteAll();
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category());
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense());
        when(budgetRepository.save(any(Budget.class))).thenReturn(new Budget());
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(any(), any(), any(), any())).thenReturn(Optional.empty());

        
        mockMvc.perform(post("/api/init/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.categories").value("✅ Categories initialized"))
                .andExpect(jsonPath("$.users").value("✅ Users initialized"))
                .andExpect(jsonPath("$.expenses").value("✅ Sample expenses created"))
                .andExpect(jsonPath("$.budgets").value("✅ Sample budgets created"))
                .andExpect(jsonPath("$.message").value("All mock data initialized successfully!"));
    }

    @Test
    void initializeAllData_ShouldReturnErrorResponseWhenExceptionOccurs() throws Exception {
        
        when(categoryRepository.findByName(anyString())).thenThrow(new RuntimeException("Test exception"));

        
        mockMvc.perform(post("/api/init/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Error initializing data")));
    }

    @Test
    void initializeCategories_ShouldReturnSuccessResponse() throws Exception {
        
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

        
        mockMvc.perform(post("/api/init/categories"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Categories initialized")));

        
        verify(categoryRepository, times(8)).save(any(Category.class));
    }

    @Test
    void initializeUsers_ShouldReturnSuccessResponse() throws Exception {
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        
        mockMvc.perform(post("/api/init/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Users initialized: 3 new users created")));

        
        verify(userRepository, times(3)).save(any(User.class));
    }

    @Test
    void initializeExpenses_ShouldReturnSuccessResponse() throws Exception {
        
        User testUser = new User();
        testUser.setUsername("testuser");

        Category foodCategory = new Category();
        foodCategory.setName("Food");

        Category transportCategory = new Category();
        transportCategory.setName("Transportation");

        Category entertainmentCategory = new Category();
        entertainmentCategory.setName("Entertainment");

        Category billsCategory = new Category();
        billsCategory.setName("Bills");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(foodCategory));
        when(categoryRepository.findByName("Transportation")).thenReturn(Optional.of(transportCategory));
        when(categoryRepository.findByName("Entertainment")).thenReturn(Optional.of(entertainmentCategory));
        when(categoryRepository.findByName("Bills")).thenReturn(Optional.of(billsCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(new Expense());

        
        mockMvc.perform(post("/api/init/expenses"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sample expenses created")));

        
        verify(expenseRepository, times(14)).save(any(Expense.class));
    }

    @Test
    void initializeExpenses_ShouldReturnBadRequestWhenUserNotFound() throws Exception {
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        
        mockMvc.perform(post("/api/init/expenses"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Test user not found")));

        
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void initializeExpenses_ShouldReturnBadRequestWhenCategoriesNotFound() throws Exception {
        
        User testUser = new User();
        testUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.empty());

        
        mockMvc.perform(post("/api/init/expenses"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Categories not found")));

        
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void initializeBudgets_ShouldReturnSuccessResponse() throws Exception {
        
        User testUser = new User();
        testUser.setUsername("testuser");

        Category foodCategory = new Category();
        foodCategory.setName("Food");

        Category transportCategory = new Category();
        transportCategory.setName("Transportation");

        Category entertainmentCategory = new Category();
        entertainmentCategory.setName("Entertainment");

        Category billsCategory = new Category();
        billsCategory.setName("Bills");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(foodCategory));
        when(categoryRepository.findByName("Transportation")).thenReturn(Optional.of(transportCategory));
        when(categoryRepository.findByName("Entertainment")).thenReturn(Optional.of(entertainmentCategory));
        when(categoryRepository.findByName("Bills")).thenReturn(Optional.of(billsCategory));
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(new Budget());

        
        mockMvc.perform(post("/api/init/budgets"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sample budgets created")));

        
        verify(budgetRepository, times(6)).save(any(Budget.class));
    }

    @Test
    void initializeBudgets_ShouldReturnBadRequestWhenUserNotFound() throws Exception {
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        
        mockMvc.perform(post("/api/init/budgets"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Test user not found")));

        
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void initializeBudgets_ShouldReturnBadRequestWhenCategoriesNotFound() throws Exception {
        
        User testUser = new User();
        testUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByName("Food")).thenReturn(Optional.empty());

        
        mockMvc.perform(post("/api/init/budgets"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Categories not found")));

        
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void clearAllData_ShouldReturnSuccessResponse() throws Exception {
        
        doNothing().when(expenseRepository).deleteAll();
        doNothing().when(budgetRepository).deleteAll();

        
        mockMvc.perform(post("/api/init/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("All expenses and budgets cleared successfully")));

        
        verify(expenseRepository).deleteAll();
        verify(budgetRepository).deleteAll();
    }

    @Test
    void clearAllData_ShouldReturnErrorResponseWhenExceptionOccurs() throws Exception {
        
        doThrow(new RuntimeException("Test exception")).when(expenseRepository).deleteAll();

        
        mockMvc.perform(post("/api/init/clear"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error clearing data")));
    }

    @Test
    void getDataStatus_ShouldReturnStatusWithCounts() throws Exception {
        
        when(userRepository.count()).thenReturn(3L);
        when(categoryRepository.count()).thenReturn(8L);
        when(expenseRepository.count()).thenReturn(14L);
        when(budgetRepository.count()).thenReturn(6L);

        
        mockMvc.perform(get("/api/init/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").value(3))
                .andExpect(jsonPath("$.categories").value(8))
                .andExpect(jsonPath("$.expenses").value(14))
                .andExpect(jsonPath("$.budgets").value(6));
    }
}
