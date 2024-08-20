package authsystem.services;
import authsystem.entity.User;
import authsystem.model.UserSearchCriteria;
import authsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> searchUsers(Specification<User> spec) {
        return userRepository.findAll((Sort) spec);
    }

    public Page<User> searchUsers(UserSearchCriteria searchCriteria, Pageable pageable) {
        log.info("searchUsers searchCriteria.search -> {}", searchCriteria.getSearch());
        return userRepository.findByCriteria(searchCriteria.getSearch(), pageable);
    }
}
