package com.example.trackexpenses.service;

import com.example.trackexpenses.dto.CategoryDto;
import com.example.trackexpenses.dto.ExpenseCreateDto;
import com.example.trackexpenses.dto.ExpenseDto;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.entity.Expense;
import com.example.trackexpenses.entity.User;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Category testCategory;
    private Expense testExpense;
    private ExpenseCreateDto expenseCreateDto;
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate today = LocalDate.now();

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

        
        testExpense = new Expense();
        testExpense.setId(1);
        testExpense.setAmount(new BigDecimal("100.00"));
        testExpense.setDescription("Test Expense");
        testExpense.setDate(today);
        testExpense.setCategory(testCategory);
        testExpense.setUser(testUser);
        testExpense.setCreatedAt(now);

        
        expenseCreateDto = new ExpenseCreateDto();
        expenseCreateDto.setAmount(new BigDecimal("100.00"));
        expenseCreateDto.setDescription("Test Expense");
        expenseCreateDto.setDate(today);
        expenseCreateDto.setCategoryId(1);
    }

    @Test
    void findAllExpenses_ShouldReturnAllExpenses() {
        
        Expense expense2 = new Expense();
        expense2.setId(2);
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setDescription("Expense 2");
        expense2.setDate(today);
        expense2.setCategory(testCategory);
        expense2.setUser(testUser);
        expense2.setCreatedAt(now);

        when(expenseRepository.findAll()).thenReturn(Arrays.asList(testExpense, expense2));

        
        List<ExpenseDto> result = expenseService.findAllExpenses();

        
        assertEquals(2, result.size());
        assertEquals(testExpense.getId(), result.get(0).getId());
        assertEquals(testExpense.getAmount(), result.get(0).getAmount());
        assertEquals(expense2.getId(), result.get(1).getId());
        assertEquals(expense2.getAmount(), result.get(1).getAmount());
    }

    @Test
    void findExpensesByCurrentUser_ShouldReturnUserExpenses() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.findByUser(testUser)).thenReturn(Collections.singletonList(testExpense));

        
        List<ExpenseDto> result = expenseService.findExpensesByCurrentUser();

        
        assertEquals(1, result.size());
        assertEquals(testExpense.getId(), result.get(0).getId());
        assertEquals(testExpense.getAmount(), result.get(0).getAmount());
        assertEquals(testExpense.getDescription(), result.get(0).getDescription());
        assertEquals(testExpense.getDate(), result.get(0).getDate());
        assertEquals(testExpense.getCategory().getId(), result.get(0).getCategory().getId());
        assertEquals(testExpense.getCategory().getName(), result.get(0).getCategory().getName());
    }

    @Test
    void findExpensesByCurrentUser_ShouldReturnEmptyListWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        List<ExpenseDto> result = expenseService.findExpensesByCurrentUser();

        
        assertTrue(result.isEmpty());
        verify(expenseRepository, never()).findByUser(any(User.class));
    }

    @Test
    void findById_ShouldReturnExpenseWhenExists() {
        
        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));

        
        Optional<ExpenseDto> result = expenseService.findById(testExpense.getId());

        
        assertTrue(result.isPresent());
        assertEquals(testExpense.getId(), result.get().getId());
        assertEquals(testExpense.getAmount(), result.get().getAmount());
        assertEquals(testExpense.getDescription(), result.get().getDescription());
        assertEquals(testExpense.getDate(), result.get().getDate());
        assertEquals(testExpense.getCategory().getId(), result.get().getCategory().getId());
        assertEquals(testExpense.getCategory().getName(), result.get().getCategory().getName());
    }

    @Test
    void findById_ShouldReturnEmptyWhenExpenseDoesNotExist() {
        
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        
        Optional<ExpenseDto> result = expenseService.findById(999);

        
        assertFalse(result.isPresent());
    }

    @Test
    void createExpense_ShouldCreateAndReturnExpense() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(expenseCreateDto.getCategoryId())).thenReturn(testCategory);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense savedExpense = invocation.getArgument(0);
            savedExpense.setId(1);
            return savedExpense;
        });

        
        ExpenseDto result = expenseService.createExpense(expenseCreateDto);

        
        assertNotNull(result);
        assertEquals(expenseCreateDto.getAmount(), result.getAmount());
        assertEquals(expenseCreateDto.getDescription(), result.getDescription());
        assertEquals(expenseCreateDto.getDate(), result.getDate());
        assertEquals(testCategory.getId(), result.getCategory().getId());
        assertEquals(testCategory.getName(), result.getCategory().getName());

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());
        Expense savedExpense = expenseCaptor.getValue();
        assertEquals(expenseCreateDto.getAmount(), savedExpense.getAmount());
        assertEquals(expenseCreateDto.getDescription(), savedExpense.getDescription());
        assertEquals(expenseCreateDto.getDate(), savedExpense.getDate());
        assertEquals(testCategory, savedExpense.getCategory());
        assertEquals(testUser, savedExpense.getUser());
        assertNotNull(savedExpense.getCreatedAt());
    }

    @Test
    void createExpense_ShouldThrowExceptionWhenUserNotAuthenticated() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        assertThrows(RuntimeException.class, () -> expenseService.createExpense(expenseCreateDto));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void updateExpense_ShouldUpdateAndReturnExpense() {
        
        ExpenseCreateDto updateDto = new ExpenseCreateDto();
        updateDto.setAmount(new BigDecimal("150.00"));
        updateDto.setDescription("Updated Expense");
        updateDto.setDate(today.plusDays(1));
        updateDto.setCategoryId(1);

        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(updateDto.getCategoryId())).thenReturn(testCategory);
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);

        
        ExpenseDto result = expenseService.updateExpense(testExpense.getId(), updateDto);

        
        assertNotNull(result);
        assertEquals(updateDto.getAmount(), result.getAmount());
        assertEquals(updateDto.getDescription(), result.getDescription());
        assertEquals(updateDto.getDate(), result.getDate());

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());
        Expense savedExpense = expenseCaptor.getValue();
        assertEquals(updateDto.getAmount(), savedExpense.getAmount());
        assertEquals(updateDto.getDescription(), savedExpense.getDescription());
        assertEquals(updateDto.getDate(), savedExpense.getDate());
        assertEquals(testCategory, savedExpense.getCategory());
        assertNotNull(savedExpense.getUpdatedAt());
    }

    @Test
    void updateExpense_ShouldThrowExceptionWhenExpenseNotFound() {
        
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> expenseService.updateExpense(999, expenseCreateDto));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void updateExpense_ShouldThrowExceptionWhenUserNotAuthenticated() {
        
        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));
        when(userService.getCurrentUser()).thenReturn(null);

        
        assertThrows(RuntimeException.class, () -> expenseService.updateExpense(testExpense.getId(), expenseCreateDto));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void updateExpense_ShouldThrowExceptionWhenNotAuthorized() {
        
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");

        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        
        assertThrows(RuntimeException.class, () -> expenseService.updateExpense(testExpense.getId(), expenseCreateDto));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void deleteExpense_ShouldDeleteExpenseWhenExists() {
        
        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));
        when(userService.getCurrentUser()).thenReturn(testUser);
        doNothing().when(expenseRepository).deleteById(testExpense.getId());

        
        expenseService.deleteExpense(testExpense.getId());

        
        verify(expenseRepository).deleteById(testExpense.getId());
    }

    @Test
    void deleteExpense_ShouldThrowExceptionWhenExpenseNotFound() {
        
        when(expenseRepository.findById(999)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> expenseService.deleteExpense(999));
        verify(expenseRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteExpense_ShouldThrowExceptionWhenUserNotAuthenticated() {
        
        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));
        when(userService.getCurrentUser()).thenReturn(null);

        
        assertThrows(RuntimeException.class, () -> expenseService.deleteExpense(testExpense.getId()));
        verify(expenseRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteExpense_ShouldThrowExceptionWhenNotAuthorized() {
        
        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("otheruser");

        when(expenseRepository.findById(testExpense.getId())).thenReturn(Optional.of(testExpense));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        
        assertThrows(RuntimeException.class, () -> expenseService.deleteExpense(testExpense.getId()));
        verify(expenseRepository, never()).deleteById(anyInt());
    }

    @Test
    void findExpensesByDateRange_ShouldReturnExpensesInRange() {
        
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.findByUserAndDateBetween(testUser, startDate, endDate))
                .thenReturn(Collections.singletonList(testExpense));

        
        List<ExpenseDto> result = expenseService.findExpensesByDateRange(startDate, endDate);

        
        assertEquals(1, result.size());
        assertEquals(testExpense.getId(), result.get(0).getId());
        assertEquals(testExpense.getAmount(), result.get(0).getAmount());
    }

    @Test
    void findExpensesByDateRange_ShouldReturnEmptyListWhenNoUser() {
        
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;

        when(userService.getCurrentUser()).thenReturn(null);

        
        List<ExpenseDto> result = expenseService.findExpensesByDateRange(startDate, endDate);

        
        assertTrue(result.isEmpty());
        verify(expenseRepository, never()).findByUserAndDateBetween(any(), any(), any());
    }

    @Test
    void findExpensesByCategory_ShouldReturnExpensesForCategory() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(categoryService.findCategoryById(testCategory.getId())).thenReturn(testCategory);
        when(expenseRepository.findByUserAndCategory(testUser, testCategory))
                .thenReturn(Collections.singletonList(testExpense));

        
        List<ExpenseDto> result = expenseService.findExpensesByCategory(testCategory.getId());

        
        assertEquals(1, result.size());
        assertEquals(testExpense.getId(), result.get(0).getId());
        assertEquals(testExpense.getAmount(), result.get(0).getAmount());
        assertEquals(testCategory.getId(), result.get(0).getCategory().getId());
    }

    @Test
    void findExpensesByCategory_ShouldReturnEmptyListWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        List<ExpenseDto> result = expenseService.findExpensesByCategory(testCategory.getId());

        
        assertTrue(result.isEmpty());
        verify(categoryService, never()).findCategoryById(anyInt());
        verify(expenseRepository, never()).findByUserAndCategory(any(), any());
    }

    @Test
    void findExpensesByAmountRange_ShouldReturnExpensesInRange() {
        
        BigDecimal minAmount = new BigDecimal("50.00");
        BigDecimal maxAmount = new BigDecimal("150.00");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.findByUserAndAmountBetween(testUser, minAmount, maxAmount))
                .thenReturn(Collections.singletonList(testExpense));

        
        List<ExpenseDto> result = expenseService.findExpensesByAmountRange(minAmount, maxAmount);

        
        assertEquals(1, result.size());
        assertEquals(testExpense.getId(), result.get(0).getId());
        assertEquals(testExpense.getAmount(), result.get(0).getAmount());
    }

    @Test
    void findExpensesByAmountRange_ShouldReturnEmptyListWhenNoUser() {
        
        BigDecimal minAmount = new BigDecimal("50.00");
        BigDecimal maxAmount = new BigDecimal("150.00");

        when(userService.getCurrentUser()).thenReturn(null);

        
        List<ExpenseDto> result = expenseService.findExpensesByAmountRange(minAmount, maxAmount);

        
        assertTrue(result.isEmpty());
        verify(expenseRepository, never()).findByUserAndAmountBetween(any(), any(), any());
    }

    @Test
    void getTotalExpensesForCurrentUser_ShouldReturnTotal() {
        
        BigDecimal total = new BigDecimal("500.00");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.sumAmountByUser(testUser)).thenReturn(total);

        
        BigDecimal result = expenseService.getTotalExpensesForCurrentUser();

        
        assertEquals(total, result);
    }

    @Test
    void getTotalExpensesForCurrentUser_ShouldReturnZeroWhenNoUser() {
        
        when(userService.getCurrentUser()).thenReturn(null);

        
        BigDecimal result = expenseService.getTotalExpensesForCurrentUser();

        
        assertEquals(BigDecimal.ZERO, result);
        verify(expenseRepository, never()).sumAmountByUser(any());
    }

    @Test
    void getTotalExpensesForCurrentUser_ShouldReturnZeroWhenNoExpenses() {
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.sumAmountByUser(testUser)).thenReturn(null);

        
        BigDecimal result = expenseService.getTotalExpensesForCurrentUser();

        
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getTotalExpensesForPeriod_ShouldReturnTotal() {
        
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today;
        BigDecimal total = new BigDecimal("300.00");

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.sumAmountByUserAndDateBetween(testUser, startDate, endDate)).thenReturn(total);

        
        BigDecimal result = expenseService.getTotalExpensesForPeriod(startDate, endDate);

        
        assertEquals(total, result);
    }

    @Test
    void getTotalExpensesForPeriod_ShouldReturnZeroWhenNoUser() {
        
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today;

        when(userService.getCurrentUser()).thenReturn(null);

        
        BigDecimal result = expenseService.getTotalExpensesForPeriod(startDate, endDate);

        
        assertEquals(BigDecimal.ZERO, result);
        verify(expenseRepository, never()).sumAmountByUserAndDateBetween(any(), any(), any());
    }

    @Test
    void getTotalExpensesForPeriod_ShouldReturnZeroWhenNoExpenses() {
        
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today;

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.sumAmountByUserAndDateBetween(testUser, startDate, endDate)).thenReturn(null);

        
        BigDecimal result = expenseService.getTotalExpensesForPeriod(startDate, endDate);

        
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getExpensesByCategory_ShouldReturnCategoryMap() {
        
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today;

        Category category2 = new Category();
        category2.setId(2);
        category2.setName("Category 2");
        category2.setDescription("Description 2");
        category2.setColorCode("#28a745");
        category2.setIsDefault(true);

        List<Object[]> results = Arrays.asList(
                new Object[]{testCategory, new BigDecimal("300.00")},
                new Object[]{category2, new BigDecimal("200.00")}
        );

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.findExpensesSumByCategory(testUser, startDate, endDate)).thenReturn(results);

        
        Map<CategoryDto, BigDecimal> result = expenseService.getExpensesByCategory(startDate, endDate);

        
        assertEquals(2, result.size());

        
        List<Map.Entry<CategoryDto, BigDecimal>> entries = new ArrayList<>(result.entrySet());

        assertEquals(testCategory.getId(), entries.get(0).getKey().getId());
        assertEquals(testCategory.getName(), entries.get(0).getKey().getName());
        assertEquals(new BigDecimal("300.00"), entries.get(0).getValue());

        assertEquals(category2.getId(), entries.get(1).getKey().getId());
        assertEquals(category2.getName(), entries.get(1).getKey().getName());
        assertEquals(new BigDecimal("200.00"), entries.get(1).getValue());
    }

    @Test
    void getExpensesByCategory_ShouldReturnEmptyMapWhenNoUser() {
        
        LocalDate startDate = today.minusDays(30);
        LocalDate endDate = today;

        when(userService.getCurrentUser()).thenReturn(null);

        
        Map<CategoryDto, BigDecimal> result = expenseService.getExpensesByCategory(startDate, endDate);

        
        assertTrue(result.isEmpty());
        verify(expenseRepository, never()).findExpensesSumByCategory(any(), any(), any());
    }

    @Test
    void getMonthlyExpenses_ShouldReturnMonthlyMap() {
        
        int year = 2023;
        BigDecimal januaryTotal = new BigDecimal("100.00");
        BigDecimal februaryTotal = new BigDecimal("200.00");

        when(userService.getCurrentUser()).thenReturn(testUser);

        
        doAnswer(invocation -> {
            LocalDate startDate = invocation.getArgument(1); 
            LocalDate endDate = invocation.getArgument(2);   
            if (startDate.getMonthValue() == 1) {
                return januaryTotal;
            } else if (startDate.getMonthValue() == 2) {
                return februaryTotal;
            }
            return BigDecimal.ZERO;
        }).when(expenseRepository).sumAmountByUserAndDateBetween(eq(testUser), any(LocalDate.class), any(LocalDate.class));

        
        Map<String, BigDecimal> result = expenseService.getMonthlyExpenses(year);

        
        assertEquals(12, result.size());
        assertEquals(januaryTotal, result.get("January"));
        assertEquals(februaryTotal, result.get("February"));
        assertEquals(BigDecimal.ZERO, result.get("March"));
        
    }

    @Test
    void getMonthlyExpenses_ShouldReturnEmptyMapWhenNoUser() {
        
        int year = 2023;
        when(userService.getCurrentUser()).thenReturn(null);

        
        Map<String, BigDecimal> result = expenseService.getMonthlyExpenses(year);

        
        assertTrue(result.isEmpty());
    }
}
