package com.cchqsa.budgetTracker.dto;

import java.math.BigDecimal;

public class CategorySpentDto {
    private final Long id;
    private final String name;
    private final BigDecimal spent;

    public CategorySpentDto(Long id, String name, BigDecimal spent) {
        this.id = id;
        this.name = name;
        this.spent = spent;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getSpent() { return spent; }
}
