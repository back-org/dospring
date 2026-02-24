package com.java.dospring.controller;

import com.java.dospring.model.Order;
import com.java.dospring.payload.response.MessageResponse;
import com.java.dospring.service.OrderService;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Order API (minimal).
 * In a real integration, create Razorpay order using the Razorpay SDK, then persist.
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "${app.security.cors-origins}", maxAge = 3600)
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<Order> create(@RequestParam Long userId,
                                      @RequestParam @NotBlank String razorpayOrderId) {
    return ResponseEntity.ok(orderService.createOrder(razorpayOrderId, userId));
  }

  @PutMapping("/paid")
  public ResponseEntity<Order> markPaid(@RequestParam @NotBlank String razorpayOrderId,
                                        @RequestParam @NotBlank String paymentId,
                                        @RequestParam(required = false) String signature) {
    return ResponseEntity.ok(orderService.markPaid(razorpayOrderId, paymentId, signature));
  }

  @GetMapping("/ping")
  public ResponseEntity<?> ping() {
    return ResponseEntity.ok(new MessageResponse("orders:ok"));
  }
}
