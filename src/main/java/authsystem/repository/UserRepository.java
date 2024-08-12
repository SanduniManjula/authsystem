package authsystem.repository;

import authsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	//Object findByUsername(String username);
    boolean existsByUsername(String username);

    User findByUsername(String username);

    //Optional< User> findByUsername(String username);
}
