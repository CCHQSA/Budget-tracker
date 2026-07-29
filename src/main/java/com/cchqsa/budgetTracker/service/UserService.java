package com.cchqsa.budgetTracker.service;

import com.cchqsa.budgetTracker.dto.JwtAuthenticationDto;
import com.cchqsa.budgetTracker.dto.RefreshTokenDto;
import com.cchqsa.budgetTracker.dto.UserCredentialsDto;
import com.cchqsa.budgetTracker.dto.UserDto;
import com.cchqsa.budgetTracker.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto);
    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception;
    String addUser(UserDto userDto);
    Optional<User> findByUsername(String username);
}
