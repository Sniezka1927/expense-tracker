package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.BudgetCreateDto;
import com.example.trackexpenses.dto.BudgetDto;
import com.example.trackexpenses.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Budgets", description = "Budget management")
@SecurityRequirement(name = "Bearer Authentication")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Get all budgets for current user")
    @GetMapping
    public ResponseEntity<List<BudgetDto>> getAllBudgets() {
        List<BudgetDto> budgets = budgetService.findBudgetsByCurrentUser();
        return ResponseEntity.ok(budgets);
    }

    @Operation(summary = "Get all budgets (admin only)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BudgetDto>> getAllBudgetsAdmin() {
        List<BudgetDto> budgets = budgetService.findAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    @Operation(summary = "Get budget by ID")
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudgetById(
            @Parameter(description = "Budget ID") @PathVariable Integer id) {
        return budgetService.findById(id)
                .map(budget -> ResponseEntity.ok(budget))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new budget")
    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(@RequestBody BudgetCreateDto budgetCreateDto) {
        try {
            BudgetDto createdBudget = budgetService.createBudget(budgetCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update budget")
    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(
            @Parameter(description = "Budget ID") @PathVariable Integer id,
            @RequestBody BudgetCreateDto budgetCreateDto) {
        try {
            BudgetDto updatedBudget = budgetService.updateBudget(id, budgetCreateDto);
            return ResponseEntity.ok(updatedBudget);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete budget")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "Budget ID") @PathVariable Integer id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get current month budgets")
    @GetMapping("/current-month")
    public ResponseEntity<List<BudgetDto>> getCurrentMonthBudgets() {
        LocalDate now = LocalDate.now();
        List<BudgetDto> budgets = budgetService.findBudgetsByYearAndMonth(now.getYear(), now.getMonthValue());
        return ResponseEntity.ok(budgets);
    }

    @Operation(summary = "Get budgets by year")
    @GetMapping("/year/{year}")
    public ResponseEntity<List<BudgetDto>> getBudgetsByYear(
            @Parameter(description = "Year") @PathVariable Integer year) {
        List<BudgetDto> budgets = budgetService.findBudgetsByYear(year);
        return ResponseEntity.ok(budgets);
    }

    @Operation(summary = "Get budgets by period")
    @GetMapping("/period/{year}/{month}")
    public ResponseEntity<List<BudgetDto>> getBudgetsByYearAndMonth(
            @Parameter(description = "Year") @PathVariable Integer year,
            @Parameter(description = "Month (1-12)") @PathVariable Integer month) {
        List<BudgetDto> budgets = budgetService.findBudgetsByYearAndMonth(year, month);
        return ResponseEntity.ok(budgets);
    }

    @Operation(summary = "Get total budget for month")
    @GetMapping("/total/{year}/{month}")
    public ResponseEntity<BigDecimal> getTotalBudgetForMonth(
            @Parameter(description = "Year") @PathVariable Integer year,
            @Parameter(description = "Month (1-12)") @PathVariable Integer month) {
        BigDecimal total = budgetService.getTotalBudgetForMonth(year, month);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Get spent amount for budget")
    @GetMapping("/{id}/spent")
    public ResponseEntity<BigDecimal> getSpentAmountForBudget(
            @Parameter(description = "Budget ID") @PathVariable Integer id) {
        BigDecimal spent = budgetService.getSpentAmountForBudget(id);
        return ResponseEntity.ok(spent);
    }

    @Operation(summary = "Get remaining budget for category")
    @GetMapping("/remaining/{categoryId}/{year}/{month}")
    public ResponseEntity<BigDecimal> getRemainingBudgetForCategory(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId,
            @Parameter(description = "Year") @PathVariable Integer year,
            @Parameter(description = "Month (1-12)") @PathVariable Integer month) {
        BigDecimal remaining = budgetService.getRemainingBudgetForCategory(categoryId, year, month);
        return ResponseEntity.ok(remaining);
    }

    @Operation(summary = "Get budget statistics")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBudgetStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBudgets", budgetService.findBudgetsByCurrentUser().size());

        LocalDate now = LocalDate.now();
        stats.put("currentMonthBudget", budgetService.getTotalBudgetForMonth(now.getYear(), now.getMonthValue()));

        return ResponseEntity.ok(stats);
    }
}