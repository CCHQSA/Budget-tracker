package com.cchqsa.budgetTracker.repository;

import com.cchqsa.budgetTracker.entity.Budget;
import com.cchqsa.budgetTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Long> {
    Optional<Budget> findFirstByUserAndMonthOrderByIdDesc(User user, YearMonth month);
}
