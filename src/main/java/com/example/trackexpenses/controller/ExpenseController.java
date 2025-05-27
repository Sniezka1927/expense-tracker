package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.dto.ExpenseCreateDto;
import com.example.trackexpenses.dto.ExpenseDto;
import com.example.trackexpenses.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Expenses", description = "Expense management")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Get all expenses")
    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getAllExpenses() {
        List<ExpenseDto> expenses = expenseService.findExpensesByCurrentUser();
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Get all expenses (admin)")
    @GetMapping("/all")
    public ResponseEntity<List<ExpenseDto>> getAllExpensesAdmin() {
        List<ExpenseDto> expenses = expenseService.findAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Get expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(
            @Parameter(description = "Expense ID") @PathVariable Integer id) {
        return expenseService.findById(id)
                .map(expense -> ResponseEntity.ok(expense))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new expense")
    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@RequestBody ExpenseCreateDto expenseCreateDto) {
        try {
            ExpenseDto createdExpense = expenseService.createExpense(expenseCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update expense")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(
            @Parameter(description = "Expense ID") @PathVariable Integer id,
            @RequestBody ExpenseCreateDto expenseCreateDto) {
        try {
            ExpenseDto updatedExpense = expenseService.updateExpense(id, expenseCreateDto);
            return ResponseEntity.ok(updatedExpense);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete expense")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Expense ID") @PathVariable Integer id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get current month expenses")
    @GetMapping("/current-month")
    public ResponseEntity<List<ExpenseDto>> getCurrentMonthExpenses() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<ExpenseDto> expenses = expenseService.findExpensesByDateRange(startOfMonth, endOfMonth);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Get expenses from last N days")
    @GetMapping("/last-days/{days}")
    public ResponseEntity<List<ExpenseDto>> getExpensesFromLastDays(
            @Parameter(description = "Number of days") @PathVariable Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<ExpenseDto> expenses = expenseService.findExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Filter expenses by date range")
    @GetMapping("/filter/date-range")
    public ResponseEntity<List<ExpenseDto>> getExpensesByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExpenseDto> expenses = expenseService.findExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Filter expenses by category")
    @GetMapping("/filter/category/{categoryId}")
    public ResponseEntity<List<ExpenseDto>> getExpensesByCategory(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId) {
        List<ExpenseDto> expenses = expenseService.findExpensesByCategory(categoryId);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Filter expenses by amount range")
    @GetMapping("/filter/amount-range")
    public ResponseEntity<List<ExpenseDto>> getExpensesByAmountRange(
            @Parameter(description = "Minimum amount") @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum amount") @RequestParam BigDecimal maxAmount) {
        List<ExpenseDto> expenses = expenseService.findExpensesByAmountRange(minAmount, maxAmount);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Get total expenses")
    @GetMapping("/reports/total")
    public ResponseEntity<BigDecimal> getTotalExpenses() {
        BigDecimal total = expenseService.getTotalExpensesForCurrentUser();
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Get total expenses for period")
    @GetMapping("/reports/total/period")
    public ResponseEntity<BigDecimal> getTotalExpensesForPeriod(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = expenseService.getTotalExpensesForPeriod(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Get expenses grouped by category")
    @GetMapping("/reports/by-category")
    public ResponseEntity<Map<CategoryDto, BigDecimal>> getExpensesByCategory(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<CategoryDto, BigDecimal> expensesByCategory = expenseService.getExpensesByCategory(startDate, endDate);
        return ResponseEntity.ok(expensesByCategory);
    }

    @Operation(summary = "Get monthly expenses for year")
    @GetMapping("/reports/monthly/{year}")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyExpenses(
            @Parameter(description = "Year") @PathVariable Integer year) {
        Map<String, BigDecimal> monthlyExpenses = expenseService.getMonthlyExpenses(year);
        return ResponseEntity.ok(monthlyExpenses);
    }

    @Operation(summary = "Get expense statistics")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getExpenseStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExpenses", expenseService.findExpensesByCurrentUser().size());
        stats.put("totalAmount", expenseService.getTotalExpensesForCurrentUser());

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        stats.put("currentMonthAmount", expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth));

        return ResponseEntity.ok(stats);
    }
}