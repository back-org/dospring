package dospring.model;

import java.util.List;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest implements Serializable{
    private String userName;
    private String customerName;
    private String email;
    private String phoneNumber;
    private String amount;
}