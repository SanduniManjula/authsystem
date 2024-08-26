package authsystem.aspect;

import authsystem.annotation.CheckUserLocked;
import authsystem.entity.User;
import authsystem.exceptions.UserPendingApprovalException;
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
public class CheckUserLockedAspect {

    @Autowired
    private UserRepository userRepository;

    @Before("@annotation(checkUserLocked) && args(userId,..)")
    public void checkUserLockStatus(JoinPoint joinPoint, CheckUserLocked checkUserLocked, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));


        if (user.isLocked()) {
            log.warn("Attempt to update locked user with ID: {}", userId);
            throw new UserPendingApprovalException("User update is not allowed while the account is locked.");
        }
    }
}
