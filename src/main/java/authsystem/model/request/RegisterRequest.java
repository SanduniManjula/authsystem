package authsystem.model.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@Getter
@Setter
public class RegisterRequest {
	private String username;
	private String password;
	private long role_id;

	public long getRole() {
		return role_id;
	}

}

