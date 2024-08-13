package authsystem.repository;

import authsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	//Object findByUsername(String username);
    boolean existsByUsername(String username);

    User findByUsername(String username);

    @Query("SELECT u FROM User u JOIN u.role r WHERE " +
            "(:search IS NULL OR u.username LIKE %:search% OR r.name LIKE %:search%)")
    Page<User> findByCriteria(@Param("search") String search, Pageable pageable);

}
