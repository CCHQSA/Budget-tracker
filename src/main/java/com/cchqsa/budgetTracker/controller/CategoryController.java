package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.dto.CategorySpentDto;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.service.CategoryService;
import com.cchqsa.budgetTracker.service.UserService;
import jakarta.transaction.Transactional;
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
    public String viewCategories(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();

        List<Category> userCategories = categoryService.findByUserId(user.getId());

        List<CategorySpentDto> categoriesWithSpent = userCategories.stream()
                .map(category -> {
                    BigDecimal totalSpent = category.getExpenses().stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new CategorySpentDto(category.getId(), category.getName(), totalSpent);
                })
                .toList();

        model.addAttribute("categories", categoriesWithSpent);
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

        BigDecimal totalSpent = category.get().getExpenses().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("category", category.get());
        model.addAttribute("totalSpent", totalSpent);
        return "view-category-expenses";
    }
}
