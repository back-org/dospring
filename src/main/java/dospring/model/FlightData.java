package dospring.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flightData")
public class FlightData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int flight_id;
    private int flight_number;
    private String departure_time;
    private String arrival_time;
}
