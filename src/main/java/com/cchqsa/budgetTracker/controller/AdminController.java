package com.cchqsa.budgetTracker.controller;


import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.enums.Role;
import com.cchqsa.budgetTracker.service.BudgetService;
import com.cchqsa.budgetTracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("admin-page")
    public String adminPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).get();
        if (!(user.getRole() == Role.ROLE_ADMIN)) {
            return "redirect:/home";
        }
        List<User> users = userService.getAll();

        model.addAttribute("users", users);

        return "admin-page";
    }



}
