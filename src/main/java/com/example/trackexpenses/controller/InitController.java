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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class InitController {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> initializeAllData() {
        Map<String, Object> result = new HashMap<>();

        try {
            initializeCategories();
            result.put("categories", "✅ Categories initialized");

            initializeUsers();
            result.put("users", "✅ Users initialized");

            initializeExpenses();
            result.put("expenses", "✅ Sample expenses created");

            initializeBudgets();
            result.put("budgets", "✅ Sample budgets created");

            result.put("status", "SUCCESS");
            result.put("message", "All mock data initialized successfully!");

            log.info("All mock data initialized successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Error initializing data: " + e.getMessage());
            log.error("Error initializing mock data", e);
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<String> initializeCategories() {
        String[][] defaultCategories = {
                {"Food", "Meals, groceries, dining out", "#28a745"},
                {"Transportation", "Gas, public transport, car maintenance", "#007bff"},
                {"Entertainment", "Movies, games, hobbies", "#ffc107"},
                {"Healthcare", "Medical expenses, pharmacy", "#dc3545"},
                {"Shopping", "Clothes, electronics, misc items", "#6f42c1"},
                {"Bills", "Utilities, rent, subscriptions", "#fd7e14"},
                {"Education", "Books, courses, training", "#17a2b8"},
                {"Travel", "Vacation, trips, accommodation", "#6610f2"}
        };

        int created = 0;
        for (String[] categoryData : defaultCategories) {
            String name = categoryData[0];
            if (categoryRepository.findByName(name).isEmpty()) {
                Category category = new Category();
                category.setName(name);
                category.setDescription(categoryData[1]);
                category.setColorCode(categoryData[2]);
                category.setIsDefault(true);
                categoryRepository.save(category);
                created++;
            }
        }

        return ResponseEntity.ok("Categories initialized: " + created + " new categories created");
    }

    @PostMapping("/users")
    public ResponseEntity<String> initializeUsers() {
        int created = 0;

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@trackexpenses.com");
            admin.setPassword("admin123");
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            created++;
        }

        if (userRepository.findByUsername("testuser").isEmpty()) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("user@trackexpenses.com");
            user.setPassword("user123");
            user.setRole(Role.USER);
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            created++;
        }

        if (userRepository.findByUsername("demo").isEmpty()) {
            User demoUser = new User();
            demoUser.setUsername("demo");
            demoUser.setEmail("demo@trackexpenses.com");
            demoUser.setPassword("demo123");
            demoUser.setRole(Role.USER);
            demoUser.setIsActive(true);
            demoUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(demoUser);
            created++;
        }

        return ResponseEntity.ok("Users initialized: " + created + " new users created");
    }

    @PostMapping("/expenses")
    public ResponseEntity<String> initializeExpenses() {
        User testUser = userRepository.findByUsername("testuser").orElse(null);
        if (testUser == null) {
            return ResponseEntity.badRequest().body("Test user not found. Initialize users first.");
        }

        Category foodCategory = categoryRepository.findByName("Food").orElse(null);
        Category transportCategory = categoryRepository.findByName("Transportation").orElse(null);
        Category entertainmentCategory = categoryRepository.findByName("Entertainment").orElse(null);
        Category billsCategory = categoryRepository.findByName("Bills").orElse(null);

        if (foodCategory == null) {
            return ResponseEntity.badRequest().body("Categories not found. Initialize categories first.");
        }

        int created = 0;
        LocalDate today = LocalDate.now();

        Object[][] sampleExpenses = {
                {new BigDecimal("45.50"), "Grocery shopping", today.minusDays(2), foodCategory},
                {new BigDecimal("12.00"), "Bus ticket", today.minusDays(1), transportCategory},
                {new BigDecimal("25.00"), "Movie ticket", today.minusDays(3), entertainmentCategory},
                {new BigDecimal("150.00"), "Electricity bill", today.minusDays(5), billsCategory},
                {new BigDecimal("35.75"), "Restaurant dinner", today.minusDays(1), foodCategory},
                {new BigDecimal("60.00"), "Gas station", today.minusDays(4), transportCategory},
                {new BigDecimal("8.50"), "Coffee", today, foodCategory},
                {new BigDecimal("120.00"), "Internet bill", today.minusDays(10), billsCategory},

                {new BigDecimal("200.00"), "Monthly groceries", today.minusMonths(1).minusDays(15), foodCategory},
                {new BigDecimal("80.00"), "Gas", today.minusMonths(1).minusDays(10), transportCategory},
                {new BigDecimal("40.00"), "Cinema", today.minusMonths(1).minusDays(5), entertainmentCategory},
                {new BigDecimal("75.00"), "Lunch", today.minusMonths(1).minusDays(20), foodCategory},

                {new BigDecimal("300.00"), "Big shopping", today.minusMonths(2).minusDays(12), foodCategory},
                {new BigDecimal("90.00"), "Transport pass", today.minusMonths(2).minusDays(8), transportCategory}
        };

        for (Object[] expenseData : sampleExpenses) {
            Expense expense = new Expense();
            expense.setAmount((BigDecimal) expenseData[0]);
            expense.setDescription((String) expenseData[1]);
            expense.setDate((LocalDate) expenseData[2]);
            expense.setCategory((Category) expenseData[3]);
            expense.setUser(testUser);
            expense.setCreatedAt(LocalDateTime.now());
            expenseRepository.save(expense);
            created++;
        }

        return ResponseEntity.ok("Sample expenses created: " + created + " expenses added");
    }

    @PostMapping("/budgets")
    public ResponseEntity<String> initializeBudgets() {
        User testUser = userRepository.findByUsername("testuser").orElse(null);
        if (testUser == null) {
            return ResponseEntity.badRequest().body("Test user not found. Initialize users first.");
        }

        Category foodCategory = categoryRepository.findByName("Food").orElse(null);
        Category transportCategory = categoryRepository.findByName("Transportation").orElse(null);
        Category entertainmentCategory = categoryRepository.findByName("Entertainment").orElse(null);
        Category billsCategory = categoryRepository.findByName("Bills").orElse(null);

        if (foodCategory == null) {
            return ResponseEntity.badRequest().body("Categories not found. Initialize categories first.");
        }

        int created = 0;
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        Object[][] sampleBudgets = {
                {new BigDecimal("500.00"), foodCategory, currentMonth, currentYear},
                {new BigDecimal("200.00"), transportCategory, currentMonth, currentYear},
                {new BigDecimal("100.00"), entertainmentCategory, currentMonth, currentYear},
                {new BigDecimal("300.00"), billsCategory, currentMonth, currentYear},

                {new BigDecimal("600.00"), foodCategory, currentMonth == 12 ? 1 : currentMonth + 1, currentMonth == 12 ? currentYear + 1 : currentYear},
                {new BigDecimal("250.00"), transportCategory, currentMonth == 12 ? 1 : currentMonth + 1, currentMonth == 12 ? currentYear + 1 : currentYear}
        };

        for (Object[] budgetData : sampleBudgets) {
            if (budgetRepository.findByUserAndCategoryAndYearAndMonth(
                    testUser, (Category) budgetData[1], (Integer) budgetData[3], (Integer) budgetData[2]).isEmpty()) {

                Budget budget = new Budget();
                budget.setAmount((BigDecimal) budgetData[0]);
                budget.setCategory((Category) budgetData[1]);
                budget.setMonth((Integer) budgetData[2]);
                budget.setYear((Integer) budgetData[3]);
                budget.setUser(testUser);
                budget.setCreatedAt(LocalDateTime.now());
                budgetRepository.save(budget);
                created++;
            }
        }

        return ResponseEntity.ok("Sample budgets created: " + created + " budgets added");
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearAllData() {
        try {
            expenseRepository.deleteAll();
            budgetRepository.deleteAll();

            return ResponseEntity.ok("All expenses and budgets cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing data", e);
            return ResponseEntity.status(500).body("Error clearing data: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDataStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("users", userRepository.count());
        status.put("categories", categoryRepository.count());
        status.put("expenses", expenseRepository.count());
        status.put("budgets", budgetRepository.count());

        return ResponseEntity.ok(status);
    }
}