package com.java.dospring.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

/**
 * Booking entity.
 * Note: for production you may want to normalize payment/otp fields into dedicated tables.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "booking")
public class Booking extends Auditable implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "booking_id")
  private Long bookingId;

  @Column(name = "departure_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date departureDate;

  @Column(name = "booking_date")
  private LocalDate bookingDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "flight_id", foreignKey = @ForeignKey(name = "fk_booking_flightdata"))
  private FlightData flight;

  @Builder.Default
  @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Passenger> passengers = new ArrayList<>();

  @Column(name = "total_amount")
  private double totalAmount;

  private int otp;

  @Column(name = "booking_cancelled", nullable = false)
  @Builder.Default
  private boolean bookingCancelled = false;

  @Column(name = "checked_in", nullable = false)
  @Builder.Default
  private boolean checkedIn = false;

  @Column(name = "payment_completed", nullable = false)
  @Builder.Default
  private boolean paymentCompleted = false;

  public void addPassenger(Passenger passenger) {
    passengers.add(passenger);
    passenger.setBooking(this);
  }

  public void removePassenger(Passenger passenger) {
    passengers.remove(passenger);
    passenger.setBooking(null);
  }
}
