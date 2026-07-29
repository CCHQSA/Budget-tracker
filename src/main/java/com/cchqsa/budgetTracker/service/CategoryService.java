package com.cchqsa.budgetTracker.service;

import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }



    public Category save(Category newCategory) {
        return categoryRepository.save(newCategory);
    }

    public Optional<Category> findFirstByUserAndNameIgnoreCase(User user, String normalizedCategoryName) {
        return categoryRepository.findFirstByUserAndNameIgnoreCase(user, normalizedCategoryName);
    }

    public List<Category> findByUser(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    public boolean expenseIsPresent(Long expenseId) {
        return categoryRepository.existsById(expenseId);
    }

    public void deleteCategory(Category category) {
        categoryRepository.delete(category);
    }

    public List<Category> findByUserId(Long id) {
        return categoryRepository.findByUserId(id);
    }
}
