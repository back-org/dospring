package dospring.repository;

import java.util.Optional;

import dospring.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
/**
 * OrderRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByRazorpayOrderId(String orderId);
}
