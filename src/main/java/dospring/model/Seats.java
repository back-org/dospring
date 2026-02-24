package dospring.model;

import java.io.Serializable;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Seats.
 *
 * <p>Enterprise V4+ documentation block.
 */
public class Seats implements Serializable{
private int seat_id;
private String status;
private int capacity;
private int rate;	
}
