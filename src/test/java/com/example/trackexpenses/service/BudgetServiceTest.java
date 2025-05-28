package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.BudgetCreateDto;
import com.example.trackexpenses.dto.BudgetDto;
import com.example.trackexpenses.entity.Budget;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.entity.User;
import com.example.trackexpenses.repository.BudgetRepository;
import com.example.trackexpenses.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category testCategory;
    private Budget testBudget;
    private BudgetCreateDto budgetCreateDto;
    private final LocalDateTime now = LocalDateTime.now();
    private final int testYear = 2023;
    private final int testMonth = 5;

    @BeforeEach
    void setUp() {
        
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");

        
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Test Category");
        testCategory.setDescription("Test Description");
        testCategory.setColorCode("#007bff");
        testCategory.setIsDefault(false);

        
        testBudget = new Budget();
        testBudget.setId(1);
        testBudget.setAmount(new BigDecimal("1000.00"));
        testBudget.setCategory(testCategory);
        testBudget.setUser(testUser);
        testBudget.setMonth(testMonth);
        testBudget.setYear(testYear);
        testBudget.setCreatedAt(now);

        
        budgetCreateDto = new BudgetCreateDto();
        budgetCreateDto.setAmount(new BigDecimal("1000.00"));
        budgetCreateDto.setCategoryId(1);
        budgetCreateDto.setMonth(testMonth);
        budgetCreateDto.setYear(testYear);
    }

    @Test
    void findAllBudgets_ShouldReturnAllBudgets() {
        
        Budget budget2 = new Budget();
        budget2.setId(2);
        budget2.setAmount(new BigDecimal("2000.00"));
        budget2.setCategory(testCategory);
        budget2.setUser(testUser);
        budget2.setMonth(6);
        budget2.setYear(testYear);
        budget2.setCreatedAt(now);

        when(budgetRepository.findAll()).thenReturn(Arrays.asList(testBudget, budget2));

        
        List<BudgetDto> result = budgetService.findAllBudgets();

        
        assertEquals(2, result.size());
        assertEquals(testBudget.getId(), result.get(0).getId());
        assertEquals(testBudget.getAmount(), result.get(0).getAmount());
        assertEquals(budget2.getId(), result.get(1).getId());
        assertEquals(budget2.getAmount(), result.get(1).getAmount());
    }

    @Test
    void findBudgetsByCurrentUser_ShouldReturnUserBudgets() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByUser(testUser)).thenReturn(Collections.singletonList(testBudget));

        
        List<BudgetDto> result = budgetService.findBudgetsByCurrentUser();

        
        assertEquals(1, result.size());
        assertEquals(testBudget.getId(), result.get(0).getId());
        assertEquals(testBudget.getAmount(), result.get(0).getAmount());
        assertEquals(testBudget.getCategory().getId(), result.get(0).getCategory().getId());
        assertEquals(testBudget.getMonth(), result.get(0).getMonth());
        assertEquals(testBudget.getYear(), result.get(0).getYear());
    }

    @Test
    void findBudgetsByCurrentUser_ShouldReturnEmptyListWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        List<BudgetDto> result = budgetService.findBudgetsByCurrentUser();

        
        assertTrue(result.isEmpty());
        verify(budgetRepository, never()).findByUser(any(User.class));
    }

    @Test
    void findBudgetsByYear_ShouldReturnBudgetsForYear() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByUserAndYear(testUser, testYear)).thenReturn(Collections.singletonList(testBudget));

        
        List<BudgetDto> result = budgetService.findBudgetsByYear(testYear);

        
        assertEquals(1, result.size());
        assertEquals(testBudget.getId(), result.get(0).getId());
        assertEquals(testBudget.getYear(), result.get(0).getYear());
    }

    @Test
    void findBudgetsByYear_ShouldReturnEmptyListWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        List<BudgetDto> result = budgetService.findBudgetsByYear(testYear);

        
        assertTrue(result.isEmpty());
        verify(budgetRepository, never()).findByUserAndYear(any(User.class), anyInt());
    }

    @Test
    void findBudgetsByYearAndMonth_ShouldReturnBudgetsForYearAndMonth() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByUserAndYearAndMonth(testUser, testYear, testMonth))
                .thenReturn(Collections.singletonList(testBudget));

        
        List<BudgetDto> result = budgetService.findBudgetsByYearAndMonth(testYear, testMonth);

        
        assertEquals(1, result.size());
        assertEquals(testBudget.getId(), result.get(0).getId());
        assertEquals(testBudget.getYear(), result.get(0).getYear());
        assertEquals(testBudget.getMonth(), result.get(0).getMonth());
    }

    @Test
    void findBudgetsByYearAndMonth_ShouldReturnEmptyListWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        List<BudgetDto> result = budgetService.findBudgetsByYearAndMonth(testYear, testMonth);

        
        assertTrue(result.isEmpty());
        verify(budgetRepository, never()).findByUserAndYearAndMonth(any(User.class), anyInt(), anyInt());
    }

    @Test
    void findById_ShouldReturnBudgetWhenExists() {
        
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));

        
        Optional<BudgetDto> result = budgetService.findById(testBudget.getId());

        
        assertTrue(result.isPresent());
        assertEquals(testBudget.getId(), result.get().getId());
        assertEquals(testBudget.getAmount(), result.get().getAmount());
        assertEquals(testBudget.getCategory().getId(), result.get().getCategory().getId());
        assertEquals(testBudget.getMonth(), result.get().getMonth());
        assertEquals(testBudget.getYear(), result.get().getYear());
    }

    @Test
    void findById_ShouldReturnEmptyWhenBudgetDoesNotExist() {
        
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        
        Optional<BudgetDto> result = budgetService.findById(999);

        
        assertFalse(result.isPresent());
    }

    @Test
    void createBudget_ShouldCreateAndReturnBudget() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(budgetCreateDto.getCategoryId())).thenReturn(testCategory);
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(
                testUser, testCategory, budgetCreateDto.getYear(), budgetCreateDto.getMonth()))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget savedBudget = invocation.getArgument(0);
            savedBudget.setId(1);
            return savedBudget;
        });

        
        BudgetDto result = budgetService.createBudget(budgetCreateDto);

        
        assertNotNull(result);
        assertEquals(budgetCreateDto.getAmount(), result.getAmount());
        assertEquals(testCategory.getId(), result.getCategory().getId());
        assertEquals(budgetCreateDto.getMonth(), result.getMonth());
        assertEquals(budgetCreateDto.getYear(), result.getYear());

        ArgumentCaptor<Budget> budgetCaptor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(budgetCaptor.capture());
        Budget savedBudget = budgetCaptor.getValue();
        assertEquals(budgetCreateDto.getAmount(), savedBudget.getAmount());
        assertEquals(testCategory, savedBudget.getCategory());
        assertEquals(testUser, savedBudget.getUser());
        assertEquals(budgetCreateDto.getMonth(), savedBudget.getMonth());
        assertEquals(budgetCreateDto.getYear(), savedBudget.getYear());
        assertNotNull(savedBudget.getCreatedAt());
    }

    @Test
    void createBudget_ShouldThrowExceptionWhenUserNotAuthenticated() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        assertThrows(RuntimeException.class, () -> budgetService.createBudget(budgetCreateDto));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldThrowExceptionWhenBudgetAlreadyExists() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(budgetCreateDto.getCategoryId())).thenReturn(testCategory);
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(
                testUser, testCategory, budgetCreateDto.getYear(), budgetCreateDto.getMonth()))
                .thenReturn(Optional.of(testBudget));

        
        assertThrows(RuntimeException.class, () -> budgetService.createBudget(budgetCreateDto));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldThrowExceptionWhenInvalidMonth() {
        
        budgetCreateDto.setMonth(13); 
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(budgetCreateDto.getCategoryId())).thenReturn(testCategory);

        
        assertThrows(RuntimeException.class, () -> budgetService.createBudget(budgetCreateDto));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldThrowExceptionWhenNegativeAmount() {
        
        budgetCreateDto.setAmount(new BigDecimal("-100.00")); 
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(budgetCreateDto.getCategoryId())).thenReturn(testCategory);

        
        assertThrows(RuntimeException.class, () -> budgetService.createBudget(budgetCreateDto));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_ShouldUpdateAndReturnBudget() {
        
        BudgetCreateDto updateDto = new BudgetCreateDto();
        updateDto.setAmount(new BigDecimal("1500.00"));
        updateDto.setCategoryId(1);
        updateDto.setMonth(6);
        updateDto.setYear(testYear);

        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(updateDto.getCategoryId())).thenReturn(testCategory);
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(
                testUser, testCategory, updateDto.getYear(), updateDto.getMonth()))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        
        BudgetDto result = budgetService.updateBudget(testBudget.getId(), updateDto);

        
        assertNotNull(result);
        assertEquals(updateDto.getAmount(), result.getAmount());
        assertEquals(updateDto.getMonth(), result.getMonth());
        assertEquals(updateDto.getYear(), result.getYear());

        ArgumentCaptor<Budget> budgetCaptor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(budgetCaptor.capture());
        Budget savedBudget = budgetCaptor.getValue();
        assertEquals(updateDto.getAmount(), savedBudget.getAmount());
        assertEquals(updateDto.getMonth(), savedBudget.getMonth());
        assertEquals(updateDto.getYear(), savedBudget.getYear());
        assertNotNull(savedBudget.getUpdatedAt());
    }

    @Test
    void updateBudget_ShouldThrowExceptionWhenBudgetNotFound() {
        
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> budgetService.updateBudget(999, budgetCreateDto));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_ShouldThrowExceptionWhenUserNotAuthenticated() {
        
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(null);

        
        assertThrows(RuntimeException.class, () -> budgetService.updateBudget(testBudget.getId(), budgetCreateDto));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_ShouldThrowExceptionWhenNotAuthorized() {
        
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");

        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        
        assertThrows(RuntimeException.class, () -> budgetService.updateBudget(testBudget.getId(), budgetCreateDto));
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void deleteBudget_ShouldDeleteBudgetWhenExists() {
        
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(budgetRepository).deleteById(testBudget.getId());

        
        budgetService.deleteBudget(testBudget.getId());

        
        verify(budgetRepository).deleteById(testBudget.getId());
    }

    @Test
    void deleteBudget_ShouldThrowExceptionWhenBudgetNotFound() {
        
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> budgetService.deleteBudget(999));
        verify(budgetRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteBudget_ShouldThrowExceptionWhenUserNotAuthenticated() {
        
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(null);

        
        assertThrows(RuntimeException.class, () -> budgetService.deleteBudget(testBudget.getId()));
        verify(budgetRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteBudget_ShouldThrowExceptionWhenNotAuthorized() {
        
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");

        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        
        assertThrows(RuntimeException.class, () -> budgetService.deleteBudget(testBudget.getId()));
        verify(budgetRepository, never()).deleteById(anyInt());
    }

    @Test
    void getTotalBudgetForMonth_ShouldReturnTotal() {
        
        Budget budget2 = new Budget();
        budget2.setId(2);
        budget2.setAmount(new BigDecimal("500.00"));
        budget2.setCategory(testCategory);
        budget2.setUser(testUser);
        budget2.setMonth(testMonth);
        budget2.setYear(testYear);

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(budgetRepository.findByUserAndYearAndMonth(testUser, testYear, testMonth))
                .thenReturn(Arrays.asList(testBudget, budget2));

        
        BigDecimal result = budgetService.getTotalBudgetForMonth(testYear, testMonth);

        
        assertEquals(new BigDecimal("1500.00"), result);
    }

    @Test
    void getTotalBudgetForMonth_ShouldReturnZeroWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        BigDecimal result = budgetService.getTotalBudgetForMonth(testYear, testMonth);

        
        assertEquals(BigDecimal.ZERO, result);
        verify(budgetRepository, never()).findByUserAndYearAndMonth(any(), anyInt(), anyInt());
    }

    @Test
    void getSpentAmountForBudget_ShouldReturnSpentAmount() {
        
        BigDecimal spentAmount = new BigDecimal("500.00");
        LocalDate startDate = LocalDate.of(testYear, testMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.sumAmountByUserAndCategoryAndDateBetween(
                testUser, testCategory, startDate, endDate)).thenReturn(spentAmount);

        
        BigDecimal result = budgetService.getSpentAmountForBudget(testBudget.getId());

        
        assertEquals(spentAmount, result);
    }

    @Test
    void getSpentAmountForBudget_ShouldReturnZeroWhenBudgetNotFound() {
        
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> budgetService.getSpentAmountForBudget(999));
    }

    @Test
    void getSpentAmountForBudget_ShouldReturnZeroWhenUserNotAuthenticated() {
        
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(null);

        
        BigDecimal result = budgetService.getSpentAmountForBudget(testBudget.getId());

        
        assertEquals(BigDecimal.ZERO, result);
        verify(expenseRepository, never()).sumAmountByUserAndCategoryAndDateBetween(any(), any(), any(), any());
    }

    @Test
    void getSpentAmountForBudget_ShouldReturnZeroWhenNotAuthorized() {
        
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");

        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        
        BigDecimal result = budgetService.getSpentAmountForBudget(testBudget.getId());

        
        assertEquals(BigDecimal.ZERO, result);
        verify(expenseRepository, never()).sumAmountByUserAndCategoryAndDateBetween(any(), any(), any(), any());
    }

    @Test
    void getSpentAmountForBudget_ShouldReturnZeroWhenNoExpenses() {
        
        LocalDate startDate = LocalDate.of(testYear, testMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.sumAmountByUserAndCategoryAndDateBetween(
                testUser, testCategory, startDate, endDate)).thenReturn(null);

        
        BigDecimal result = budgetService.getSpentAmountForBudget(testBudget.getId());

        
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getRemainingBudgetForCategory_ShouldReturnRemainingAmount() {
        
        BigDecimal budgetAmount = new BigDecimal("1000.00");
        BigDecimal spentAmount = new BigDecimal("400.00");
        BigDecimal expectedRemaining = new BigDecimal("600.00");
        LocalDate startDate = LocalDate.of(testYear, testMonth, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(testCategory.getId())).thenReturn(testCategory);
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(
                testUser, testCategory, testYear, testMonth)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.findById(testBudget.getId())).thenReturn(Optional.of(testBudget));
        when(expenseRepository.sumAmountByUserAndCategoryAndDateBetween(
                testUser, testCategory, startDate, endDate)).thenReturn(spentAmount);

        
        BigDecimal result = budgetService.getRemainingBudgetForCategory(testCategory.getId(), testYear, testMonth);

        
        assertEquals(expectedRemaining, result);
    }

    @Test
    void getRemainingBudgetForCategory_ShouldReturnZeroWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        BigDecimal result = budgetService.getRemainingBudgetForCategory(testCategory.getId(), testYear, testMonth);

        
        assertEquals(BigDecimal.ZERO, result);
        verify(categoryService, never()).findCategoryById(anyInt());
    }

    @Test
    void getRemainingBudgetForCategory_ShouldReturnZeroWhenNoBudget() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(testCategory.getId())).thenReturn(testCategory);
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(
                testUser, testCategory, testYear, testMonth)).thenReturn(Optional.empty());

        
        BigDecimal result = budgetService.getRemainingBudgetForCategory(testCategory.getId(), testYear, testMonth);

        
        assertEquals(BigDecimal.ZERO, result);
        verify(expenseRepository, never()).sumAmountByUserAndCategoryAndDateBetween(any(), any(), any(), any());
    }
}
