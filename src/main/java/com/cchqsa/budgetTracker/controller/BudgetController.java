package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.service.BudgetService;
import com.cchqsa.budgetTracker.service.CategoryService;
import com.cchqsa.budgetTracker.service.ExpensesService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Optional;

@Controller
public class BudgetController {

    private final UserService userService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final ExpensesService expensesService;

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
                          @PageableDefault(size = 10, sort = "month", direction = Sort.Direction.DESC) Pageable pageable,
                          Model model) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();
        Page<Budget> budgetPage = budgetService.getBudgetsByUser(user, pageable);

        if (budgetPage == null) {
            model.addAttribute("budgetPage", Page.empty());
            model.addAttribute("budgets", Collections.emptyList());
        } else {
            model.addAttribute("budgetPage", budgetPage);
            model.addAttribute("budgets", budgetPage.getContent());
        }
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

    @GetMapping("/expenses-budget/edit/{id}")
    public String editExpense(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).get();

        Expense expense = expensesService.findByIdAndUser(id, user);
        model.addAttribute("expense", expense);
        return "edit-expense";
    }

    @GetMapping("/budget/edit")
    public String editBudget(@RequestParam("budgetId") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).get();
        Budget currBudget = budgetService.findByIdAndUser(id, user).get();

        model.addAttribute("budget", currBudget);
        model.addAttribute("currentMonth", currBudget.getMonth());

        return "edit-budget";
    }

    @PostMapping("/budget/edit")
    public String updateBudget(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("budgetId") Long id){
        User user = userService.findByUsername(userDetails.getUsername()).get();
        Budget currBudget = budgetService.findByIdAndUser(id, user).get();
        currBudget.setLimitAmount(amount);
        budgetService.addOrUpdateBudget(user, currBudget.getMonth(), amount);
        return "redirect:/budgets";
    }
}
