package com.java.dospring.model;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse implements Serializable{
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
