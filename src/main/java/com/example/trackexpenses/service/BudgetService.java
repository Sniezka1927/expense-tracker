package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.BudgetCreateDto;
import com.example.trackexpenses.dto.BudgetDto;
import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.entity.Budget;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.repository.BudgetRepository;
import com.example.trackexpenses.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<BudgetDto> findAllBudgets() {
        return budgetRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> findBudgetsByCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return budgetRepository.findByUser(currentUser).stream()
                .map(this::convertToDto)
                .sorted((b1, b2) -> {
                    int yearCompare = b2.getYear().compareTo(b1.getYear());
                    if (yearCompare != 0) return yearCompare;
                    return b2.getMonth().compareTo(b1.getMonth());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> findBudgetsByYear(Integer year) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return budgetRepository.findByUserAndYear(currentUser, year).stream()
                .map(this::convertToDto)
                .sorted((b1, b2) -> b1.getMonth().compareTo(b2.getMonth()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> findBudgetsByYearAndMonth(Integer year, Integer month) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        return budgetRepository.findByUserAndYearAndMonth(currentUser, year, month).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BudgetDto> findById(Integer id) {
        return budgetRepository.findById(id)
                .map(this::convertToDto);
    }

    public BudgetDto createBudget(BudgetCreateDto budgetCreateDto) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        Category category = categoryService.findCategoryById(budgetCreateDto.getCategoryId());

        Optional<Budget> existingBudget = budgetRepository.findByUserAndCategoryAndYearAndMonth(
                currentUser, category, budgetCreateDto.getYear(), budgetCreateDto.getMonth());

        if (existingBudget.isPresent()) {
            throw new RuntimeException("Budget already exists for this category and period");
        }

        if (budgetCreateDto.getMonth() < 1 || budgetCreateDto.getMonth() > 12) {
            throw new RuntimeException("Month must be between 1 and 12");
        }

        if (budgetCreateDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Budget amount must be positive");
        }

        Budget budget = new Budget();
        budget.setAmount(budgetCreateDto.getAmount());
        budget.setCategory(category);
        budget.setUser(currentUser);
        budget.setMonth(budgetCreateDto.getMonth());
        budget.setYear(budgetCreateDto.getYear());
        budget.setCreatedAt(LocalDateTime.now());

        Budget savedBudget = budgetRepository.save(budget);
        return convertToDto(savedBudget);
    }

    public BudgetDto updateBudget(Integer id, BudgetCreateDto budgetCreateDto) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        User currentUser = userService.getCurrentUser();
        if (currentUser == null || !budget.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to update this budget");
        }

        Category category = categoryService.findCategoryById(budgetCreateDto.getCategoryId());

        Optional<Budget> existingBudget = budgetRepository.findByUserAndCategoryAndYearAndMonth(
                currentUser, category, budgetCreateDto.getYear(), budgetCreateDto.getMonth());

        if (existingBudget.isPresent() && !existingBudget.get().getId().equals(id)) {
            throw new RuntimeException("Budget already exists for this category and period");
        }

        if (budgetCreateDto.getMonth() < 1 || budgetCreateDto.getMonth() > 12) {
            throw new RuntimeException("Month must be between 1 and 12");
        }

        if (budgetCreateDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Budget amount must be positive");
        }

        budget.setAmount(budgetCreateDto.getAmount());
        budget.setCategory(category);
        budget.setMonth(budgetCreateDto.getMonth());
        budget.setYear(budgetCreateDto.getYear());
        budget.setUpdatedAt(LocalDateTime.now());

        Budget savedBudget = budgetRepository.save(budget);
        return convertToDto(savedBudget);
    }

    public void deleteBudget(Integer id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        User currentUser = userService.getCurrentUser();
        if (currentUser == null || !budget.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to delete this budget");
        }

        budgetRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public BigDecimal getTotalBudgetForMonth(Integer year, Integer month) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }

        return budgetRepository.findByUserAndYearAndMonth(currentUser, year, month).stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal getSpentAmountForBudget(Integer budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        User currentUser = userService.getCurrentUser();
        if (currentUser == null || !budget.getUser().getId().equals(currentUser.getId())) {
            return BigDecimal.ZERO;
        }

        LocalDate startDate = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal spent = expenseRepository.sumAmountByUserAndCategoryAndDateBetween(
                currentUser, budget.getCategory(), startDate, endDate);

        return spent != null ? spent : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getRemainingBudgetForCategory(Integer categoryId, Integer year, Integer month) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return BigDecimal.ZERO;
        }

        Category category = categoryService.findCategoryById(categoryId);

        Optional<Budget> budget = budgetRepository.findByUserAndCategoryAndYearAndMonth(
                currentUser, category, year, month);

        if (budget.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal budgetAmount = budget.get().getAmount();
        BigDecimal spentAmount = getSpentAmountForBudget(budget.get().getId());

        return budgetAmount.subtract(spentAmount);
    }

    private BudgetDto convertToDto(Budget budget) {
        BudgetDto dto = new BudgetDto();
        dto.setId(budget.getId());
        dto.setAmount(budget.getAmount());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(budget.getCategory().getId());
        categoryDto.setName(budget.getCategory().getName());
        categoryDto.setDescription(budget.getCategory().getDescription());
        categoryDto.setColorCode(budget.getCategory().getColorCode());
        categoryDto.setIsDefault(budget.getCategory().getIsDefault());
        dto.setCategory(categoryDto);

        return dto;
    }
}