package dospring.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.*;


/**
 * The persistent class for the user_order database table.
 *
 */

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 65981149772133526L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId;

    private String razorpayPaymentId;

    private String razorpayOrderId;

    private String razorpaySignature;

}