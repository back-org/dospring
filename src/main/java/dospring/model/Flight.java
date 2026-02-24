package com.java.dospring.model;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;

/**
 * Flight entity (example domain entity).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flights")
public class Flight extends Auditable implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "departure_location", length = 120)
  private String departureLocation;

  @Column(name = "arrival_location", length = 120)
  private String arrivalLocation;

  @Column(name = "departure_date", length = 30)
  private String departureDate;

  @Column(name = "arrival_date", length = 30)
  private String arrivalDate;

  @Column(name = "departure_time", length = 30)
  private String departureTime;

  @Column(name = "arrival_time", length = 30)
  private String arrivalTime;

  @Column(name = "total_seats")
  private Integer totalSeats;

  @Column(name = "available_seats")
  private Integer availableSeats;

  @Column(name = "price")
  private Double price;
}
