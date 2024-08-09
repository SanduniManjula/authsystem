package authsystem.model;

import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
	private String username;
	private String password;
	private Set<String> roles;


	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
	}


	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
}
