package authsystem.aspect;

import authsystem.annotation.CheckLockStatus;
import authsystem.entity.Role;
import authsystem.entity.User;
import authsystem.exceptions.EntityPendingApprovalException;
import authsystem.repository.RoleRepository;
import authsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;

@Aspect
@Component
@Slf4j
public class LockStatusAspect {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Before("@annotation(checkLockStatus) && args(entityId,..)")
    public void checkLockStatus(JoinPoint joinPoint, CheckLockStatus checkLockStatus, Long entityId) {
        String entityType = checkLockStatus.entityType();
        if ("User".equals(entityType)) {
            checkUserLockStatus(entityId);
        } else if ("Role".equals(entityType)) {
            checkRoleLockStatus(entityId);
        } else {
            log.error("Invalid entity type specified in annotation: {}", entityType);
            throw new IllegalArgumentException("Invalid entity type: " + entityType);
        }
    }

    private void checkUserLockStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if (user.isLocked()) {
            log.warn("Attempt to update locked user with ID: {}", userId);
            throw new EntityPendingApprovalException("User update is not allowed while the account is locked.");
        }
    }

    private void checkRoleLockStatus(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with ID: " + roleId));

        if (role.isLocked()) {
            log.warn("Attempt to update locked role with ID: {}", roleId);
            throw new EntityPendingApprovalException("Role update is not allowed while the account is locked.");
        }
    }
}
