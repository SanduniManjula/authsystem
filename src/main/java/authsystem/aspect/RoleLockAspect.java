package authsystem.aspect;

import authsystem.annotation.CheckRoleLocked;
import authsystem.annotation.UnlockRoleAfterApproval;
import authsystem.entity.Role;
import authsystem.repository.DualAuthSystemRepository;
import authsystem.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RoleLockAspect {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DualAuthSystemRepository dualAuthSystemRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Before("@annotation(authsystem.annotation.CheckRoleLocked) && args(roleId,..)")
    public void lockRoleBeforePending(Long roleId) {
        roleRepository.findById(roleId).ifPresent(role -> {
            role.setLocked(true);
            roleRepository.save(role);
        });
    }

    @AfterReturning(pointcut = "@annotation(authsystem.annotation.UnlockRoleAfterApproval) && args(id)", returning = "result")
    public void unlockRoleAfterApproval(Long id, boolean result) {
        if (result) {
            dualAuthSystemRepository.findById(id).ifPresent(dualAuthSystem -> {
                if ("Role".equals(dualAuthSystem.getEntity())) {
                    try {
                        String newRoleData = dualAuthSystem.getNewData();
                        if (newRoleData != null && !newRoleData.isEmpty()) {
                            Role role = convertFromJson(newRoleData, Role.class);
                            role.setLocked(false);
                            roleRepository.save(role);
                        } else {
                            log.warn("Role JSON data is null or empty for approval ID: {}", id);
                        }
                    } catch (Exception e) {
                        log.error("Error processing role approval for ID: {}", id, e);
                    }
                }
            });
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