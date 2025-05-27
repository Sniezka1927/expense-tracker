package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.dto.ExpenseCreateDto;
import com.example.trackexpenses.dto.ExpenseDto;
import com.example.trackexpenses.service.ExpenseService;
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
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getAllExpenses() {
        List<ExpenseDto> expenses = expenseService.findExpensesByCurrentUser();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExpenseDto>> getAllExpensesAdmin() {
        // TODO: Add admin authorization check
        List<ExpenseDto> expenses = expenseService.findAllExpenses();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable Integer id) {
        return expenseService.findById(id)
                .map(expense -> ResponseEntity.ok(expense))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@RequestBody ExpenseCreateDto expenseCreateDto) {
        try {
            ExpenseDto createdExpense = expenseService.createExpense(expenseCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Integer id, @RequestBody ExpenseCreateDto expenseCreateDto) {
        try {
            ExpenseDto updatedExpense = expenseService.updateExpense(id, expenseCreateDto);
            return ResponseEntity.ok(updatedExpense);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Integer id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/filter/date-range")
    public ResponseEntity<List<ExpenseDto>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExpenseDto> expenses = expenseService.findExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/filter/category/{categoryId}")
    public ResponseEntity<List<ExpenseDto>> getExpensesByCategory(@PathVariable Integer categoryId) {
        List<ExpenseDto> expenses = expenseService.findExpensesByCategory(categoryId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/filter/amount-range")
    public ResponseEntity<List<ExpenseDto>> getExpensesByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        List<ExpenseDto> expenses = expenseService.findExpensesByAmountRange(minAmount, maxAmount);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/reports/total")
    public ResponseEntity<BigDecimal> getTotalExpenses() {
        BigDecimal total = expenseService.getTotalExpensesForCurrentUser();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/reports/total/period")
    public ResponseEntity<BigDecimal> getTotalExpensesForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = expenseService.getTotalExpensesForPeriod(startDate, endDate);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/reports/by-category")
    public ResponseEntity<Map<CategoryDto, BigDecimal>> getExpensesByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<CategoryDto, BigDecimal> expensesByCategory = expenseService.getExpensesByCategory(startDate, endDate);
        return ResponseEntity.ok(expensesByCategory);
    }

    @GetMapping("/reports/monthly/{year}")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyExpenses(@PathVariable Integer year) {
        Map<String, BigDecimal> monthlyExpenses = expenseService.getMonthlyExpenses(year);
        return ResponseEntity.ok(monthlyExpenses);
    }

    @GetMapping("/current-month")
    public ResponseEntity<List<ExpenseDto>> getCurrentMonthExpenses() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<ExpenseDto> expenses = expenseService.findExpensesByDateRange(startOfMonth, endOfMonth);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/last-days/{days}")
    public ResponseEntity<List<ExpenseDto>> getExpensesFromLastDays(@PathVariable Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<ExpenseDto> expenses = expenseService.findExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

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