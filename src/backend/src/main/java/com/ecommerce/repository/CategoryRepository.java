package com.ecommerce.repository;

import com.ecommerce.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products p WHERE p.isActive = true OR p IS NULL")
    List<Category> findAllWithProducts();

    List<Category> findAllByOrderByNameAsc();
}
