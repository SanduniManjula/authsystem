package authsystem.aspect;

import authsystem.entity.Role;
import authsystem.repository.RoleRepository;
import authsystem.annotation.ActivateRole;
import authsystem.annotation.DeactivateRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RoleActivationAspect {

    @Autowired
    private RoleRepository roleRepository;

    @Before("@annotation(authsystem.annotation.ActivateRole) && args(roleId,..)")
    public void checkBeforeActivation(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (role.getStatus() == Role.Status.ACTIVATED) {
            throw new RuntimeException("Role is already activated, cannot activate again");
        }

        if (role.isLocked()) {
            throw new RuntimeException("Role is locked and cannot be activated");
        }

        log.info("Role with id {} is deactivated, proceeding with activation", roleId);
    }

    @Before("@annotation(authsystem.annotation.DeactivateRole) && args(roleId,..)")
    public void checkBeforeDeactivation(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (role.getStatus() == Role.Status.DEACTIVATED) {
            throw new RuntimeException("Role is already deactivated, cannot deactivate again");
        }

        if (role.isLocked()) {
            throw new RuntimeException("Role is locked and cannot be deactivated");
        }

        log.info("Role with id {} is activated, proceeding with deactivation", roleId);
    }
}
