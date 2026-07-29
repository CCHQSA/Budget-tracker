package com.cchqsa.budgetTracker.mapper;


public interface ModelMapper<A, B> {
    B mapTo(A a);
    A mapFrom(B b);
}
