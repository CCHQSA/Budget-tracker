package com.cchqsa.budgetTracker.repository;

import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findFirstByUserAndNameIgnoreCase(User user, String name);

    List<Category> findByUserId(Long userId);
}
