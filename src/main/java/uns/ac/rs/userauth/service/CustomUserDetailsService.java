package uns.ac.rs.userauth.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import uns.ac.rs.userauth.domain.Authority;
import uns.ac.rs.userauth.domain.User;
import uns.ac.rs.userauth.dto.UserRegistrationDTO;
import uns.ac.rs.userauth.mapper.UserMapper;
import uns.ac.rs.userauth.repository.AuthorityRepository;
import uns.ac.rs.userauth.repository.UserRepository;
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

	// Funkcija koja na osnovu username-a iz baze vraca objekat User-a
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
		this.userRepository.save(u);
		emailService.sendNotificaitionAsyncRegistration(u);
		return u;
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

	public boolean confirmRegistration(String token) throws InvalidDataException {
		User user = findUserByToken(token);
		if (user != null) {
			user.setVerified(true);
			this.userRepository.save(user);
			return true;
		}
		throw new InvalidDataException("Invalid token!");
	}
}
