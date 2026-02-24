package com.java.dospring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.dospring.model.Order;

@Repository
/**
 * OrderRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByRazorpayOrderId(String orderId);
}
