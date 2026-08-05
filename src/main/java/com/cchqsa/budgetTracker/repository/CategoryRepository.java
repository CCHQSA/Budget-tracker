package com.cchqsa.budgetTracker.repository;

import com.cchqsa.budgetTracker.dto.CategorySpentDto;
import com.cchqsa.budgetTracker.entity.Category;
import com.cchqsa.budgetTracker.entity.Expense;
import com.cchqsa.budgetTracker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findFirstByUserAndNameIgnoreCase(User user, String name);

    List<Category> findByUserId(Long userId);

    Page<Category> findByUserId(Long userId, Pageable pageable);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    List<Category> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    @Query("SELECT new com.cchqsa.budgetTracker.dto.CategorySpentDto(c.id, c.name, COALESCE(SUM(e.amount), 0) AS spent) " +
            "FROM Category c LEFT JOIN c.expenses e " +
            "WHERE c.user.id = :userId " +
            "GROUP BY c.id, c.name")
    Page<CategorySpentDto> findCategoriesWithSpentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.cchqsa.budgetTracker.dto.CategorySpentDto(c.id, c.name, COALESCE(SUM(e.amount), 0) AS spent) " +
            "FROM Category c LEFT JOIN c.expenses e " +
            "WHERE c.user.id = :userId AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "GROUP BY c.id, c.name")
    Page<CategorySpentDto> findCategoriesWithSpentByUserIdAndName(@Param("userId") Long userId, @Param("query") String query, Pageable pageable);

}
