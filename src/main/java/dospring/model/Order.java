package com.java.dospring.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;

/**
 * Payment order (Razorpay).
 * In production, consider storing only what is necessary and encrypting sensitive data at rest.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_order", indexes = {
    @Index(name = "idx_order_razorpay_order_id", columnList = "razorpay_order_id", unique = true)
})
public class Order extends Auditable implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_order_user"))
  private User user;

  @Column(name = "razorpay_payment_id", length = 120)
  private String razorpayPaymentId;

  @Column(name = "razorpay_order_id", length = 120, nullable = false)
  private String razorpayOrderId;

  @Column(name = "razorpay_signature", length = 200)
  private String razorpaySignature;
}
