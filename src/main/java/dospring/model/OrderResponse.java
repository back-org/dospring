package dospring.model;

import lombok.Data;

@Data
/**
 * OrderResponse.
 *
 * <p>Enterprise V4+ documentation block.
 */
public class OrderResponse {
    private String applicationFee;
    private String razorpayOrderId;
    private String secretKey;
}
