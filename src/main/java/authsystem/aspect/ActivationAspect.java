package authsystem.aspect;

import authsystem.annotation.CheckActivationStatus;
import authsystem.entity.Role;
import authsystem.entity.User;
import authsystem.repository.RoleRepository;
import authsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ActivationAspect {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Before("@annotation(checkActivationStatus) && args(entityId,..)")
    public void checkActivationStatus(CheckActivationStatus checkActivationStatus, Long entityId) {
        String entityType = checkActivationStatus.entityType();
        String action = checkActivationStatus.action();

        if ("User".equalsIgnoreCase(entityType)) {
            handleUserActivation(entityId, action);
        } else if ("Role".equalsIgnoreCase(entityType)) {
            handleRoleActivation(entityId, action);
        } else {
            throw new IllegalArgumentException("Invalid entity type: " + entityType);
        }
    }

    private void handleUserActivation(Long userId, String action) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if ("activate".equalsIgnoreCase(action)) {
            if (user.getStatus() == User.Status.ACTIVATED) {
                throw new RuntimeException("User is already activated, cannot activate again");
            }
            log.info("User with id {} is deactivated, proceeding with activation", userId);
        } else if ("deactivate".equalsIgnoreCase(action)) {
            if (!user.isActivated()) {
                throw new RuntimeException("User is already deactivated, cannot deactivate again");
            }
            log.info("User with id {} is activated, proceeding with deactivation", userId);
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    private void handleRoleActivation(Long roleId, String action) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if ("activate".equalsIgnoreCase(action)) {
            if (role.getStatus() == Role.Status.ACTIVATED) {
                throw new RuntimeException("Role is already activated, cannot activate again");
            }
            if (role.isLocked()) {
                throw new RuntimeException("Role is locked and cannot be activated");
            }
            log.info("Role with id {} is deactivated, proceeding with activation", roleId);
        } else if ("deactivate".equalsIgnoreCase(action)) {
            if (role.getStatus() == Role.Status.DEACTIVATED) {
                throw new RuntimeException("Role is already deactivated, cannot deactivate again");
            }
            if (role.isLocked()) {
                throw new RuntimeException("Role is locked and cannot be deactivated");
            }
            log.info("Role with id {} is activated, proceeding with deactivation", roleId);
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }
    }
}
