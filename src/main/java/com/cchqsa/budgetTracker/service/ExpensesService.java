package com.cchqsa.budgetTracker.service;

import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpensesService {

    private final ExpenseRepository expenseRepository;

    public ExpensesService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
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
}
