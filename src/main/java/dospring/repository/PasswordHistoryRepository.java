package dospring.repository;

import java.util.List;

import dospring.model.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;



/**
 * PasswordHistoryRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
  List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
