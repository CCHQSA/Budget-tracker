package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.service.BudgetService;
import com.cchqsa.budgetTracker.service.UserService;
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
import java.util.Optional;

@Controller
public class BudgetController {

    private final UserService userService;
    private final BudgetService budgetService;

    public BudgetController(UserService userService, BudgetService budgetService) {
        this.userService = userService;
        this.budgetService = budgetService;
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
}
