package com.cchqsa.budgetTracker.controller;

import com.cchqsa.budgetTracker.dto.UserDto;
import com.cchqsa.budgetTracker.dto.JwtAuthenticationDto;
import com.cchqsa.budgetTracker.dto.UserCredentialsDto;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.mapper.ModelMapper;
import com.cchqsa.budgetTracker.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;
    private final ModelMapper<User, UserDto> userMapper;

    public AuthController(UserService userService, ModelMapper<User, UserDto> userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, HttpServletResponse response) {
        userService.addUser(userMapper.mapTo(user));
        UserCredentialsDto credentials = new UserCredentialsDto();
        credentials.setEmail(user.getEmail());
        credentials.setPassword(user.getPassword());
        addAuthCookie(response, userService.signIn(credentials).getJwtToken());
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute("user") User user, HttpServletResponse response, Model model) {
        UserCredentialsDto credentials = new UserCredentialsDto();
        credentials.setEmail(user.getEmail());
        credentials.setPassword(user.getPassword());

        JwtAuthenticationDto authToken = userService.signIn(credentials);
        addAuthCookie(response, authToken.getJwtToken());
        return "redirect:/home";
    }

    private void addAuthCookie(HttpServletResponse response, String jwtToken) {
        Cookie cookie = new Cookie("jwt", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);
    }

}
