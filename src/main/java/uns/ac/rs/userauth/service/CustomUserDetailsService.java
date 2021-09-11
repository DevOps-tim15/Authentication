package uns.ac.rs.userauth.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import uns.ac.rs.userauth.domain.Authority;
import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.domain.UserType;
import uns.ac.rs.userauth.domain.VerificationToken;
import uns.ac.rs.userauth.dto.UserRegistrationDTO;
import uns.ac.rs.userauth.kafka.Producer;
import uns.ac.rs.userauth.kafka.domain.UserMessage;
import uns.ac.rs.userauth.mapper.UserMapper;
import uns.ac.rs.userauth.repository.AuthorityRepository;
import uns.ac.rs.userauth.repository.UserRepository;
import uns.ac.rs.userauth.security.TokenUtils;
import uns.ac.rs.userauth.util.InvalidDataException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthorityRepository authorityRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private VerificationTokenService verificationService;

	@Autowired 
	private Producer producer;

	@Autowired
	private TokenUtils tokenUtils;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails u =  userRepository.findByUsername(username);
		if(u!= null)
			return u;
		else
			throw new UsernameNotFoundException(String.format("User with username '%s' not found", username));
	}
	
	
	public void encodePassword(User u) {
		String pass =  this.passwordEncoder.encode(u.getPassword());
		u.setPassword(pass);
	}
	
	public String encodePassword(String password) {
		return this.passwordEncoder.encode(password);		
	}
	
	public User saveRegisteredUser(UserRegistrationDTO ru) throws InvalidDataException, MailException, UnsupportedEncodingException, InterruptedException {
		
		User u = findByUsername(ru.getUsername());
		if(u != null) {
			throw new InvalidDataException("Username already taken!"); 
		}
		u = findByEmail(ru.getEmail());
		if(u != null) {
			throw new InvalidDataException("Email already taken!"); 
		}
		
		if(Stream.of(ru.getUsername(), ru.getFirstName(), ru.getLastName(), ru.getEmail(), ru.getPassword()).anyMatch(Objects::isNull)) {
			throw new InvalidDataException("Some data is missing");
		}
		
		if (ru.getUsername().isEmpty() || ru.getFirstName().isEmpty() || ru.getLastName().isEmpty() || ru.getEmail().isEmpty()
				|| ru.getPassword().isEmpty()) {
			throw new InvalidDataException("User information is incomplete!");
		}
		
		if (ru.getUsername().isEmpty() || ru.getFirstName().isEmpty() || ru.getLastName().isEmpty() || ru.getEmail().isEmpty()
				|| ru.getPassword().isEmpty()) {
			throw new InvalidDataException("User information is incomplete!");
		}
		u = UserMapper.toUser(ru);
		encodePassword(u);
		List<Authority> authorities = new ArrayList<Authority>();
		Authority a = findAuthority(1);
		authorities.add(a);
		u.setAuthorities(authorities);
		u.setVerified(false);
		u = this.userRepository.save(u);
		
		String token = UUID.randomUUID().toString();
		VerificationToken verToken = new VerificationToken();
		verToken.setId(null);
		verToken.setToken(token);
		verToken.setUser(u);
		verificationService.saveToken(verToken);
		String subject = "Confirmation of registration";
		String emailMessage = String.format("Confirm your registration on this link: \nhttp://localhost:4200/#/registration/confirmation/%s",URLEncoder.encode(token, "UTF-8"));
		emailService.sendNotificaitionAsyncRegistration(u, emailMessage, subject);
		return u;
	}
	
	public String updateUser(UserRegistrationDTO ru) throws InvalidDataException, JsonProcessingException {
		
		User contextUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String oldUsername = contextUser.getUsername();
		User user = findByUsername(contextUser.getUsername());
		User u = findByUsername(ru.getUsername());
		if(u != null && (!ru.getUsername().equals(user.getUsername()))) {
			throw new InvalidDataException("Username already taken!"); 
		}
		u = findByEmail(ru.getEmail());
		if(u != null && (!ru.getEmail().equals(user.getEmail()))) {
			throw new InvalidDataException("Email already taken!"); 
		}
		
		if(Stream.of(ru.getUsername(), ru.getFirstName(), ru.getLastName(), ru.getEmail()).anyMatch(Objects::isNull)) {
			throw new InvalidDataException("Some data is missing");
		}
		
		if (ru.getUsername().isEmpty() || ru.getFirstName().isEmpty() || ru.getLastName().isEmpty() || ru.getEmail().isEmpty()) {
			throw new InvalidDataException("User information is incomplete!");
		}
		
		if (ru.getUsername().isEmpty() || ru.getFirstName().isEmpty() || ru.getLastName().isEmpty() || ru.getEmail().isEmpty()
				) {
			throw new InvalidDataException("User information is incomplete!");
		}

		user.setUsername(ru.getUsername());
		user.setFirstName(ru.getLastName());
		user.setLastName(ru.getLastName());
		user.setEmail(ru.getEmail());
		user.setPhone(ru.getPhone());
		user.setSex(ru.getSex());
		user.setWebsiteUrl(ru.getWebsiteUrl());
		user.setBiography(ru.getBiography());
		user.setBirthDate(ru.getBirthDate());
		user.setIsPrivate(ru.getIsPrivate());
		user.setCanBeTagged(ru.getCanBeTagged());
		user.setAuthorities(contextUser.getAuthorities());
		user = this.userRepository.save(user);
		contextUser = user;
		String token = "noToken";
		if(!user.getUsername().equals(oldUsername)) {
			token = this.updateToken(user.getUsername(), user.getAuthorities().get(0).getUserType());
		}
		UserType ut = UserType.valueOf(user.getAuthorities().get(0).getUserType());
		UserMessage message = new UserMessage(user, oldUsername, ut , "update");
		producer.sendMessageToTopic("auth-topic", message);
		return token;
	}
	
	
	
	public User findByUsername(String username) {
		return userRepository.findByUsername(username);
	}
	
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public User findUserByToken(String token) {
		return userRepository.findByToken(token);
	}
	
	public Authority findAuthority(Integer id) {
		return authorityRepository.findById(id).get();
	}

	public boolean confirmRegistration(String token) throws InvalidDataException, JsonProcessingException {
		User user = findUserByToken(token);
		if (user != null) {
			user.setVerified(true);
			User u = this.userRepository.save(user);
			UserMessage message = new UserMessage(u, "registration");
			producer.sendMessageToTopic("auth-topic", message);
			return true;
		}else {
			throw new InvalidDataException("Invalid token!");
		}
	}
	
	public void deleteUser(User u) throws MailException, UnsupportedEncodingException, InterruptedException {
		User user = findByUsername(u.getUsername());
		verificationService.deleteToken(user);
		userRepository.delete(user);
		String message = "Error happend while creating account! Please, try to register again!";
		String subject = "Registration error!";
		emailService.sendNotificaitionAsyncRegistration(user, message, subject);
	}
	
	public UserRegistrationDTO getLoggedIn() {
		String loggedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = findByUsername(loggedUsername);
		UserRegistrationDTO dto = UserMapper.toUserRegistrationDTO(user);
		return dto;
	}
		
	private String updateToken(String username, String role) {
		String jwt = tokenUtils.generateToken(username, role);
		return jwt;
	}
}
