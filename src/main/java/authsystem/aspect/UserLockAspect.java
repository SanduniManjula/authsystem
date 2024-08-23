package authsystem.aspect;

import authsystem.entity.User;
import authsystem.repository.DualAuthSystemRepository;
import authsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class UserLockAspect {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DualAuthSystemRepository dualAuthSystemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(authsystem.annotation.UnlockUserAfterApproval) && args(id)", returning = "result")
    public void unlockUserAfterApproval(Long id, boolean result) {
        if (result) {
            dualAuthSystemRepository.findById(id).ifPresent(dualAuthSystem -> {
                if ("User".equals(dualAuthSystem.getEntity())) {
                    try {
                        String newUserData = dualAuthSystem.getNewData();
                        if (newUserData != null && !newUserData.isEmpty()) {
                            User user = convertFromJson(newUserData, User.class);
                            user.setLocked(false);
                            userRepository.save(user);
                        } else {
                            log.warn("User JSON data is null or empty for approval ID: {}", id);
                        }
                    } catch (Exception e) {
                        log.error("Error processing user approval for ID: {}", id, e);
                    }
                }
            });
        }
    }

    @AfterReturning(pointcut = "execution(* authsystem.services.DualAuthSystemService.reject*(..)) && args(id)", returning = "result")
    public void unlockUserAfterRejection(Long id, boolean result) {
        if (result) {
            dualAuthSystemRepository.findById(id).ifPresent(dualAuthSystem -> {
                if ("User".equals(dualAuthSystem.getEntity())) {
                    try {
                        String oldUserData = dualAuthSystem.getOldData();
                        if (oldUserData != null && !oldUserData.isEmpty()) {
                            User user = convertFromJson(oldUserData, User.class);
                            user.setLocked(false);
                            userRepository.save(user);
                        } else {
                            log.warn("User JSON data is null or empty for rejection ID: {}", id);
                        }
                    } catch (Exception e) {
                        log.error("Error processing user rejection for ID: {}", id, e);
                    }
                }
            });
        }
    }

    private <T> T convertFromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("Argument 'content' is null or empty");
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert from JSON", e);
        }
    }
}
