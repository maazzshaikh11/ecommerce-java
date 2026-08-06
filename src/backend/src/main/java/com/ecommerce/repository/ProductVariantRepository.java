package com.ecommerce.repository;

import com.ecommerce.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
    List<ProductVariant> findByProductId(@Param("productId") Integer productId);
}
