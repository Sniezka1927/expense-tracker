package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.dto.ExpenseDto;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.service.BudgetService;
import com.example.trackexpenses.service.CategoryService;
import com.example.trackexpenses.service.ExpenseService;
import com.example.trackexpenses.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private BudgetService budgetService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DashboardController dashboardController;

    private ExpenseDto testExpense;
    private CategoryDto testCategory;
    private User testUser;
    private final LocalDate today = LocalDate.now();
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); 

        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();

        testCategory = new CategoryDto();
        testCategory.setId(1);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setColorCode("#007bff");
        testCategory.setIsDefault(false);

        testExpense = new ExpenseDto();
        testExpense.setId(1);
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test Expense");
        testExpense.setDate(today);
        testExpense.setCategory(testCategory);
        testExpense.setCreatedAt(now);

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
    }

    @Test
    void getDashboardSummary_ShouldReturnDashboardSummary() throws Exception {
        
        BigDecimal totalExpenses = new BigDecimal("500.00");
        BigDecimal monthlyExpenses = new BigDecimal("200.00");
        BigDecimal totalBudget = new BigDecimal("1000.00");

        when(expenseService.getTotalExpensesForCurrentUser()).thenReturn(totalExpenses);

        LocalDate startOfMonth = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        when(expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth)).thenReturn(monthlyExpenses);

        when(budgetService.getTotalBudgetForMonth(today.getYear(), today.getMonthValue())).thenReturn(totalBudget);
        when(categoryService.findAllCategories()).thenReturn(Collections.singletonList(testCategory));
        when(expenseService.findExpensesByCurrentUser()).thenReturn(Collections.singletonList(testExpense));
        when(expenseService.getExpensesByCategory(startOfMonth, endOfMonth))
                .thenReturn(Collections.singletonMap(testCategory, new BigDecimal("200.00")));

        
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses", is(totalExpenses.doubleValue())))
                .andExpect(jsonPath("$.currentMonthExpenses", is(monthlyExpenses.doubleValue())))
                .andExpect(jsonPath("$.currentMonthBudget", is(totalBudget.doubleValue())))
                .andExpect(jsonPath("$.totalCategories", is(1)))
                .andExpect(jsonPath("$.recentExpenses", hasSize(1)));
    }

    @Test
    void getMonthlyExpenses_ShouldReturnMonthlyExpenses() throws Exception {
        
        int year = 2023;
        Map<String, BigDecimal> monthlyExpenses = new HashMap<>();
        monthlyExpenses.put("January", new BigDecimal("100.00"));
        monthlyExpenses.put("February", new BigDecimal("200.00"));

        when(expenseService.getMonthlyExpenses(year)).thenReturn(monthlyExpenses);

        
        mockMvc.perform(get("/api/dashboard/monthly/{year}", year))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.January", is(100.0)))
                .andExpect(jsonPath("$.February", is(200.0)));
    }

    @Test
    void getOverview_ShouldReturnOverview() throws Exception {
        
        BigDecimal totalSpent = new BigDecimal("500.00");
        BigDecimal monthlySpent = new BigDecimal("200.00");
        BigDecimal monthlyBudget = new BigDecimal("1000.00");

        when(expenseService.findExpensesByCurrentUser()).thenReturn(Collections.singletonList(testExpense));
        when(budgetService.findBudgetsByCurrentUser()).thenReturn(Collections.emptyList());
        when(categoryService.findAllCategories()).thenReturn(Collections.singletonList(testCategory));

        when(expenseService.getTotalExpensesForCurrentUser()).thenReturn(totalSpent);

        LocalDate startOfMonth = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        when(expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth)).thenReturn(monthlySpent);

        when(budgetService.getTotalBudgetForMonth(today.getYear(), today.getMonthValue())).thenReturn(monthlyBudget);

        
        mockMvc.perform(get("/api/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses", is(1)))
                .andExpect(jsonPath("$.totalBudgets", is(0)))
                .andExpect(jsonPath("$.totalCategories", is(1)))
                .andExpect(jsonPath("$.totalSpent", is(totalSpent.doubleValue())))
                .andExpect(jsonPath("$.currentMonthBudget", is(monthlyBudget.doubleValue())))
                .andExpect(jsonPath("$.currentMonthSpent", is(monthlySpent.doubleValue())))
                .andExpect(jsonPath("$.budgetUsagePercent", is(20.0)));
    }

    @Test
    void getRecentActivity_ShouldReturnRecentActivity() throws Exception {
        
        List<ExpenseDto> recentExpenses = Collections.singletonList(testExpense);
        when(expenseService.findExpensesByCurrentUser()).thenReturn(recentExpenses);

        
        mockMvc.perform(get("/api/dashboard/recent-activity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentExpenses", hasSize(1)))
                .andExpect(jsonPath("$.recentExpenses[0].id", is(testExpense.getId())))
                .andExpect(jsonPath("$.recentExpenses[0].amount", is(testExpense.getAmount().doubleValue())))
                .andExpect(jsonPath("$.recentExpenses[0].description", is(testExpense.getDescription())));
    }

    @Test
    void getBudgetStatus_ShouldReturnBudgetStatus() throws Exception {
        
        com.example.trackexpenses.dto.BudgetDto budgetDto = new com.example.trackexpenses.dto.BudgetDto();
        budgetDto.setId(1);
        budgetDto.setAmount(new BigDecimal("500.00"));
        budgetDto.setCategory(testCategory);
        budgetDto.setMonth(today.getMonthValue());
        budgetDto.setYear(today.getYear());

        List<com.example.trackexpenses.dto.BudgetDto> budgets = Collections.singletonList(budgetDto);
        when(budgetService.findBudgetsByYearAndMonth(today.getYear(), today.getMonthValue())).thenReturn(budgets);

        when(budgetService.getSpentAmountForBudget(budgetDto.getId())).thenReturn(new BigDecimal("100.00"));

        
        mockMvc.perform(get("/api/dashboard/budget-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is(testCategory.getName())))
                .andExpect(jsonPath("$[0].budgetAmount", is(500.0)))
                .andExpect(jsonPath("$[0].spentAmount", is(100.0)))
                .andExpect(jsonPath("$[0].remainingAmount", is(400.0)))
                .andExpect(jsonPath("$[0].usagePercentage", is(20.0)))
                .andExpect(jsonPath("$[0].isOverBudget", is(false)));
    }

    @Test
    void getSpendingTrends_ShouldReturnSpendingTrends() throws Exception {
        
        int months = 6;

        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate targetDate = today.minusMonths(i);
            LocalDate startOfMonth = LocalDate.of(targetDate.getYear(), targetDate.getMonthValue(), 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            BigDecimal monthTotal = new BigDecimal((i + 1) * 100);
            when(expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth)).thenReturn(monthTotal);
        }

        
        mockMvc.perform(get("/api/dashboard/trends/{months}", months))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlySpending", aMapWithSize(months)))
                .andExpect(jsonPath("$.averageMonthlySpending").exists());
    }

    @Test
    void getCategoryBreakdown_ShouldReturnCategoryBreakdown() throws Exception {
        
        Map<CategoryDto, BigDecimal> expensesByCategory = new HashMap<>();
        expensesByCategory.put(testCategory, new BigDecimal("300.00"));

        LocalDate startOfMonth = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        when(expenseService.getExpensesByCategory(startOfMonth, endOfMonth)).thenReturn(expensesByCategory);

        
        mockMvc.perform(get("/api/dashboard/category-breakdown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryAmounts", aMapWithSize(1)))
                .andExpect(jsonPath("$.categoryAmounts.['Test Category']", is(300.0)))
                .andExpect(jsonPath("$.totalAmount", is(300.0)))
                .andExpect(jsonPath("$.categoryPercentages", aMapWithSize(1)))
                .andExpect(jsonPath("$.categoryPercentages.['Test Category']", is(100.0)));
    }
}
