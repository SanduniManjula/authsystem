package authsystem.exceptions;

public class UserPendingApprovalException extends RuntimeException {
    public UserPendingApprovalException(String msg) {
        super(msg);
    }
}
