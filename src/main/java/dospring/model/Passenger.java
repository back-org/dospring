package dospring.model;

import dospring.model.Booking;
import jakarta.persistence.*;
import lombok.*;

/**
 * Passenger associated with a Booking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "passenger")
public class Passenger extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "passenger_id")
  private Long passengerId;

  @Column(name = "passenger_name", length = 120)
  private String passengerName;

  @Column(name = "passenger_age")
  private Integer passengerAge;

  @Column(name = "passenger_seat")
  private Integer passengerSeat;

  private Double amount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id", foreignKey = @ForeignKey(name = "fk_passenger_booking"))
  private Booking booking;
}
