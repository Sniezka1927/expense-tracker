package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.BudgetCreateDto;
import com.example.trackexpenses.dto.BudgetDto;
import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.service.BudgetService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BudgetController budgetController;

    @Mock
    private BudgetService budgetService;

    private BudgetDto testBudget;
    private BudgetCreateDto budgetCreateDto;
    private CategoryDto testCategory;
    private final int testYear = 2023;
    private final int testMonth = 5;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();

        testCategory = new CategoryDto();
        testCategory.setId(1);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setColorCode("#007bff");
        testCategory.setIsDefault(false);

        testBudget = new BudgetDto();
        testBudget.setId(1);
        testBudget.setAmount(new BigDecimal("1000.00"));
        testBudget.setCategory(testCategory);
        testBudget.setMonth(testMonth);
        testBudget.setYear(testYear);
        testBudget.setCreatedAt(LocalDateTime.now());

        budgetCreateDto = new BudgetCreateDto();
        budgetCreateDto.setAmount(new BigDecimal("1000.00"));
        budgetCreateDto.setCategoryId(1);
        budgetCreateDto.setMonth(testMonth);
        budgetCreateDto.setYear(testYear);
    }

    @Test
    void getAllBudgets_ShouldReturnAllBudgetsForCurrentUser() throws Exception {
        
        List<BudgetDto> budgets = Collections.singletonList(testBudget);
        when(budgetService.findBudgetsByCurrentUser()).thenReturn(budgets);

        
        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testBudget.getId())))
                .andExpect(jsonPath("$[0].amount", is(testBudget.getAmount().doubleValue())))
                .andExpect(jsonPath("$[0].category.id", is(testCategory.getId())))
                .andExpect(jsonPath("$[0].month", is(testBudget.getMonth())))
                .andExpect(jsonPath("$[0].year", is(testBudget.getYear())));
    }

    @Test
    void getAllBudgetsAdmin_ShouldReturnAllBudgets() throws Exception {
        
        List<BudgetDto> budgets = Collections.singletonList(testBudget);
        when(budgetService.findAllBudgets()).thenReturn(budgets);

        
        mockMvc.perform(get("/api/budgets/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testBudget.getId())))
                .andExpect(jsonPath("$[0].amount", is(testBudget.getAmount().doubleValue())));
    }

    @Test
    void getBudgetById_ShouldReturnBudgetWhenExists() throws Exception {
        
        when(budgetService.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));

        
        mockMvc.perform(get("/api/budgets/{id}", testBudget.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBudget.getId())))
                .andExpect(jsonPath("$.amount", is(testBudget.getAmount().doubleValue())))
                .andExpect(jsonPath("$.category.id", is(testCategory.getId())))
                .andExpect(jsonPath("$.month", is(testBudget.getMonth())))
                .andExpect(jsonPath("$.year", is(testBudget.getYear())));
    }

    @Test
    void getBudgetById_ShouldReturnNotFoundWhenBudgetDoesNotExist() throws Exception {
        
        when(budgetService.findById(999)).thenReturn(Optional.empty());

        
        mockMvc.perform(get("/api/budgets/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBudget_ShouldCreateAndReturnBudget() throws Exception {
        
        when(budgetService.createBudget(any(BudgetCreateDto.class))).thenReturn(testBudget);

        
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(testBudget.getId())))
                .andExpect(jsonPath("$.amount", is(testBudget.getAmount().doubleValue())));
    }

    @Test
    void createBudget_ShouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        
        when(budgetService.createBudget(any(BudgetCreateDto.class))).thenThrow(new RuntimeException("Budget already exists"));

        
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBudget_ShouldUpdateAndReturnBudget() throws Exception {
        
        when(budgetService.updateBudget(eq(testBudget.getId()), any(BudgetCreateDto.class))).thenReturn(testBudget);

        
        mockMvc.perform(put("/api/budgets/{id}", testBudget.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testBudget.getId())))
                .andExpect(jsonPath("$.amount", is(testBudget.getAmount().doubleValue())));
    }

    @Test
    void updateBudget_ShouldReturnNotFoundWhenServiceThrowsException() throws Exception {
        
        when(budgetService.updateBudget(eq(999), any(BudgetCreateDto.class))).thenThrow(new RuntimeException("Budget not found"));

        
        mockMvc.perform(put("/api/budgets/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budgetCreateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBudget_ShouldDeleteBudget() throws Exception {
        
        doNothing().when(budgetService).deleteBudget(testBudget.getId());

        
        mockMvc.perform(delete("/api/budgets/{id}", testBudget.getId()))
                .andExpect(status().isNoContent());

        verify(budgetService).deleteBudget(testBudget.getId());
    }

    @Test
    void deleteBudget_ShouldReturnNotFoundWhenServiceThrowsException() throws Exception {
        
        doThrow(new RuntimeException("Budget not found")).when(budgetService).deleteBudget(999);

        
        mockMvc.perform(delete("/api/budgets/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentMonthBudgets_ShouldReturnCurrentMonthBudgets() throws Exception {
        
        LocalDate now = LocalDate.now();
        List<BudgetDto> budgets = Collections.singletonList(testBudget);
        when(budgetService.findBudgetsByYearAndMonth(now.getYear(), now.getMonthValue())).thenReturn(budgets);

        
        mockMvc.perform(get("/api/budgets/current-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testBudget.getId())));
    }

    @Test
    void getBudgetsByYear_ShouldReturnBudgetsForYear() throws Exception {
        
        List<BudgetDto> budgets = Collections.singletonList(testBudget);
        when(budgetService.findBudgetsByYear(testYear)).thenReturn(budgets);

        
        mockMvc.perform(get("/api/budgets/year/{year}", testYear))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testBudget.getId())))
                .andExpect(jsonPath("$[0].year", is(testYear)));
    }

    @Test
    void getBudgetsByYearAndMonth_ShouldReturnBudgetsForYearAndMonth() throws Exception {
        
        List<BudgetDto> budgets = Collections.singletonList(testBudget);
        when(budgetService.findBudgetsByYearAndMonth(testYear, testMonth)).thenReturn(budgets);

        
        mockMvc.perform(get("/api/budgets/period/{year}/{month}", testYear, testMonth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testBudget.getId())))
                .andExpect(jsonPath("$[0].year", is(testYear)))
                .andExpect(jsonPath("$[0].month", is(testMonth)));
    }

    @Test
    void getTotalBudgetForMonth_ShouldReturnTotalBudget() throws Exception {
        
        BigDecimal total = new BigDecimal("1500.00");
        when(budgetService.getTotalBudgetForMonth(testYear, testMonth)).thenReturn(total);

        
        mockMvc.perform(get("/api/budgets/total/{year}/{month}", testYear, testMonth))
                .andExpect(status().isOk())
                .andExpect(content().string(total.toString()));
    }

    @Test
    void getSpentAmountForBudget_ShouldReturnSpentAmount() throws Exception {
        
        BigDecimal spent = new BigDecimal("500.00");
        when(budgetService.getSpentAmountForBudget(testBudget.getId())).thenReturn(spent);

        
        mockMvc.perform(get("/api/budgets/{id}/spent", testBudget.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(spent.toString()));
    }

    @Test
    void getRemainingBudgetForCategory_ShouldReturnRemainingBudget() throws Exception {
        
        BigDecimal remaining = new BigDecimal("500.00");
        when(budgetService.getRemainingBudgetForCategory(testCategory.getId(), testYear, testMonth)).thenReturn(remaining);

        
        mockMvc.perform(get("/api/budgets/remaining/{categoryId}/{year}/{month}", testCategory.getId(), testYear, testMonth))
                .andExpect(status().isOk())
                .andExpect(content().string(remaining.toString()));
    }

    @Test
    void getBudgetStats_ShouldReturnBudgetStatistics() throws Exception {
        
        List<BudgetDto> budgets = Arrays.asList(testBudget, testBudget);
        BigDecimal currentMonthBudget = new BigDecimal("2000.00");
        
        when(budgetService.findBudgetsByCurrentUser()).thenReturn(budgets);
        LocalDate now = LocalDate.now();
        when(budgetService.getTotalBudgetForMonth(now.getYear(), now.getMonthValue())).thenReturn(currentMonthBudget);

        
        mockMvc.perform(get("/api/budgets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBudgets", is(2)))
                .andExpect(jsonPath("$.currentMonthBudget", is(currentMonthBudget.doubleValue())));
    }
}