package dospring.service;

import dospring.model.Order;
import dospring.model.User;
import dospring.repository.OrderRepository;
import dospring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Order persistence service (Razorpay related).
 * Keep business logic here, not in controllers.
 */
@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public Order createOrder(String razorpayOrderId, Long userId) {
    Optional<User> user = userRepository.findById(userId);
    Order order = Order.builder()
        .razorpayOrderId(razorpayOrderId)
        .user(user.orElse(null))
        .build();
    return orderRepository.save(order);
  }

  @Transactional
  public Order markPaid(String razorpayOrderId, String paymentId, String signature) {
    Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found"));

    order.setRazorpayPaymentId(paymentId);
    order.setRazorpaySignature(signature);
    return orderRepository.save(order);
  }
}
