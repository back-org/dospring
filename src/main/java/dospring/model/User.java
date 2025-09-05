package dospring.model;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
@Table(name= "users")
public class User implements Serializable{
	  @Id
	  private long id;
	  @NotBlank
	  @Size(max = 20)
	  private String username;
	  @NotBlank
	  @Size(max = 50)
	  @Email
	  private String email;
	  @NotBlank
	  @Size(max = 120)
	  private String password;

	  private Set<Role> roles = new HashSet<>();

}