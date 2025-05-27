package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.BudgetCreateDto;
import com.example.trackexpenses.dto.BudgetDto;
import com.example.trackexpenses.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDto>> getAllBudgets() {
        List<BudgetDto> budgets = budgetService.findBudgetsByCurrentUser();
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BudgetDto>> getAllBudgetsAdmin() {
        // TODO: Add admin authorization check
        List<BudgetDto> budgets = budgetService.findAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudgetById(@PathVariable Integer id) {
        return budgetService.findById(id)
                .map(budget -> ResponseEntity.ok(budget))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(@RequestBody BudgetCreateDto budgetCreateDto) {
        try {
            BudgetDto createdBudget = budgetService.createBudget(budgetCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(@PathVariable Integer id, @RequestBody BudgetCreateDto budgetCreateDto) {
        try {
            BudgetDto updatedBudget = budgetService.updateBudget(id, budgetCreateDto);
            return ResponseEntity.ok(updatedBudget);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Integer id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<BudgetDto>> getBudgetsByYear(@PathVariable Integer year) {
        List<BudgetDto> budgets = budgetService.findBudgetsByYear(year);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/period/{year}/{month}")
    public ResponseEntity<List<BudgetDto>> getBudgetsByYearAndMonth(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        List<BudgetDto> budgets = budgetService.findBudgetsByYearAndMonth(year, month);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/total/{year}/{month}")
    public ResponseEntity<BigDecimal> getTotalBudgetForMonth(
            @PathVariable Integer year,
            @PathVariable Integer month) {
        BigDecimal total = budgetService.getTotalBudgetForMonth(year, month);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}/spent")
    public ResponseEntity<BigDecimal> getSpentAmountForBudget(@PathVariable Integer id) {
        BigDecimal spent = budgetService.getSpentAmountForBudget(id);
        return ResponseEntity.ok(spent);
    }

    @GetMapping("/remaining/{categoryId}/{year}/{month}")
    public ResponseEntity<BigDecimal> getRemainingBudgetForCategory(
            @PathVariable Integer categoryId,
            @PathVariable Integer year,
            @PathVariable Integer month) {
        BigDecimal remaining = budgetService.getRemainingBudgetForCategory(categoryId, year, month);
        return ResponseEntity.ok(remaining);
    }

    @GetMapping("/current-month")
    public ResponseEntity<List<BudgetDto>> getCurrentMonthBudgets() {
        LocalDate now = LocalDate.now();
        List<BudgetDto> budgets = budgetService.findBudgetsByYearAndMonth(now.getYear(), now.getMonthValue());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBudgetStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBudgets", budgetService.findBudgetsByCurrentUser().size());

        LocalDate now = LocalDate.now();
        stats.put("currentMonthBudget", budgetService.getTotalBudgetForMonth(now.getYear(), now.getMonthValue()));

        return ResponseEntity.ok(stats);
    }

}