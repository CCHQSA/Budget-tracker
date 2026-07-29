package com.cchqsa.budgetTracker.service;

import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.repository.BudgetRepository;
import com.cchqsa.budgetTracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

@Service
public class ExpensesService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public ExpensesService(ExpenseRepository expenseRepository, BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }


    public void deleteByUserIdAndExpenseId(Long user_id, Long expense_id) {
        expenseRepository.deleteByUserIdAndExpenseId(user_id, expense_id);
    }

    public Category getCategoryById(Long expenseId) {
        return expenseRepository.findCategoryById(expenseId);
    }

    public long countByCategory(Category category) {
        return expenseRepository.countByCategory(category);
    }
}
