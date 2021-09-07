package uns.ac.rs.userauth.kafka.domain;

import uns.ac.rs.userauth.domain.User;

public class UserMessage {
	
	private User user;
	
	private String type;

	
	public UserMessage() {
		super();
	}

	public UserMessage(User user, String type) {
		super();
		this.user = user;
		this.type = type;
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
