package dospring.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Password history to prevent password reuse.
 * Stores bcrypt hash, not plaintext.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "password_history", indexes = {
    @Index(name = "idx_password_history_user", columnList = "user_id")
})
public class PasswordHistory extends Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_password_history_user"))
  private User user;

  @Column(name = "password_hash", nullable = false, length = 200)
  private String passwordHash;
}
