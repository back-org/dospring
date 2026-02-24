package dospring.repository;

import java.util.Optional;

import dospring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * UserRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Boolean existsByUsername(String username);
  Boolean existsByEmail(String email);
}
