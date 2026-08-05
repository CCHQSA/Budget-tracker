package com.cchqsa.budgetTracker.service;

import com.cchqsa.budgetTracker.dto.CategorySpentDto;
import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.repository.ExpenseRepository;
import com.cchqsa.budgetTracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ExpensesService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpensesService(ExpenseRepository expenseRepository,
                           UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public Category getCategoryById(Long expenseId) {
        return expenseRepository.findCategoryById(expenseId);
    }

    public void deleteByUserIdAndExpenseId(Long userId, Long expenseId) {
        expenseRepository.deleteByUserIdAndExpenseId(userId, expenseId);
    }

    public long countByCategory(Category category) {
        return expenseRepository.countByCategory(category);
    }

    public Expense findByIdAndUser(Long id, User user) {
        return expenseRepository.findByIdAndUser(id, user);
    }

    public void updateExpense(Expense expense) {
        expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.getAllExpenses();
    }

    public Page<Expense> findByUser(User user, Pageable pageable) {
        return expenseRepository.findByUser(user, pageable);
    }

    public List<Expense> findRecentExpenses(User user) {
        return expenseRepository.findTop3ByUserOrderByIdDesc(user);
    }

    public Page<Expense> search(Long userId,
                                String query,
                                Pageable pageable) {

        if (query == null || query.isBlank()) {
            return expenseRepository.findByUserId(userId, pageable);
        }

        return expenseRepository.findByUserIdAndTitleContainingIgnoreCase(
                userId,
                query,
                pageable
        );
    }

    public BigDecimal getAverageExpense(Long userId) {
        return expenseRepository.getAverageExpense(userId);
    }

    public Expense getMostExpensiveExpense(User user) {
        return expenseRepository
                .findTopByUserOrderByAmountDesc(user)
                .orElse(null);
    }

    public List<Expense> getExpensesByMonth(
            User user,
            YearMonth month) {

        return expenseRepository.findByUserAndDateBetween(
                user,
                month.atDay(1),
                month.atEndOfMonth()
        );
    }

    public List<CategorySpentDto> getTopCategories(Long userId) {
        return expenseRepository.getTopCategories(userId);
    }

    public List<Expense> getExpensesBetweenDates(
            Long userId,
            LocalDate start,
            LocalDate end) {

        return expenseRepository.findByUserIdAndDateBetween(
                userId,
                start,
                end
        );
    }

    public BigDecimal getThisWeekSpent(Long userId) {

        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return expenseRepository.sumExpensesSince(userId, startOfWeek);
    }
}