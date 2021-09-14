package uns.ac.rs.userauth.kafka.domain;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.domain.UserType;


public class UserMessage {
	
	private User user;
	
	private String oldUsername;
	
	private String type;
	
	private UserType role;

	
	public UserMessage() {
		super();
	}

	public UserMessage(User user, String type) {
		super();
		this.user = user;
		this.type = type;
	}

	public UserMessage(User user, String oldUsername, UserType role, String type) {
		super();
		this.user = user;
		this.oldUsername = oldUsername;
		this.role = role;
		this.type = type;
	}
	

	public UserType getRole() {
		return role;
	}

	public void setRole(UserType role) {
		this.role = role;
	}

	public String getOldUsername() {
		return oldUsername;
	}

	public void setOldUsername(String oldUsername) {
		this.oldUsername = oldUsername;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "UserMessage [user=" + user + ", type=" + type + "]";
	}

}
