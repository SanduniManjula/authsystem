package authsystem.exceptions;


public class EntityPendingApprovalException extends RuntimeException {

    public EntityPendingApprovalException(String message) {
        super(message);
    }

    public EntityPendingApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}
