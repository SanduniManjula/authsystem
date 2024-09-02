package authsystem.repository;
import authsystem.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByCreatedOnBetweenOrUpdatedOnBetween(LocalDateTime startCreatedOn, LocalDateTime endCreatedOn, LocalDateTime startUpdatedOn, LocalDateTime endUpdatedOn);


    Optional<Role> findById(long id);
}
