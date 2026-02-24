package dospring.model;

import java.io.Serializable;


import dospring.constants.ERole;
import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity (RBAC).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
})
public class Role extends Auditable implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ERole name;
}
