package authsystem.aspect;

import authsystem.entity.User;
import authsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class UserActivationAspect {

    @Autowired
    private UserRepository userRepository;


    @Before("@annotation(authsystem.annotation.CheckBeforeActivation) && args(userId,..)")
    public void checkBeforeActivation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getStatus() == User.Status.ACTIVATED) {
            throw new RuntimeException("User is already activated, cannot activate again");
        }

        log.info("User with id {} is deactivated, proceeding with activation", userId);
    }


    @Before("@annotation(authsystem.annotation.CheckBeforeDeactivation) && args(userId,..)")
    public void checkBeforeDeactivation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!user.isActivated()) {
            throw new RuntimeException("User is already deactivated, cannot deactivate again");
        }

        log.info("User with id {} is activated, proceeding with deactivation", userId);
    }
}
