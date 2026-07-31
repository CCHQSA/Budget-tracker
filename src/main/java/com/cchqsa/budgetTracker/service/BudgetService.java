package com.cchqsa.budgetTracker.service;

import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.repository.BudgetRepository;
import com.cchqsa.budgetTracker.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    public Budget addOrUpdateBudget(User user, YearMonth month, BigDecimal amount) {
        Objects.requireNonNull(user, "user is required");
        Objects.requireNonNull(month, "month is required");
        Objects.requireNonNull(amount, "amount is required");

        Budget budget = findBudget(user, month).orElseGet(Budget::new);
        budget.setUser(user);
        budget.setMonth(month);
        budget.setLimitAmount(amount);
        return budgetRepository.save(budget);
    }

    public Optional<Budget> findBudget(User user, YearMonth month) {
        return budgetRepository.findFirstByUserAndMonthOrderByIdDesc(user, month);
    }

    public BigDecimal getSpent(Budget budget) {
        return expenseRepository.sumAmountByBudget(budget);
    }

    public long getTotalExpenses(Budget budget) {
        return expenseRepository.countByBudget(budget);
    }

    public void saveBudget(Budget budget) {
        budgetRepository.save(budget);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.getAllExpenses();
    }

    public Page<Budget> getBudgetsByUser(User user, Pageable pageable) {
        return budgetRepository.getBudgetsByUser(user, pageable);
    }

    public void deleteBudgetByIdAndUser(Long budgetId, User user) {
        List<Expense> expenses = budgetRepository.findBudgetExpenses(budgetId, user);
        for (Expense expense : expenses) {
            if(expense.getBudget().getUser().equals(user)) {
                expenseRepository.deleteById(expense.getId());
            }
        }
        budgetRepository.deleteByIdAndUser(budgetId, user);
    }

    public Optional<Budget> findByIdAndUser(Long budgetId, User user) {
        return budgetRepository.findByIdAndUser(budgetId, user);
    }

    public List<Expense> findExpensesByIdAndUser(Long budgetId, User user) {
        return budgetRepository.findBudgetExpenses(budgetId, user);
    }

    public Page<Expense> findExpensesByIdAndUser(Long budgetId, User user, Pageable pageable) {
        return expenseRepository.findBudgetExpensesPageable(budgetId, user, pageable);
    }
}
