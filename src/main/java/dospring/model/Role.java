package dospring.model;

import java.io.Serializable;

import dospring.constants.ERole;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "roles")
public class Role implements Serializable{
	  @Id
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	  private long id;
	  private ERole name;

}