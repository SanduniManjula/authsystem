package authsystem.repository;

import authsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByCreatedOnBetweenOrUpdatedOnBetween(LocalDateTime createdOnStart, LocalDateTime createdOnEnd, LocalDateTime updatedOnStart, LocalDateTime updatedOnEnd);

    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

     //User findByUsername(String username);
/*
    @Query("SELECT u FROM User u JOIN u.role r WHERE " +
            "(:search IS NULL OR u.username LIKE %:search% OR r.name LIKE %:search%)")

 */


    @Query("SELECT u FROM User u LEFT JOIN u.role r WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findByCriteria(@Param("keyword") String keyword, Pageable pageable);
}