package authsystem.repository;
import authsystem.entity.DualAuthSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DualAuthSystemRepository extends JpaRepository<DualAuthSystem, Long> {
    Optional<DualAuthSystem> findByIdAndStatus(Long id, DualAuthSystem.Status status);
}
