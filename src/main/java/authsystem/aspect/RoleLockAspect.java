package authsystem.aspect;

import authsystem.annotation.CheckRoleLocked;
import authsystem.annotation.UnlockRoleAfterApproval;
import authsystem.repository.DualAuthSystemRepository;
import authsystem.repository.RoleRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleLockAspect {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DualAuthSystemRepository dualAuthSystemRepository;

    @Before("@annotation(authsystem.annotation.CheckRoleLocked) && args(roleId,..)")
    public void lockRoleBeforePending(Long roleId) {
        roleRepository.findById(roleId).ifPresent(role -> {
            role.setLocked(true);
            roleRepository.save(role);
        });
    }

    @Before("@annotation(authsystem.annotation.UnlockRoleAfterApproval) && args(roleId,..)")
    public void unlockRoleAfterApprovalOrRejection(Long roleId) {
        roleRepository.findById(roleId).ifPresent(role -> {
            role.setLocked(false);
            roleRepository.save(role);
        });
    }
}
