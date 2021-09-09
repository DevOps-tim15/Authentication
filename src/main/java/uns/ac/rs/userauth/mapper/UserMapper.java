package uns.ac.rs.userauth.mapper;

import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.dto.UserRegistrationDTO;

public class UserMapper {
	
	
	public static User toUser(UserRegistrationDTO dto) {
		return new User(dto.getUsername(), dto.getPassword(), 
				dto.getEmail(), dto.getFirstName(), dto.getLastName(),
				dto.getPhone(), dto.getWebsiteUrl(), dto.getSex(),
				dto.getBirthDate(), dto.getBiography(), dto.getCanBeTagged(), dto.getIsPrivate());
	}

}
