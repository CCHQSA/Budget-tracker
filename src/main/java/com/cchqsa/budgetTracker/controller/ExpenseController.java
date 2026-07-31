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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Controller
public class ExpenseController {

    private final UserService userService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final ExpensesService expensesService;

    public ExpenseController(UserService userService, BudgetService budgetService,
                             CategoryService categoryService, ExpensesService expensesService) {
        this.userService = userService;
        this.budgetService = budgetService;
        this.categoryService = categoryService;
        this.expensesService = expensesService;
    }

    @GetMapping("/expenses")
    public String expenses(@AuthenticationPrincipal UserDetails userDetails,
                           @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
                           Model model) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();
        Budget budget = budgetService.findBudget(user, YearMonth.now()).orElse(null);
        if (budget == null) {
            return "redirect:/budget/create";
        }

        Page<Expense> expensePage = expensesService.findByUser(user, pageable);

        model.addAttribute("budget", budget);
        model.addAttribute("expensePage", expensePage);
        model.addAttribute("expenses", expensePage.getContent());
        return "expenses";
    }

    @GetMapping("/expenses/add")
    public String showAddExpenseForm(Model model) {
        model.addAttribute("expense", new Expense());
        return "add-expense";
    }

    @PostMapping("/add-expense")
    public String addExpense(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam("title") String title,
                             @RequestParam("amount") BigDecimal amount,
                             @RequestParam("category") String categoryName,
                             RedirectAttributes redirectAttributes) {

        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();
        Optional<Budget> currentBudget = budgetService.findBudget(user, YearMonth.now());
        if (currentBudget.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Create a budget before adding expenses.");
            return "redirect:/budget/create";
        }

        String normalizedCategoryName = categoryName.trim();
        Category category = categoryService.findFirstByUserAndNameIgnoreCase(user, normalizedCategoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(normalizedCategoryName);
                    newCategory.setUser(user);
                    return categoryService.save(newCategory);
                });

        Budget budget = currentBudget.get();
        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setDate(LocalDate.now());
        expense.setUser(user);
        expense.setCategory(category);
        expense.setBudget(budget);
        budget.getExpenses().add(expense);
        budgetService.saveBudget(budget);

        return "redirect:/expenses";
    }

    @Transactional
    @PostMapping("/delete-expense")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String deleteExpense(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam Long expenseId) {
        User user = userService.findByUsername(userDetails.getUsername()).get();

        Category category = expensesService.getCategoryById(expenseId);
        expensesService.deleteByUserIdAndExpenseId(user.getId(), expenseId);

        if (category != null) {
            long remainingExpenses = expensesService.countByCategory(category);

            if (remainingExpenses == 0) {
                categoryService.deleteCategory(category);
            }
        }

        return "redirect:/expenses";
    }

    @GetMapping("/budget-expenses")
    public String budgetExpense(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam Long budgetId,
                                @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
                                Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).get();
        Page<Expense> expensePage = budgetService.findExpensesByIdAndUser(budgetId, user, pageable);

        model.addAttribute("budgetId", budgetId);
        model.addAttribute("expensePage", expensePage);
        model.addAttribute("expenses", expensePage.getContent());
        return "budget-expenses";
    }

    @GetMapping("/expenses/edit/{id}")
    public String editExpense(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).get();
        Expense expense = expensesService.findByIdAndUser(id, user);
        model.addAttribute("expense", expense);
        return "edit-expense";
    }

    @PostMapping("/expenses/edit/{id}")
    public String saveEditedExpense(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable Long id,
                                    @RequestParam String title,
                                    @RequestParam BigDecimal amount,
                                    @RequestParam LocalDate date){
        User user = userService.findByUsername(userDetails.getUsername()).get();
        Expense expense = expensesService.findByIdAndUser(id, user);
        expense.setTitle(title);
        expense.setAmount(amount);
        expense.setDate(date);

        expensesService.updateExpense(expense);

        return "redirect:/expenses";
    }

    @GetMapping("user/budget/expenses")
    @PreAuthorize("hasRole('ADMIN')")
    public String userBudgetForAdmin(@RequestParam("userId") Long userId,
                                     @RequestParam("budgetId") Long budgetId,
                                     @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
                                     Model model) {

        User ownerUser = userService.findById(userId).get();

        Budget budget = budgetService.findByIdAndUser(budgetId, ownerUser).get();

        Page<Expense> expensePage = budgetService.findExpensesByIdAndUser(budgetId, ownerUser, pageable);

        model.addAttribute("budget", budget);
        model.addAttribute("expensePage", expensePage);
        model.addAttribute("expenses", expensePage.getContent());
        model.addAttribute("isAdminView", true);

        return "expenses";
    }

}
