package com.cchqsa.budgetTracker.service;

import com.cchqsa.budgetTracker.dto.CategorySpentDto;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.repository.CategoryRepository;
import com.cchqsa.budgetTracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category save(Category newCategory) {
        return categoryRepository.save(newCategory);
    }

    public void deleteCategory(Category category) {
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findFirstByUserAndNameIgnoreCase(User user, String normalizedCategoryName) {
        return categoryRepository.findFirstByUserAndNameIgnoreCase(user, normalizedCategoryName);
    }

    @Transactional(readOnly = true)
    public List<Category> findByUser(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean expenseIsPresent(Long expenseId) {
        return categoryRepository.existsById(expenseId);
    }

    @Transactional(readOnly = true)
    public List<Category> findByUserId(Long id) {
        return categoryRepository.findByUserId(id);
    }

    @Transactional(readOnly = true)
    public Page<Category> findByUserId(Long id, Pageable pageable) {
        return categoryRepository.findByUserId(id, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByIdAndUserId(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId);
    }

    @Transactional(readOnly = true)
    public Page<CategorySpentDto> findCategoriesWithSpentByUserId(Long userId, Pageable pageable) {
        return categoryRepository.findCategoriesWithSpentByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CategorySpentDto> findCategoriesWithSpentByUserIdAndName(Long id, String query, Pageable pageable) {
        return categoryRepository.findCategoriesWithSpentByUserIdAndName(id, query, pageable);
    }

}
