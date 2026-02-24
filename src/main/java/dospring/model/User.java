package dospring.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import dospring.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * User entity.
 *
 * Security hardening:
 * - failedLoginAttempts + lockUntil for brute-force mitigation
 * - passwordChangedAt for future policies (e.g., rotate credentials)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
    @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class User extends Auditable implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 50)
  @Column(nullable = false, length = 50)
  private String username;

  @NotBlank
  @Size(max = 120)
  @Email
  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, length = 512)
  private String email;

  @NotBlank
  @Size(max = 200)
  @Column(nullable = false, length = 200)
  private String password;

  @Builder.Default
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_roles_user")),
      inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_roles_role"))
  )
  private Set<Role> roles = new HashSet<>();

  @Column(name = "failed_login_attempts", nullable = false)
  @Builder.Default
  private int failedLoginAttempts = 0;

  @Column(name = "lock_until")
  private Instant lockUntil;

  @Column(name = "password_changed_at")
  private Instant passwordChangedAt;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @Column(nullable = false)
  @Builder.Default
  private boolean enabled = true;

  public boolean isLockedNow(Instant now) {
    return lockUntil != null && lockUntil.isAfter(now);
  }
}
