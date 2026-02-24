package dospring.repository;

import java.util.Optional;

import dospring.constants.ERole;
import dospring.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * RoleRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
