package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.service.BudgetService;
import com.cchqsa.budgetTracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserService userService;
    private final BudgetService budgetService;

    public HomeController(UserService userService, BudgetService budgetService) {
        this.userService = userService;
        this.budgetService = budgetService;
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
}
