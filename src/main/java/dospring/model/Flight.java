package dospring.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flights")
public class Flight  implements Serializable {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
private long id;
private String departure_location;
private String arrival_location;
private List<FlightData> flight;

}
