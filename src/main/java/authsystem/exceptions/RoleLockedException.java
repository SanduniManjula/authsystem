package authsystem.exceptions;

public class RoleLockedException extends RuntimeException {
    public RoleLockedException(String message) {
        super(message);
    }
}
