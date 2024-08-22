package authsystem.services;

import authsystem.entity.DualAuthSystem;
import authsystem.entity.User;
import authsystem.model.UserDto;
import authsystem.repository.DualAuthSystemRepository;
import authsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@Slf4j
public class DualAuthSystemService {

    @Autowired
    private DualAuthSystemRepository dualAuthSystemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    public UserDto createPendingUser(User user) {
        Long creatorId = getCurrentUserId();
        log.info("Creating pending user: {} with creatorId: {}", user.getUsername(), creatorId);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLocked(true);
        userRepository.save(user);

        String newUserJson = convertToJson(user);

        DualAuthSystem dualAuthSystem = new DualAuthSystem();
        dualAuthSystem.setEntity("User");
        dualAuthSystem.setNewData(newUserJson);
        dualAuthSystem.setCreatedBy(creatorId);
        dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
        dualAuthSystem.setAction(DualAuthSystem.Action.CREATE);

        dualAuthSystemRepository.save(dualAuthSystem);

        return new UserDto(user.getId(), user.getUsername(), user.getRole().getId());
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found for username: " + username));

        return user.getId();
    }

    public UserDto updateUser(Long id, User updatedUser) {
        Long creatorId = getCurrentUserId();
        Optional<User> existingUserOpt = userRepository.findById(id);

        User existingUser = existingUserOpt.orElseThrow(() -> new EntityNotFoundException("User not found"));

        String oldUserJson = convertToJson(existingUser);
        String newUserJson = convertToJson(updatedUser);

        DualAuthSystem dualAuthSystem = new DualAuthSystem();
        dualAuthSystem.setEntity("User");
        dualAuthSystem.setOldData(oldUserJson);
        dualAuthSystem.setNewData(newUserJson);
        dualAuthSystem.setCreatedBy(creatorId);
        dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
        dualAuthSystem.setAction(DualAuthSystem.Action.UPDATE);

        dualAuthSystemRepository.save(dualAuthSystem);

        updatedUser.setLocked(true);
        userRepository.save(updatedUser);

        return new UserDto(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getRole().getId());
    }

    public boolean approveUser(Long id) {
        Long reviewerId = getCurrentUserId();
        return processUser(id, reviewerId, DualAuthSystem.Status.APPROVED);
    }

    public boolean rejectUser(Long id) {
        Long reviewerId = getCurrentUserId();
        return processUser(id, reviewerId, DualAuthSystem.Status.REJECTED);
    }

    private boolean processUser(Long id, Long reviewerId, DualAuthSystem.Status status) {
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getNewData(), User.class);

            if (status == DualAuthSystem.Status.APPROVED) {
                if (user.getId() == null) {
                    throw new IllegalArgumentException("User ID must not be null");
                }
                Optional<User> existingUserOpt = userRepository.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    User existingUser = existingUserOpt.get();
                    existingUser.setLocked(false);
                    existingUser.setUsername(user.getUsername());
                    existingUser.setRole(user.getRole());
                    existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(existingUser);
                } else {
                    throw new EntityNotFoundException("User not found for approval");
                }
            } else if (status == DualAuthSystem.Status.REJECTED) {
                if (user.getId() == null) {
                    throw new IllegalArgumentException("User ID must not be null");
                }
                Optional<User> existingUserOpt = userRepository.findById(user.getId());
                if (existingUserOpt.isPresent()) {
                    User existingUser = existingUserOpt.get();
                    existingUser.setLocked(false);
                    userRepository.save(existingUser);
                } else {
                    throw new EntityNotFoundException("User not found for rejection");
                }
            }

            dualAuthSystem.setStatus(status);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    public boolean deleteUser(Long userId) {
        Long creatorId = getCurrentUserId();
        return userRepository.findById(userId).map(user -> {
            user.setLocked(true);
            String oldUserData = convertToJson(user);

            DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("User");
            dualAuthSystem.setOldData(oldUserData);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.DELETE);

            dualAuthSystemRepository.save(dualAuthSystem);



            return true;
        }).orElse(false);
    }

    public boolean approveUserDeletion(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);
            userRepository.deleteById(user.getId());
            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    public boolean rejectUserDeletion(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);
            user.setLocked(false);
            userRepository.save(user);
            dualAuthSystem.setStatus(DualAuthSystem.Status.REJECTED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);
            return true;
        }).orElse(false);
    }

    public boolean activateUser(Long userId) {
        Long creatorId = getCurrentUserId();
        return userRepository.findById(userId).map(user -> {
            String userJson = convertToJson(user);

            DualAuthSystem dualAuthSystem = new DualAuthSystem();
            dualAuthSystem.setEntity("User");
            dualAuthSystem.setOldData(userJson);
            dualAuthSystem.setCreatedBy(creatorId);
            dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
            dualAuthSystem.setAction(DualAuthSystem.Action.UPDATE);

            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }

    public boolean approveActivation(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);
            user.setStatus(User.Status.ACTIVATED);
            userRepository.save(user);

            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }

    public boolean approveDeactivation(Long id) {
        Long reviewerId = getCurrentUserId();
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            User user = convertFromJson(dualAuthSystem.getOldData(), User.class);
            user.setStatus(User.Status.DEACTIVATED);
            userRepository.save(user);

            dualAuthSystem.setStatus(DualAuthSystem.Status.APPROVED);
            dualAuthSystem.setReviewedBy(reviewerId);
            dualAuthSystemRepository.save(dualAuthSystem);

            return true;
        }).orElse(false);
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    private <T> T convertFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert from JSON", e);
        }
    }
}
