package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.dto.ExpenseCreateDto;
import com.example.trackexpenses.dto.ExpenseDto;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.entity.Expense;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<ExpenseDto> findAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> findExpensesByCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return expenseRepository.findByUser(currentUser).stream()
                .map(this::convertToDto)
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ExpenseDto> findById(Integer id) {
        return expenseRepository.findById(id)
                .map(this::convertToDto);
    }

    public ExpenseDto createExpense(ExpenseCreateDto expenseCreateDto) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        Category category = categoryService.findCategoryById(expenseCreateDto.getCategoryId());

        Expense expense = new Expense();
        expense.setAmount(expenseCreateDto.getAmount());
        expense.setDescription(expenseCreateDto.getDescription());
        expense.setDate(expenseCreateDto.getDate());
        expense.setCategory(category);
        expense.setUser(currentUser);
        expense.setCreatedAt(LocalDateTime.now());

        Expense savedExpense = expenseRepository.save(expense);
        return convertToDto(savedExpense);
    }

    public ExpenseDto updateExpense(Integer id, ExpenseCreateDto expenseCreateDto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        User currentUser = userService.getCurrentUser();
        if (currentUser == null || !expense.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to update this expense");
        }

        Category category = categoryService.findCategoryById(expenseCreateDto.getCategoryId());

        expense.setAmount(expenseCreateDto.getAmount());
        expense.setDescription(expenseCreateDto.getDescription());
        expense.setDate(expenseCreateDto.getDate());
        expense.setCategory(category);
        expense.setUpdatedAt(LocalDateTime.now());

        Expense savedExpense = expenseRepository.save(expense);
        return convertToDto(savedExpense);
    }

    public void deleteExpense(Integer id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        User currentUser = userService.getCurrentUser();
        if (currentUser == null || !expense.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to delete this expense");
        }

        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> findExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return expenseRepository.findByUserAndDateBetween(currentUser, startDate, endDate).stream()
                .map(this::convertToDto)
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> findExpensesByCategory(Integer categoryId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        Category category = categoryService.findCategoryById(categoryId);
        return expenseRepository.findByUserAndCategory(currentUser, category).stream()
                .map(this::convertToDto)
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> findExpensesByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return expenseRepository.findByUserAndAmountBetween(currentUser, minAmount, maxAmount).stream()
                .map(this::convertToDto)
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = expenseRepository.sumAmountByUser(currentUser);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpensesForPeriod(LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = expenseRepository.sumAmountByUserAndDateBetween(currentUser, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Map<CategoryDto, BigDecimal> getExpensesByCategory(LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new HashMap<>();
        }

        List<Object[]> results = expenseRepository.findExpensesSumByCategory(currentUser, startDate, endDate);
        Map<CategoryDto, BigDecimal> categoryExpenses = new LinkedHashMap<>();

        for (Object[] result : results) {
            Category category = (Category) result[0];
            BigDecimal amount = (BigDecimal) result[1];

            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(category.getId());
            categoryDto.setName(category.getName());
            categoryDto.setDescription(category.getDescription());
            categoryDto.setColorCode(category.getColorCode());
            categoryDto.setIsDefault(category.getIsDefault());

            categoryExpenses.put(categoryDto, amount);
        }

        return categoryExpenses;
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyExpenses(Integer year) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new HashMap<>();
        }

        Map<String, BigDecimal> monthlyExpenses = new LinkedHashMap<>();
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            BigDecimal monthTotal = getTotalExpensesForPeriod(startDate, endDate);
            monthlyExpenses.put(months[month - 1], monthTotal);
        }

        return monthlyExpenses;
    }

    private ExpenseDto convertToDto(Expense expense) {
        ExpenseDto dto = new ExpenseDto();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());
        dto.setDate(expense.getDate());
        dto.setCreatedAt(expense.getCreatedAt());
        dto.setUpdatedAt(expense.getUpdatedAt());

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(expense.getCategory().getId());
        categoryDto.setName(expense.getCategory().getName());
        categoryDto.setDescription(expense.getCategory().getDescription());
        categoryDto.setColorCode(expense.getCategory().getColorCode());
        categoryDto.setIsDefault(expense.getCategory().getIsDefault());
        dto.setCategory(categoryDto);

        return dto;
    }
}