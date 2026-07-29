package com.cchqsa.budgetTracker.dto;

import lombok.Data;

@Data
public class JwtAuthenticationDto {
    private String jwtToken;
    private String refreshToken;

}
