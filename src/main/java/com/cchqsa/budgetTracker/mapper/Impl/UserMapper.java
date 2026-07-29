package com.cchqsa.budgetTracker.mapper.Impl;

import com.cchqsa.budgetTracker.dto.UserDto;
import com.cchqsa.budgetTracker.entity.User;
import com.cchqsa.budgetTracker.mapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements ModelMapper<User, UserDto> {

    @Override
    public UserDto mapTo(User user) {
        if (user == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        return userDto;
    }

    @Override
    public User mapFrom(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        return user;
    }
}
