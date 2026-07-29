package com.cchqsa.budgetTracker.dto;


import com.cchqsa.budgetTracker.enums.Role;
import lombok.Data;

@Data
public class UserCredentialsDto {
    private String email;
    private String password;
    Role role;
}
