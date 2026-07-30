package com.cchqsa.budgetTracker.repository;

import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Long> {
    Optional<Budget> findFirstByUserAndMonthOrderByIdDesc(User user, YearMonth month);

    List<Budget> getBudgetsByUser(User user);

    void deleteByIdAndUser(Long budgetId, User user);

    Optional<Budget> findByIdAndUser(Long budgetId, User user);

    @Query("SELECT e FROM Expense e WHERE e.budget.id = :id AND e.user = :user")
    List<Expense> findBudgetExpenses(@Param("id") Long id, @Param("user") User user);

}
