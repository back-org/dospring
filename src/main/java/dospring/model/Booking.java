package dospring.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "booking")
public class Booking implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long booking_id;

    public Date departure_date;
    public LocalDate booking_date;
    @ManyToOne
    @JoinColumn(name = "flight_flight_id")
    public FlightData flight;
    public List<Passenger> passenger = new ArrayList<>();
    public double total_amount;
    public int otp;
    public boolean booking_cancelled = false;
    public boolean checked_in = false;
    public boolean payment_completed = false;



}
