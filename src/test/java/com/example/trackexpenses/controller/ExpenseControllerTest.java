package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.dto.ExpenseCreateDto;
import com.example.trackexpenses.dto.ExpenseDto;
import com.example.trackexpenses.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExpenseControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @InjectMocks
    private ExpenseController expenseController;

    @Mock
    private ExpenseService expenseService;

    private ExpenseDto testExpense;
    private ExpenseCreateDto expenseCreateDto;
    private CategoryDto testCategory;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build();

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
        testExpense.setCreatedAt(LocalDateTime.now());

        expenseCreateDto = new ExpenseCreateDto();
        expenseCreateDto.setAmount(new BigDecimal("100.00"));
        expenseCreateDto.setDescription("Test Expense");
        expenseCreateDto.setDate(today);
        expenseCreateDto.setCategoryId(1);
    }

    @Test
    void getAllExpenses_ShouldReturnAllExpensesForCurrentUser() throws Exception {
        List<ExpenseDto> expenses = Collections.singletonList(testExpense);
        when(expenseService.findExpensesByCurrentUser()).thenReturn(expenses);
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testExpense.getId())))
                .andExpect(jsonPath("$[0].amount", is(testExpense.getAmount().doubleValue())))
                .andExpect(jsonPath("$[0].description", is(testExpense.getDescription())))
                .andExpect(jsonPath("$[0].category.id", is(testCategory.getId())));
    }

    @Test
    void getAllExpensesAdmin_ShouldReturnAllExpenses() throws Exception {
        List<ExpenseDto> expenses = Collections.singletonList(testExpense);
        when(expenseService.findAllExpenses()).thenReturn(expenses);
        mockMvc.perform(get("/api/expenses/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testExpense.getId())))
                .andExpect(jsonPath("$[0].amount", is(testExpense.getAmount().doubleValue())));
    }

    @Test
    void getExpenseById_ShouldReturnExpenseWhenExists() throws Exception {
        when(expenseService.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));
        mockMvc.perform(get("/api/expenses/{id}", testExpense.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testExpense.getId())))
                .andExpect(jsonPath("$.amount", is(testExpense.getAmount().doubleValue())))
                .andExpect(jsonPath("$.description", is(testExpense.getDescription())))
                .andExpect(jsonPath("$.category.id", is(testCategory.getId())));
    }

    @Test
    void getExpenseById_ShouldReturnNotFoundWhenExpenseDoesNotExist() throws Exception {
        when(expenseService.findById(999)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/expenses/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExpense_ShouldCreateAndReturnExpense() throws Exception {
        
        when(expenseService.createExpense(any(ExpenseCreateDto.class))).thenReturn(testExpense);

        
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(testExpense.getId())))
                .andExpect(jsonPath("$.amount", is(testExpense.getAmount().doubleValue())))
                .andExpect(jsonPath("$.description", is(testExpense.getDescription())));
    }

    @Test
    void createExpense_ShouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        
        when(expenseService.createExpense(any(ExpenseCreateDto.class))).thenThrow(new RuntimeException("User not authenticated"));

        
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExpense_ShouldUpdateAndReturnExpense() throws Exception {
        
        when(expenseService.updateExpense(eq(testExpense.getId()), any(ExpenseCreateDto.class))).thenReturn(testExpense);

        
        mockMvc.perform(put("/api/expenses/{id}", testExpense.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testExpense.getId())))
                .andExpect(jsonPath("$.amount", is(testExpense.getAmount().doubleValue())))
                .andExpect(jsonPath("$.description", is(testExpense.getDescription())));
    }

    @Test
    void updateExpense_ShouldReturnNotFoundWhenServiceThrowsException() throws Exception {
        
        when(expenseService.updateExpense(eq(999), any(ExpenseCreateDto.class))).thenThrow(new RuntimeException("Expense not found"));

        
        mockMvc.perform(put("/api/expenses/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseCreateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpense_ShouldDeleteExpense() throws Exception {
        
        doNothing().when(expenseService).deleteExpense(testExpense.getId());

        
        mockMvc.perform(delete("/api/expenses/{id}", testExpense.getId()))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(testExpense.getId());
    }

    @Test
    void deleteExpense_ShouldReturnNotFoundWhenServiceThrowsException() throws Exception {
        
        doThrow(new RuntimeException("Expense not found")).when(expenseService).deleteExpense(999);

        
        mockMvc.perform(delete("/api/expenses/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentMonthExpenses_ShouldReturnCurrentMonthExpenses() throws Exception {
        
        LocalDate startOfMonth = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        List<ExpenseDto> expenses = Collections.singletonList(testExpense);

        when(expenseService.findExpensesByDateRange(startOfMonth, endOfMonth)).thenReturn(expenses);

        
        mockMvc.perform(get("/api/expenses/current-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testExpense.getId())));
    }

    @Test
    void getExpensesFromLastDays_ShouldReturnExpensesFromLastDays() throws Exception {
        
        int days = 7;
        LocalDate endDate = today;
        LocalDate startDate = endDate.minusDays(days);
        List<ExpenseDto> expenses = Collections.singletonList(testExpense);

        when(expenseService.findExpensesByDateRange(startDate, endDate)).thenReturn(expenses);

        
        mockMvc.perform(get("/api/expenses/last-days/{days}", days))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testExpense.getId())));
    }

    @Test
    void getExpensesByDateRange_ShouldReturnExpensesInDateRange() throws Exception {
        
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;
        List<ExpenseDto> expenses = Collections.singletonList(testExpense);

        when(expenseService.findExpensesByDateRange(startDate, endDate)).thenReturn(expenses);

        
        mockMvc.perform(get("/api/expenses/filter/date-range")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testExpense.getId())));
    }

    @Test
    void getExpensesByCategory_ShouldReturnExpensesForCategory() throws Exception {
        
        List<ExpenseDto> expenses = Collections.singletonList(testExpense);
        when(expenseService.findExpensesByCategory(testCategory.getId())).thenReturn(expenses);

        
        mockMvc.perform(get("/api/expenses/filter/category/{categoryId}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testExpense.getId())))
                .andExpect(jsonPath("$[0].category.id", is(testCategory.getId())));
    }

    @Test
    void getExpensesByAmountRange_ShouldReturnExpensesInAmountRange() throws Exception {
        
        BigDecimal minAmount = new BigDecimal("50.00");
        BigDecimal maxAmount = new BigDecimal("150.00");
        List<ExpenseDto> expenses = Collections.singletonList(testExpense);

        when(expenseService.findExpensesByAmountRange(minAmount, maxAmount)).thenReturn(expenses);

        
        mockMvc.perform(get("/api/expenses/filter/amount-range")
                .param("minAmount", minAmount.toString())
                .param("maxAmount", maxAmount.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testExpense.getId())))
                .andExpect(jsonPath("$[0].amount", is(testExpense.getAmount().doubleValue())));
    }

    @Test
    void getTotalExpenses_ShouldReturnTotalExpenses() throws Exception {
        
        BigDecimal total = new BigDecimal("500.00");
        when(expenseService.getTotalExpensesForCurrentUser()).thenReturn(total);

        
        mockMvc.perform(get("/api/expenses/reports/total"))
                .andExpect(status().isOk())
                .andExpect(content().string(total.toString()));
    }

    @Test
    void getTotalExpensesForPeriod_ShouldReturnTotalExpensesForPeriod() throws Exception {
        
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today;
        BigDecimal total = new BigDecimal("300.00");

        when(expenseService.getTotalExpensesForPeriod(startDate, endDate)).thenReturn(total);

        
        mockMvc.perform(get("/api/expenses/reports/total/period")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(total.toString()));
    }

    @Test
    void getExpensesByCategory_ReportShouldReturnExpensesByCategory() throws Exception {
        
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today;
        Map<CategoryDto, BigDecimal> expensesByCategory = new HashMap<>();
        expensesByCategory.put(testCategory, new BigDecimal("300.00"));

        when(expenseService.getExpensesByCategory(startDate, endDate)).thenReturn(expensesByCategory);

        
        mockMvc.perform(get("/api/expenses/reports/by-category")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getMonthlyExpenses_ShouldReturnMonthlyExpenses() throws Exception {
        
        int year = 2023;
        Map<String, BigDecimal> monthlyExpenses = new HashMap<>();
        monthlyExpenses.put("January", new BigDecimal("100.00"));
        monthlyExpenses.put("February", new BigDecimal("200.00"));

        when(expenseService.getMonthlyExpenses(year)).thenReturn(monthlyExpenses);

        
        mockMvc.perform(get("/api/expenses/reports/monthly/{year}", year))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.January", is(100.0)))
                .andExpect(jsonPath("$.February", is(200.0)));
    }

    @Test
    void getExpenseStats_ShouldReturnExpenseStatistics() throws Exception {
        
        List<ExpenseDto> expenses = Arrays.asList(testExpense, testExpense);
        BigDecimal totalAmount = new BigDecimal("200.00");
        BigDecimal currentMonthAmount = new BigDecimal("100.00");

        when(expenseService.findExpensesByCurrentUser()).thenReturn(expenses);
        when(expenseService.getTotalExpensesForCurrentUser()).thenReturn(totalAmount);

        LocalDate startOfMonth = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        when(expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth)).thenReturn(currentMonthAmount);

        
        mockMvc.perform(get("/api/expenses/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(totalAmount.doubleValue())))
                .andExpect(jsonPath("$.currentMonthAmount", is(currentMonthAmount.doubleValue())));
    }
}
