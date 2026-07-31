package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.dto.CategorySpentDto;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.service.CategoryService;
import com.cchqsa.budgetTracker.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class CategoryController {

    private final UserService userService;
    private final CategoryService categoryService;

    public CategoryController(UserService userService, CategoryService categoryService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @Transactional
    @GetMapping("/categories")
    public String viewCategories(@AuthenticationPrincipal UserDetails userDetails,
                                 @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                                 Model model) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();

        Page<Category> userCategoriesPage = categoryService.findByUserId(user.getId(), pageable);

        Page<CategorySpentDto> categoriesWithSpentPage = userCategoriesPage.map(category -> {
            BigDecimal totalSpent = category.getExpenses().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new CategorySpentDto(category.getId(), category.getName(), totalSpent);
        });

        model.addAttribute("categoryPage", categoriesWithSpentPage);
        model.addAttribute("categories", categoriesWithSpentPage.getContent());
        return "categories";
    }

    @Transactional
    @GetMapping("/view-category-expenses")
    public String viewCategoryExpenses(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam Long categoryId,
                                       Model model) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Category> category = categoryService.findByIdAndUserId(categoryId, currentUser.get().getId());
        if (category.isEmpty()) {
            return "redirect:/categories";
        }

        Category currentCategory = category.get();
        List<Expense> expenses = currentCategory.getExpenses();

        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("category", currentCategory);
        model.addAttribute("expenses", expenses);
        model.addAttribute("totalSpent", totalSpent);
        return "view-category-expenses";
    }
}
