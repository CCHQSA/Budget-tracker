package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.dto.CategorySpentDto;
import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.repository.CategoryRepository;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {


    private final UserService userService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final ExpensesService expensesService;

    public HomeController(UserService userService, BudgetService budgetService, CategoryRepository categoryRepository, CategoryService categoryService, ExpensesService expensesService) {
        this.userService = userService;
        this.budgetService = budgetService;
        this.categoryService = categoryService;
        this.expensesService = expensesService;
    }

    @GetMapping("/home")
    public String homeInfo(@AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        model.addAttribute("username", userDetails.getUsername());
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = currentUser.get();
        YearMonth currentMonth = YearMonth.now();

        budgetService.findBudget(user, currentMonth).ifPresent(budget -> {
            BigDecimal spent = budgetService.getSpent(budget);
            model.addAttribute("currentMonth", budget.getMonth());
            model.addAttribute("budget", budget.getLimitAmount());
            model.addAttribute("spent", spent);
            model.addAttribute("remaining", budget.getLimitAmount().subtract(spent));
            model.addAttribute("totalExpenses", budgetService.getTotalExpenses(budget));
        });

        return "home";
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

    @GetMapping("/expenses")
    public String expenses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> currentUser = userService.findByUsername(userDetails.getUsername());
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        Budget budget = budgetService.findBudget(currentUser.get(), YearMonth.now())
                .orElse(null);
        if (budget == null) {
            return "redirect:/budget/create";
        }

        model.addAttribute("budget", budget);
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


    @Transactional
    @GetMapping("/categories")
    public String viewCategories(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).get();

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



}
