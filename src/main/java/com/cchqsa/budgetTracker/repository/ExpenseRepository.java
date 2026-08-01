package com.cchqsa.budgetTracker.repository;

import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.budget = :budget")
    BigDecimal sumAmountByBudget(Budget budget);

    long countByBudget(Budget budget);

    @Modifying
    @Transactional
    @Query("delete from Expense e where e.user.id = :userId and e.id = :expenseId")
    void deleteByUserIdAndExpenseId(Long userId, Long expenseId);

    @Query("select e from Expense e")
    List<Expense> getAllExpenses();

    @Query("select e.category from Expense e where e.id = :expenseId")
    Category findCategoryById(@Param("expenseId") Long expenseId);

    long countByCategory(Category category);

    Expense findByIdAndUser(Long id, User user);

    Page<Expense> findByUser(User user, Pageable pageable);

    @Query("select e from Expense e where e.budget.id = :budgetId and e.user = :user")
    Page<Expense> findBudgetExpensesPageable(@Param("budgetId") Long budgetId, @Param("user") User user, Pageable pageable);

    List<Expense> findTop3ByUserOrderByIdDesc(User user);

    Page<Expense> findByUserIdAndTitleContainingIgnoreCase(Long userId, String query, Pageable pageable);

    Page<Expense> findByUserId(Long userId, Pageable pageable);
}