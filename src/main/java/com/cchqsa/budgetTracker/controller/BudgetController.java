package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.repository.UserRepository;
import com.cchqsa.budgetTracker.service.BudgetService;
import com.cchqsa.budgetTracker.service.CategoryService;
import com.cchqsa.budgetTracker.service.ExpensesService;
import com.cchqsa.budgetTracker.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
public class BudgetController {

    private final UserService userService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final ExpensesService  expensesService;

    public BudgetController(UserService userService, BudgetService budgetService, CategoryService categoryService, ExpensesService expensesService) {
        this.userService = userService;
        this.budgetService = budgetService;
        this.categoryService = categoryService;
        this.expensesService = expensesService;
    }

    @GetMapping("/budget/create")
    public String createBudget(Model model) {
        model.addAttribute("currentMonth", YearMonth.now());
        return "budget-page";
    }

    @PostMapping("/budget/create")
    public String createBudget(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("month") String month,
                               @RequestParam("amount") BigDecimal amount,
                               RedirectAttributes redirectAttributes) {
        if (amount == null || amount.signum() < 0) {
            redirectAttributes.addFlashAttribute("error", "Budget amount is required.");
            return "redirect:/budget/create";
        }

        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();
        budgetService.addOrUpdateBudget(user, YearMonth.parse(month), amount);
        return "redirect:/home";
    }

    @GetMapping("/budgets")
    public String budgets(@AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).get();
        List<Budget> budgets = budgetService.getBudgetsByUser(user);
        model.addAttribute("budgets", budgets);
        return "budgets";
    }

    @Transactional
    @PostMapping("/delete-budget")
    public String deleteBudget(@RequestParam("budgetId") Long budgetId,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }
        budgetService.deleteBudgetByIdAndUser(budgetId, currentUser.get());
        return "redirect:/budgets";
    }

    @Transactional
    @PostMapping("/delete-expense-from-budget")
    public String deleteExpenseFromBudget(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam("expenseId") Long expenseId) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();
        Category category = expensesService.getCategoryById(expenseId);
        categoryService.deleteCategory(category);
        expensesService.deleteByUserIdAndExpenseId(user.getId(), expenseId);

        return "redirect:/home";
    }





}
