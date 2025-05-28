package com.example.trackexpenses.controller;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.dto.ExpenseDto;
import com.example.trackexpenses.service.BudgetService;
import com.example.trackexpenses.service.CategoryService;
import com.example.trackexpenses.service.ExpenseService;
import com.example.trackexpenses.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Analytics and reporting")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final UserService userService;

    @Operation(summary = "Get dashboard summary")
    @GetMapping
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        DashboardSummary summary = new DashboardSummary();

        LocalDate startOfMonth = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        summary.setCurrentMonthExpenses(expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth));
        summary.setTotalExpenses(expenseService.getTotalExpensesForCurrentUser());
        summary.setCurrentMonthBudget(budgetService.getTotalBudgetForMonth(currentYear, currentMonth));

        summary.setRecentExpenses(expenseService.findExpensesByCurrentUser().stream()
                .limit(10)
                .toList());

        summary.setTotalCategories(categoryService.findAllCategories().size());
        summary.setExpensesByCategory(expenseService.getExpensesByCategory(startOfMonth, endOfMonth));

        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get monthly expenses")
    @GetMapping("/monthly/{year}")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyExpenses(
            @Parameter(description = "Year") @PathVariable Integer year) {
        Map<String, BigDecimal> monthlyExpenses = expenseService.getMonthlyExpenses(year);
        return ResponseEntity.ok(monthlyExpenses);
    }

    @Operation(summary = "Get overview statistics")
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        LocalDate now = LocalDate.now();

        overview.put("totalExpenses", expenseService.findExpensesByCurrentUser().size());
        overview.put("totalBudgets", budgetService.findBudgetsByCurrentUser().size());
        overview.put("totalCategories", categoryService.findAllCategories().size());

        overview.put("totalSpent", expenseService.getTotalExpensesForCurrentUser());
        overview.put("currentMonthBudget", budgetService.getTotalBudgetForMonth(now.getYear(), now.getMonthValue()));

        LocalDate startOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        overview.put("currentMonthSpent", expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth));

        BigDecimal monthlyBudget = budgetService.getTotalBudgetForMonth(now.getYear(), now.getMonthValue());
        BigDecimal monthlySpent = expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth);

        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            double budgetUsagePercent = monthlySpent.divide(monthlyBudget, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
            overview.put("budgetUsagePercent", budgetUsagePercent);
        } else {
            overview.put("budgetUsagePercent", 0);
        }

        return ResponseEntity.ok(overview);
    }

    @Operation(summary = "Get recent activity")
    @GetMapping("/recent-activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity() {
        Map<String, Object> activity = new HashMap<>();

        List<ExpenseDto> recentExpenses = expenseService.findExpensesByCurrentUser().stream()
                .limit(5)
                .collect(Collectors.toList());
        activity.put("recentExpenses", recentExpenses);

        LocalDate weekAgo = LocalDate.now().minusDays(7);
        LocalDate today = LocalDate.now();
        List<ExpenseDto> weekExpenses = expenseService.findExpensesByDateRange(weekAgo, today);
        activity.put("lastWeekExpenses", weekExpenses);

        BigDecimal weekTotal = weekExpenses.stream()
                .map(ExpenseDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        activity.put("lastWeekTotal", weekTotal);

        return ResponseEntity.ok(activity);
    }

    @Operation(summary = "Get budget status")
    @GetMapping("/budget-status")
    public ResponseEntity<List<Map<String, Object>>> getBudgetStatus() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        List<com.example.trackexpenses.dto.BudgetDto> currentBudgets = budgetService.findBudgetsByYearAndMonth(currentYear, currentMonth);
        List<Map<String, Object>> budgetStatus = new ArrayList<>();

        for (com.example.trackexpenses.dto.BudgetDto budget : currentBudgets) {
            Map<String, Object> status = new HashMap<>();
            status.put("category", budget.getCategory().getName());
            status.put("budgetAmount", budget.getAmount());

            BigDecimal spent = budgetService.getSpentAmountForBudget(budget.getId());
            status.put("spentAmount", spent);
            status.put("remainingAmount", budget.getAmount().subtract(spent));

            double percentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(budget.getAmount(), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue()
                    : 0;
            status.put("usagePercentage", percentage);
            status.put("isOverBudget", spent.compareTo(budget.getAmount()) > 0);

            budgetStatus.add(status);
        }

        return ResponseEntity.ok(budgetStatus);
    }

    @Operation(summary = "Get spending trends")
    @GetMapping("/trends/{months}")
    public ResponseEntity<Map<String, Object>> getSpendingTrends(
            @Parameter(description = "Number of months") @PathVariable Integer months) {
        Map<String, Object> trends = new HashMap<>();
        LocalDate now = LocalDate.now();

        Map<String, BigDecimal> monthlyTrends = new LinkedHashMap<>();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate targetDate = now.minusMonths(i);
            LocalDate startOfMonth = LocalDate.of(targetDate.getYear(), targetDate.getMonthValue(), 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            BigDecimal monthTotal = expenseService.getTotalExpensesForPeriod(startOfMonth, endOfMonth);
            String monthKey = targetDate.getYear() + "-" + String.format("%02d", targetDate.getMonthValue());
            monthlyTrends.put(monthKey, monthTotal);
        }

        trends.put("monthlySpending", monthlyTrends);

        BigDecimal average = monthlyTrends.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(monthlyTrends.size()), 2, BigDecimal.ROUND_HALF_UP);
        trends.put("averageMonthlySpending", average);

        return ResponseEntity.ok(trends);
    }

    @Operation(summary = "Get category breakdown")
    @GetMapping("/category-breakdown")
    public ResponseEntity<Map<String, Object>> getCategoryBreakdown() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        Map<CategoryDto, BigDecimal> categoryExpenses = expenseService.getExpensesByCategory(startOfMonth, endOfMonth);

        Map<String, Object> breakdown = new HashMap<>();
        Map<String, BigDecimal> categoryAmounts = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<CategoryDto, BigDecimal> entry : categoryExpenses.entrySet()) {
            categoryAmounts.put(entry.getKey().getName(), entry.getValue());
            total = total.add(entry.getValue());
        }

        breakdown.put("categoryAmounts", categoryAmounts);
        breakdown.put("totalAmount", total);

        Map<String, Double> categoryPercentages = new LinkedHashMap<>();
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : categoryAmounts.entrySet()) {
                double percentage = entry.getValue().divide(total, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue();
                categoryPercentages.put(entry.getKey(), percentage);
            }
        }
        breakdown.put("categoryPercentages", categoryPercentages);

        return ResponseEntity.ok(breakdown);
    }

    @Data
    public static class DashboardSummary {
        private BigDecimal currentMonthExpenses;
        private BigDecimal totalExpenses;
        private BigDecimal currentMonthBudget;
        private List<?> recentExpenses;
        private Integer totalCategories;
        private Map<CategoryDto, BigDecimal> expensesByCategory;
    }
}