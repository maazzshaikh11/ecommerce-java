package com.ecommerce.repository;

import com.ecommerce.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.name")
    List<Product> findActiveProducts();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :term, '%'))) ORDER BY p.name")
    List<Product> search(@Param("term") String term);

    List<Product> findByCategoryIdAndIsActiveTrue(Integer categoryId);

    // Do not use multiple LEFT JOIN FETCH on @OneToMany collections — MultipleBagFetchException
    Optional<Product> findById(Integer id);
}
