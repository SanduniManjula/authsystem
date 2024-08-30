package authsystem.aspect;

import authsystem.annotation.UnlockAfterApprovalOrRejection;
import authsystem.entity.Role;
import authsystem.entity.User;
import authsystem.exceptions.EntityPendingApprovalException;
import authsystem.repository.DualAuthSystemRepository;
import authsystem.repository.RoleRepository;
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
public class UnlockEntityAspect {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DualAuthSystemRepository dualAuthSystemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(unlockAfterApprovalOrRejection) && args(id, ..)", returning = "result")
    public void unlockEntityAfterApprovalOrRejection(Long id, boolean result, UnlockAfterApprovalOrRejection unlockAfterApprovalOrRejection) {
        if (result) {
            String entityType = unlockAfterApprovalOrRejection.entityType();
            dualAuthSystemRepository.findById(id).ifPresent(dualAuthSystem -> {
                try {
                    if ("User".equals(entityType)) {
                        processUserUnlock(dualAuthSystem);
                    } else if ("Role".equals(entityType)) {
                        processRoleUnlock(dualAuthSystem);
                    } else {
                        log.error("Invalid entity type specified: {}", entityType);
                        throw new IllegalArgumentException("Invalid entity type: " + entityType);
                    }
                } catch (Exception e) {
                    log.error("Error processing {} approval/rejection for ID: {}", entityType, id, e);
                }
            });
        }
    }

    private void processUserUnlock(authsystem.entity.DualAuthSystem dualAuthSystem) throws Exception {
        String newUserData = dualAuthSystem.getNewData();
        if (newUserData != null && !newUserData.isEmpty()) {
            User user = convertFromJson(newUserData, User.class);
            user.setLocked(false);
            userRepository.save(user);
        } else {
            log.warn("User JSON data is null or empty for dualAuthSystem ID: {}", dualAuthSystem.getId());
        }
    }

    private void processRoleUnlock(authsystem.entity.DualAuthSystem dualAuthSystem) throws Exception {
        String newRoleData = dualAuthSystem.getNewData();
        if (newRoleData != null && !newRoleData.isEmpty()) {
            Role role = convertFromJson(newRoleData, Role.class);
            role.setLocked(false);
            roleRepository.save(role);
        } else {
            log.warn("Role JSON data is null or empty for dualAuthSystem ID: {}", dualAuthSystem.getId());
        }
    }

    private <T> T convertFromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("Argument 'json' is null or empty");
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert from JSON", e);
        }
    }
}
