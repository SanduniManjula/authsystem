package authsystem.services;

import authsystem.entity.DualAuthSystem;
import authsystem.entity.Role;
import authsystem.entity.User;
import authsystem.model.UserDto;
import authsystem.repository.DualAuthSystemRepository;
import authsystem.repository.RoleRepository;
import authsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DualAuthSystemService {

    @Autowired
    private DualAuthSystemRepository dualAuthSystemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public UserDto createPendingUser(User user, Long creatorId) {
        log.info("Creating pending user: {} with creatorId: {}", user.getUsername(), creatorId);
        String newUserJson = convertToJson(user);


        DualAuthSystem dualAuthSystem = new DualAuthSystem();
        dualAuthSystem.setEntity("User");
        dualAuthSystem.setNewData(newUserJson);
        dualAuthSystem.setCreatedBy(creatorId);
        dualAuthSystem.setStatus(DualAuthSystem.Status.PENDING);
      //  dualAuthSystem.setCreatedAt(LocalDateTime.now());

        dualAuthSystemRepository.save(dualAuthSystem);

        return new UserDto(user.getId(), user.getUsername(), user.getRole().getId());
    }

    public boolean approveUser(Long id, Long reviewerId) {
        return processUser(id, reviewerId, DualAuthSystem.Status.APPROVED);
    }

    public boolean rejectUser(Long id, Long reviewerId) {
        return processUser(id, reviewerId, DualAuthSystem.Status.REJECTED);
    }

    private boolean processUser(Long id, Long reviewerId, DualAuthSystem.Status status) {
        return dualAuthSystemRepository.findByIdAndStatus(id, DualAuthSystem.Status.PENDING).map(dualAuthSystem -> {
            if (status == DualAuthSystem.Status.APPROVED) {
                User user = convertFromJson(dualAuthSystem.getNewData(), User.class);
                userRepository.save(user);
            }
            dualAuthSystem.setStatus(status);
            dualAuthSystem.setReviewedBy(reviewerId);
          //  dualAuthSystem.setUpdatedAt(LocalDateTime.now());
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
