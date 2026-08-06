package com.ecommerce.repository;

import com.ecommerce.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer>, CartItemRepositoryCustom {

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.customerId = :customerId")
    void clearCartByCustomerId(@Param("customerId") Integer customerId);

    List<CartItem> findByCustomerId(Integer customerId);
}
