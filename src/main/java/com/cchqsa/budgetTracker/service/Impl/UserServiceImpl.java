package com.cchqsa.budgetTracker.service.Impl;

import com.cchqsa.budgetTracker.dto.JwtAuthenticationDto;
import com.cchqsa.budgetTracker.dto.RefreshTokenDto;
import com.cchqsa.budgetTracker.dto.UserCredentialsDto;
import com.cchqsa.budgetTracker.dto.UserDto;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.enums.Role;
import com.cchqsa.budgetTracker.mapper.ModelMapper;
import com.cchqsa.budgetTracker.repository.UserRepository;
import com.cchqsa.budgetTracker.security.jwt.JwtService;
import com.cchqsa.budgetTracker.service.UserService;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper<User, UserDto> userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ModelMapper<User, UserDto> userMapper, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto) {
        User user = findByCredentials(userCredentialsDto);
        return jwtService .generateJwtAuthToken(user.getEmail());
    }

    @Override
    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {
        String refreshToken = refreshTokenDto.getRefreshToken();

        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {

            User user = findByUsername(jwtService.getUserNameFromToken(refreshToken))
                    .orElseThrow(() -> new Exception("User not found"));

            return jwtService.refreshBaseToken(user.getEmail(), refreshToken);
        }

        throw new AuthenticationCredentialsNotFoundException("Invalid refresh token");
    }

    @Override
    public String addUser(UserDto userDto) {
        User user = userMapper.mapFrom(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
        return "User created";
    }


    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private User findByCredentials(UserCredentialsDto  userCredentialsDto) {
        Optional<User> optionalUser = userRepository.findByEmail(userCredentialsDto.getEmail());
        if(optionalUser.isPresent()){
            User user =  optionalUser.get();
            if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPassword())) {
                return user;
            }
        }
        throw new BadCredentialsException("Username or password or is not correct");
    }


}
