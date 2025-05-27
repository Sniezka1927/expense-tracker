package com.example.trackexpenses.repository;

import com.example.trackexpenses.entity.Budget;
import com.example.trackexpenses.entity.Category;
import com.example.trackexpenses.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    List<Budget> findByUser(User user);

    List<Budget> findByUserAndYear(User user, Integer year);

    List<Budget> findByUserAndYearAndMonth(User user, Integer year, Integer month);

    Optional<Budget> findByUserAndCategoryAndYearAndMonth(User user, Category category, Integer year, Integer month);

    boolean existsByUserAndCategoryAndYearAndMonth(User user, Category category, Integer year, Integer month);
}