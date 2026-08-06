package com.ecommerce.repository;

import com.ecommerce.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM EcommerceOrder o WHERE o.customerId = :customerId ORDER BY o.orderDate DESC")
    List<Order> findOrdersByCustomer(@Param("customerId") Integer customerId);

    @Query("SELECT o FROM EcommerceOrder o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findOrderWithItems(@Param("id") Integer id);

    @Query("SELECT o FROM EcommerceOrder o LEFT JOIN FETCH o.orderItems WHERE o.idempotencyKey = :key")
    Optional<Order> findByIdempotencyKey(@Param("key") String key);
}
