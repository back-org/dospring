package dospring.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Static flight data (schedule).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flight_data")
public class FlightData extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "flight_id")
  private Long flightId;

  @Column(name = "flight_number")
  private Integer flightNumber;

  @Column(name = "departure_time", length = 30)
  private String departureTime;

  @Column(name = "arrival_time", length = 30)
  private String arrivalTime;
}
