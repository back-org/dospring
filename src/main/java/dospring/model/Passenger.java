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
@Table(name = "passenger")

public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int passenger_id;
    public String passenger_name;
    public int passenger_age;
    public int passenger_seat;
    public double amount;
}
